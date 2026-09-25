package monopoly.ui

import monopoly.domain.model.GameSession
import monopoly.domain.model.Move
import monopoly.domain.model.MoveHistory
import monopoly.domain.model.Player
import monopoly.domain.model.PlayerStatistics
import monopoly.domain.model.card.CardFactory
import monopoly.domain.model.card.DeductionCard
import monopoly.domain.model.card.Deck
import monopoly.domain.model.cell.Cell
import monopoly.domain.model.cell.CellFactory
import monopoly.domain.model.cell.CellType
import monopoly.domain.model.property.Property
import monopoly.domain.model.property.Railroad
import monopoly.domain.model.property.Street
import monopoly.domain.model.property.Utility
import monopoly.domain.repository.GameHistoryRepository
import monopoly.domain.repository.GameRepository
import monopoly.domain.repository.MoveHistoryRepository
import monopoly.domain.repository.PlayerStatisticsRepository
import monopoly.domain.service.AuctionService
import monopoly.domain.service.BankruptcyService
import monopoly.domain.service.CardService
import monopoly.domain.service.DebtService
import monopoly.domain.service.GameRules
import monopoly.domain.service.MoveDescriptionBuilder
import monopoly.domain.service.MoveService
import monopoly.domain.service.MovementService
import monopoly.domain.service.PropertyService
import java.util.UUID

class GameMenu(
    private val input: ConsoleInput,
    private val output: ConsoleOutput,
    private val gameRepository: GameRepository,
    private val statisticsRepository: PlayerStatisticsRepository,
    private val gameHistoryRepository: GameHistoryRepository,
    private val moveHistoryRepository: MoveHistoryRepository,
) {
    private val auctionService = AuctionService()
    private val bankruptcyService = BankruptcyService()
    private val cardService = CardService()
    private val moveService = MoveService()

    fun showMainMenu() {
        while (true) {
            output.println()
            output.println("Главное меню")
            output.println("1 — Новая игра")
            output.println("2 — Продолжить незавершённую игру")
            output.println("3 — Статистика игроков")
            output.println("4 — Общая история игр")
            output.println("5 — История ходов конкретной игры")
            output.println("6 — Выйти")
            when (input.readLine().trim()) {
                "1" -> createGame()
                "2" -> continueGame()
                "3" -> showStatistics()
                "4" -> showGameHistory()
                "5" -> showMoveHistory()
                "6" -> return
                else -> output.println("Некорректный ввод.")
            }
        }
    }

    private fun createGame() {
        val count = readPlayerCount() ?: return
        val players = mutableListOf<Player>()

        repeat(count) {
            while (true) {
                output.println("Введите имя игрока ${it + 1}:")
                val name = input.readLine()
                if (name.length !in 1..50) {
                    output.println("Имя должно содержать от 1 до 50 символов.")
                    continue
                }
                if (players.any { player -> player.name.equals(name, ignoreCase = true) }) {
                    output.println("Такое имя уже используется в этой игре.")
                    continue
                }

                val statistics = statisticsRepository.findByName(name)
                val playerId = statistics?.playerId ?: UUID.randomUUID().toString()
                players += Player(playerId, name)
                break
            }
        }

        val game = GameSession(
            id = UUID.randomUUID().toString(),
            players = players,
            board = CellFactory.createBoard(),
            chanceDeck = CardFactory.createChanceDeck(),
            communityChestDeck = CardFactory.createCommunityChestDeck(),
        )
        gameRepository.save(game)
        output.println("Игра создана. ID: ${game.id}")
        cardSetupMenu(game)
    }

    private fun readPlayerCount(): Int? {
        output.println("Введите количество игроков от 2 до 8:")
        val count = input.readLine().toIntOrNull()
        if (count !in 2..8) {
            output.println("Некорректное количество игроков.")
            return null
        }
        return count
    }

    private fun cardSetupMenu(game: GameSession) {
        while (game.active) {
            output.println()
            output.println("К1 — Настройка колод")
            output.println("1 — Расставить карты вручную")
            output.println("2 — Оставить карты по порядку")
            output.println("3 — Завершить создание игры")
            when (input.readLine().trim()) {
                "1" -> {
                    if (manualReorder(game.chanceDeck, "Шанс") &&
                        manualReorder(game.communityChestDeck, "Общественная казна")) {
                        startGame(game)
                        return
                    }
                }
                "2" -> {
                    startGame(game)
                    return
                }
                "3" -> {
                    gameRepository.delete(game.id)
                    output.println("Игра удалена.")
                    return
                }
                else -> output.println("Некорректный ввод.")
            }
        }
    }

    private fun manualReorder(deck: Deck, title: String): Boolean {
        output.println("Колода «$title»:")
        deck.cards.forEach { output.println("${it.number} — ${it.text}") }
        output.println("Введите номера всех 10 карт в нужном порядке через пробел:")
        val order = input.readLine().trim().split(Regex("\\s+")).mapNotNull { it.toIntOrNull() }
        if (order.size != 10 || order.toSet() != (1..10).toSet()) {
            output.println("Некорректный порядок. Вернитесь к настройке колод.")
            return false
        }
        deck.reorder(order)
        return true
    }

    private fun startGame(game: GameSession) {
        output.println("Игра ${game.id} началась.")
        output.println("Ход игрока ${game.currentPlayer.name}")
        gameLoop(game)
    }

    private fun gameLoop(game: GameSession) {
        while (game.active) {
            output.println()
            output.println("Игра ${game.id}. Сейчас ходит ${game.currentPlayer.name}.")
            output.println("1 — Сделать ход")
            output.println("2 — Сохранить игру и выйти в главное меню")
            when (input.readLine().trim()) {
                "1" -> {
                    val turnCompleted = playTurn(game)
                    if (!turnCompleted) continue
                    if (game.winner() != null) finishGame(game)
                    else if (game.active) {
                        game.moveToNextActivePlayer()
                        output.println("Ход игрока ${game.currentPlayer.name}")
                    }
                }
                "2" -> {
                    gameRepository.save(game)
                    moveHistoryRepository.save(game.moveHistory)
                    output.println("Игра сохранена. ID: ${game.id}")
                    return
                }
                else -> output.println("Некорректный ввод.")
            }
        }
    }

    private fun playTurn(game: GameSession): Boolean {
        val player = game.currentPlayer
        val builder = MoveDescriptionBuilder()
        val moveNumber = game.moveHistory.moveCount + 1

        if (player.atFreeParking) {
            player.atFreeParking = false
            builder.cellEffect("Игрок пропустил ход после бесплатной стоянки.")
            saveMove(game, moveService.createMove(moveNumber, player, builder))
            return true
        }

        if (player.inJail) {
            handleJail(game, player, builder)
            saveMove(game, moveService.createMove(moveNumber, player, builder))
            return true
        }

        val dice = readDiceUntilValid() ?: return false
        builder.diceRoll(dice.first, dice.second)
        val passedStart = MovementService.move(player, dice.first + dice.second)
        if (passedStart) builder.passedStart()
        addLandingDescription(game, player, builder)
        handleCell(game, player, builder, dice.first + dice.second)
        saveMove(game, moveService.createMove(moveNumber, player, builder))
        return true
    }

    private fun readDiceUntilValid(): Pair<Int, Int>? {
        while (true) {
            output.println("Введите два значения кубиков через пробел или 0 для выхода из текущего хода:")
            val line = input.readLine().trim()
            if (line == "0") return null
            val parts = line.split(Regex("\\s+"))
            if (parts.size == 2) {
                val first = parts[0].toIntOrNull()
                val second = parts[1].toIntOrNull()
                if (first in 1..6 && second in 1..6) return first!! to second!!
            }
            output.println("Некорректные значения кубиков.")
        }
    }

    private fun addLandingDescription(game: GameSession, player: Player, builder: MoveDescriptionBuilder) {
        val cell = game.board.getCell(player.position)
        builder.movedTo(cell.name, cell.number)
    }

    private fun handleJail(game: GameSession, player: Player, builder: MoveDescriptionBuilder) {
        builder.jail(player.jailTurns)
        while (true) {
            output.println("Вы в тюрьме. Пропущено ходов: ${player.jailTurns}.")
            output.println("1 — Бросить кубики")
            if (player.balance >= GameRules.JAIL_FINE) output.println("2 — Заплатить штраф 50 М")
            if (DebtService.availableProperties(player).isNotEmpty()) output.println("3 — Заложить недвижимость")
            if (player.streets().any { it.houses > 0 }) output.println("4 — Продать дом")
            output.println("5 — Объявить себя банкротом")

            when (input.readLine().trim()) {
                "1" -> {
                    val dice = readDiceUntilValid() ?: continue
                    builder.diceRoll(dice.first, dice.second)
                    if (dice.first == dice.second) {
                        player.inJail = false
                        player.jailTurns = 0
                        val passed = MovementService.move(player, dice.first + dice.second)
                        if (passed) builder.passedStart()
                        addLandingDescription(game, player, builder)
                        handleCell(game, player, builder, dice.first + dice.second)
                        return
                    }
                    player.jailTurns++
                    if (player.jailTurns >= 3) {
                        handleMandatoryJailPayment(game, player, builder)
                        return
                    }
                    output.println("Бросок не дал дубль. Ход завершён.")
                    return
                }
                "2" -> {
                    if (player.balance >= GameRules.JAIL_FINE) {
                        DebtService.payToBank(player, GameRules.JAIL_FINE)
                        player.inJail = false
                        player.jailTurns = 0
                        builder.paid(GameRules.JAIL_FINE, "освобождение из тюрьмы")
                        val dice = readDiceUntilValid() ?: return
                        builder.diceRoll(dice.first, dice.second)
                        val passed = MovementService.move(player, dice.first + dice.second)
                        if (passed) builder.passedStart()
                        addLandingDescription(game, player, builder)
                        handleCell(game, player, builder, dice.first + dice.second)
                        return
                    }
                    output.println("Недостаточно денег.")
                }
                "3" -> mortgageMenu(player, builder)
                "4" -> sellHouseMenu(player, builder)
                "5" -> {
                    bankruptToBank(game, player, builder)
                    return
                }
                else -> output.println("Некорректный ввод.")
            }
        }
    }

    private fun handleMandatoryJailPayment(game: GameSession, player: Player, builder: MoveDescriptionBuilder) {
        while (true) {
            output.println("Три хода в тюрьме прошли без дубля. Нужно заплатить 50 М или объявить себя банкротом.")
            output.println("1 — Заплатить штраф 50 М")
            output.println("2 — Заложить недвижимость")
            output.println("3 — Продать дом")
            output.println("4 — Объявить себя банкротом")
            when (input.readLine().trim()) {
                "1" -> {
                    if (player.balance < GameRules.JAIL_FINE) {
                        output.println("Недостаточно денег. Заложите недвижимость, продайте дом или объявите банкротство.")
                        continue
                    }
                    DebtService.payToBank(player, GameRules.JAIL_FINE)
                    player.inJail = false
                    player.jailTurns = 0
                    builder.paid(GameRules.JAIL_FINE, "освобождение из тюрьмы")
                    val dice = readDiceUntilValid() ?: return
                    builder.diceRoll(dice.first, dice.second)
                    val passed = MovementService.move(player, dice.first + dice.second)
                    if (passed) builder.passedStart()
                    addLandingDescription(game, player, builder)
                    handleCell(game, player, builder, dice.first + dice.second)
                    return
                }
                "2" -> mortgageMenu(player, builder)
                "3" -> sellHouseMenu(player, builder)
                "4" -> {
                    bankruptToBank(game, player, builder)
                    return
                }
                else -> output.println("Некорректный ввод.")
            }
        }
    }

    private fun handleCell(game: GameSession, player: Player, builder: MoveDescriptionBuilder, diceSum: Int) {
        val cell = game.board.getCell(player.position)
        when (cell.type) {
            CellType.START -> {
                builder.cellEffect("Игрок находится на клетке «старт».")
                actionMenu(game, player, builder)
            }
            CellType.STREET, CellType.RAILROAD, CellType.UTILITY -> {
                handleProperty(game, player, cell, builder, diceSum)
            }
            CellType.TAX -> handleBankDebt(game, player, if (cell.number == 4) GameRules.TAX else GameRules.SUPER_TAX, "налог", builder)
            CellType.CHANCE -> handleCard(game, player, game.chanceDeck, builder)
            CellType.COMMUNITY_CHEST -> handleCard(game, player, game.communityChestDeck, builder)
            CellType.JAIL -> {
                builder.cellEffect("Игрок просто посетил тюрьму.")
                actionMenu(game, player, builder)
            }
            CellType.FREE_PARKING -> {
                player.atFreeParking = true
                builder.freeParking()
                actionMenu(game, player, builder)
            }
            CellType.GO_TO_JAIL -> {
                player.position = 10
                player.inJail = true
                player.jailTurns = 0
                builder.cellEffect("Игрок отправлен в тюрьму.")
                actionMenu(game, player, builder)
            }
        }
    }

    private fun handleProperty(game: GameSession, player: Player, cell: Cell, builder: MoveDescriptionBuilder, diceSum: Int) {
        val property = cell.property ?: return
        when {
            property.owner == null -> buyMenu(game, player, property, builder)
            property.owner === player -> actionMenu(game, player, builder)
            else -> {
                val rent = when (property) {
                    is Street -> if (property.mortgaged) 0 else property.calculateRent()
                    is Railroad -> if (property.mortgaged) 0 else property.calculateRent()
                    is Utility -> if (property.mortgaged) 0 else property.calculateRent(diceSum)
                    else -> 0
                }
                if (rent == 0) builder.cellEffect("Аренда не начисляется.")
                else debtMenu(player, property.owner!!, rent, builder)
            }
        }
    }

    private fun buyMenu(game: GameSession, player: Player, property: Property, builder: MoveDescriptionBuilder) {
        while (true) {
            output.println("А4 — Недвижимость свободна: «${property.name}», цена ${property.price} М")
            output.println("1 — Купить")
            output.println("2 — Отказаться")
            when (input.readLine().trim()) {
                "1" -> {
                    try {
                        PropertyService.buy(player, property)
                        builder.boughtProperty(property.name, property.price)
                        return
                    } catch (e: IllegalArgumentException) {
                        output.println(e.message ?: "Покупка невозможна.")
                    }
                }
                "2" -> {
                    startAuction(game, listOf(property))
                    return
                }
                else -> output.println("Некорректный ввод.")
            }
        }
    }

    private fun actionMenu(game: GameSession, player: Player, builder: MoveDescriptionBuilder) {
        while (true) {
            output.println("А3 — Действия с недвижимостью")
            output.println("1 — Построить дом")
            output.println("2 — Продать дом")
            output.println("3 — Заложить землю")
            output.println("4 — Снять залог")
            output.println("5 — Продолжить")
            output.println("6 — Объявить себя банкротом")
            when (input.readLine().trim()) {
                "1" -> buildHouseMenu(player, builder)
                "2" -> sellHouseMenu(player, builder)
                "3" -> mortgageMenu(player, builder)
                "4" -> unmortgageMenu(player, builder)
                "5" -> return
                "6" -> {
                    bankruptToBank(game, player, builder)
                    return
                }
                else -> output.println("Некорректный ввод.")
            }
        }
    }

    private fun debtMenu(debtor: Player, creditor: Player, amount: Int, builder: MoveDescriptionBuilder) {
        while (true) {
            output.println("А5 — Долг игроку ${creditor.name}: $amount М")
            output.println("1 — Оплатить аренду")
            output.println("2 — Продать дом")
            output.println("3 — Заложить землю")
            output.println("4 — Объявить себя банкротом")
            when (input.readLine().trim()) {
                "1" -> {
                    if (debtor.balance < amount) {
                        output.println("Недостаточно денег. Продайте дом или заложите недвижимость.")
                        continue
                    }
                    DebtService.payToPlayer(debtor, creditor, amount)
                    builder.paid(amount, "аренда")
                    return
                }
                "2" -> sellHouseMenu(debtor, builder)
                "3" -> mortgageMenu(debtor, builder)
                "4" -> {
                    bankruptcyService.bankruptToPlayer(debtor, creditor)
                    builder.bankruptcy(debtor.name)
                    return
                }
                else -> output.println("Некорректный ввод.")
            }
        }
    }

    private fun handleBankDebt(game: GameSession, player: Player, amount: Int, reason: String, builder: MoveDescriptionBuilder) {
        while (true) {
            output.println("В4 — Долг банку: $amount М ($reason)")
            output.println("1 — Заплатить")
            output.println("2 — Продать дом")
            output.println("3 — Заложить землю")
            output.println("4 — Объявить себя банкротом")
            when (input.readLine().trim()) {
                "1" -> {
                    if (player.balance < amount) {
                        output.println("Недостаточно денег. Продайте дом или заложите недвижимость.")
                        continue
                    }
                    DebtService.payToBank(player, amount)
                    builder.paid(amount, reason)
                    return
                }
                "2" -> sellHouseMenu(player, builder)
                "3" -> mortgageMenu(player, builder)
                "4" -> {
                    bankruptToBank(game, player, builder)
                    return
                }
                else -> output.println("Некорректный ввод.")
            }
        }
    }

    private fun handleCard(game: GameSession, player: Player, deck: Deck, builder: MoveDescriptionBuilder) {
        val result = cardService.draw(player, deck)
        builder.cardDrawn(result.card.text)
        when (val card = result.card) {
            is DeductionCard -> handleBankDebt(game, player, card.amount, "эффект карточки", builder)
            else -> {
                if (result.passedStart) builder.passedStart()
                if (result.movedToJail) builder.cellEffect("Игрок отправлен в тюрьму.")
                if (card is monopoly.domain.model.card.IncomeCard) builder.received(card.amount, "эффект карточки")
                if (card is monopoly.domain.model.card.SpecificMovementCard ||
                    card is monopoly.domain.model.card.NearestMovementCard) {
                    addLandingDescription(game, player, builder)
                    if (!result.movedToJail) handleCell(game, player, builder, 0)
                }
            }
        }
        builder.cardEffect(result.card.text)
    }

    private fun buildHouseMenu(player: Player, builder: MoveDescriptionBuilder) {
        val streets = player.streets().filter { GameRules.canBuild(player, it) }
        if (streets.isEmpty()) {
            output.println("Нет улиц, на которых можно построить дом.")
            return
        }
        val street = chooseProperty(streets, "улицу для строительства") ?: return
        try {
            PropertyService.buildHouse(player, street)
            builder.builtHouse(street.name, street.houseCost)
        } catch (e: IllegalArgumentException) {
            output.println(e.message ?: "Невозможно построить дом.")
        }
    }

    private fun sellHouseMenu(player: Player, builder: MoveDescriptionBuilder) {
        val streets = player.streets().filter { it.houses > 0 }
        if (streets.isEmpty()) {
            output.println("Нет улиц с домами.")
            return
        }
        val street = chooseProperty(streets, "улицу для продажи дома") ?: return
        val amount = street.houseCost / 2
        PropertyService.sellHouse(player, street)
        builder.soldHouse(street.name, amount)
    }

    private fun mortgageMenu(player: Player, builder: MoveDescriptionBuilder) {
        val properties = DebtService.availableProperties(player)
        if (properties.isEmpty()) {
            output.println("Нет недвижимости, которую можно заложить.")
            return
        }
        val property = chooseProperty(properties, "недвижимость для залога") ?: return
        try {
            PropertyService.mortgage(player, property)
            builder.mortgaged(property.name, property.mortgageValue)
        } catch (e: IllegalArgumentException) {
            output.println(e.message ?: "Невозможно заложить недвижимость.")
        }
    }

    private fun unmortgageMenu(player: Player, builder: MoveDescriptionBuilder) {
        val properties = player.properties.filter { it.mortgaged }
        if (properties.isEmpty()) {
            output.println("Нет заложенной недвижимости.")
            return
        }
        val property = chooseProperty(properties, "недвижимость для снятия залога") ?: return
        try {
            PropertyService.unmortgage(player, property)
            builder.unmortgaged(property.name, property.unmortgageCost)
        } catch (e: IllegalArgumentException) {
            output.println(e.message ?: "Невозможно снять залог.")
        }
    }

    private fun <T : Property> chooseProperty(properties: List<T>, action: String): T? {
        output.println("Выберите $action:")
        properties.forEachIndexed { index, property -> output.println("${index + 1} — ${property.name}") }
        val number = input.readLine().toIntOrNull()
        val property = number?.let { properties.getOrNull(it - 1) }
        if (property == null) output.println("Некорректный номер.")
        return property
    }

    private fun startAuction(game: GameSession, properties: List<Property>) {
        auctionService.auction(
            properties = properties,
            players = game.players,
            readLine = { prompt ->
                output.println(prompt)
                input.readLine()
            },
            onMessage = output::println,
        )
    }

    private fun bankruptToBank(game: GameSession, player: Player, builder: MoveDescriptionBuilder) {
        val properties = player.properties.toList()
        val lots = bankruptcyService.bankruptToBank(player, properties)
        if (lots.isNotEmpty()) startAuction(game, lots)
        builder.bankruptcy(player.name)
    }

    private fun saveMove(game: GameSession, move: Move) {
        game.moveHistory.add(move)
        moveHistoryRepository.save(game.moveHistory)
        gameRepository.save(game)
        if (game.winner() != null) game.finish()
    }

    private fun finishGame(game: GameSession) {
        val winner = game.winner() ?: return
        output.println("Победил игрок ${winner.name}")
        game.players.forEach { player ->
            val statistics = statisticsRepository.findByPlayerId(player.id)
                ?: PlayerStatistics(player.id, player.name)
            statistics.gamesPlayed++
            if (player.id == winner.id) statistics.wins++
            statisticsRepository.save(statistics)
        }
        gameHistoryRepository.save(
            monopoly.domain.model.GameSummary(
                gameId = game.id,
                playerNames = game.players.map { it.name },
                moveCount = game.moveHistory.moveCount,
                winnerName = winner.name,
            ),
        )
        moveHistoryRepository.save(game.moveHistory)
        gameRepository.delete(game.id)
    }

    private fun continueGame() {
        val games = gameRepository.findAll()
        if (games.isEmpty()) {
            output.println("Незавершённых игр нет.")
            return
        }
        output.println("Незавершённые игры:")
        games.forEach { game ->
            output.println("${game.id} — ${game.players.joinToString(", ") { it.name }}")
        }
        output.println("Введите ID игры или 0 для возврата:")
        val id = input.readLine().trim()
        if (id == "0") return
        val game = gameRepository.findById(id)
        if (game == null) {
            output.println("Игра не найдена.")
            return
        }
        output.println("Игра продолжена. Ход игрока ${game.currentPlayer.name}")
        gameLoop(game)
    }

    private fun showStatistics() {
        val statistics = statisticsRepository.findAll()
        if (statistics.isEmpty()) {
            output.println("Статистика отсутствует.")
            return
        }
        statistics.forEach { output.println("${it.playerName}: игр — ${it.gamesPlayed}, побед — ${it.wins}") }
    }

    private fun showGameHistory() {
        val history = gameHistoryRepository.findAll()
        if (history.isEmpty()) {
            output.println("История игр отсутствует.")
            return
        }
        history.forEach {
            output.println(
                "Игра ${it.gameId}: игроки — ${it.playerNames.joinToString(", ")}, " +
                    "ходов — ${it.moveCount}, победитель — ${it.winnerName}",
            )
        }
    }

    private fun showMoveHistory() {
        output.println("Введите ID игры:")
        val id = input.readLine().trim()
        val history: MoveHistory? = moveHistoryRepository.findByGameId(id)
        if (history == null) {
            output.println("История ходов не найдена.")
            return
        }
        output.println("История игры ${history.gameId}. Ходов: ${history.moveCount}")
        history.moves.forEach { move ->
            output.println("Ход №${move.number}, игрок ${move.playerName}")
            move.descriptions.forEach { description -> output.println("  $description") }
        }
    }
}
