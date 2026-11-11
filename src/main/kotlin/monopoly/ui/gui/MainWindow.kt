package monopoly.ui.gui

import monopoly.domain.model.*
import monopoly.domain.model.card.*
import monopoly.domain.model.cell.*
import monopoly.domain.model.property.*
import monopoly.domain.repository.*
import monopoly.domain.service.*
import monopoly.infrastructure.file.*
import java.awt.*
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableModel

class MainWindow : JFrame("Monopoly") {
    private val cream = Color(250, 246, 232)
    private val darkGreen = Color(27, 78, 55)
    private val gold = Color(196, 155, 61)
    private val darkText = Color(35, 39, 36)
    private val screens = JPanel(CardLayout())
    private val games: GameRepository = FileGameRepository()
    private val statistics: PlayerStatisticsRepository = FilePlayerStatisticsRepository()
    private val history: GameHistoryRepository = FileGameHistoryRepository()
    private val moveHistory: MoveHistoryRepository = FileMoveHistoryRepository()
    private val bankruptcyService = BankruptcyService()
    private var game: GameSession? = null
    private var gameFinished = false
    private val pendingMoveDescriptions = mutableMapOf<String, MutableList<String>>()
    private val titleFont = Font("SansSerif", Font.BOLD, 20)

    init {
        UIManager.put("Panel.background", cream)
        UIManager.put("Button.background", darkGreen)
        UIManager.put("Button.foreground", Color.WHITE)
        UIManager.put("OptionPane.background", cream)
        UIManager.put("OptionPane.messageForeground", darkText)
        UIManager.put("Table.background", Color(255, 253, 247))
        UIManager.put("Table.foreground", darkText)
        UIManager.put("Table.selectionBackground", darkGreen)
        UIManager.put("Table.selectionForeground", Color.WHITE)
        defaultCloseOperation = EXIT_ON_CLOSE
        addWindowListener(object : java.awt.event.WindowAdapter() {
            override fun windowClosing(event: java.awt.event.WindowEvent) = saveCurrent()
        })
        minimumSize = Dimension(900, 620)
        size = Dimension(1000, 700)
        setLocationRelativeTo(null)
        screens.background = cream
        contentPane = screens
        screens.add(mainMenu(), "main")
        showScreen("main")
    }

    private fun showScreen(name: String) = (screens.layout as CardLayout).show(screens, name)

    private fun page(title: String): JPanel = JPanel(BorderLayout(16, 16)).apply {
        background = cream
        border = EmptyBorder(24, 32, 24, 32)
        add(JLabel(title, SwingConstants.CENTER).apply {
            font = titleFont
            foreground = darkGreen
            border = BorderFactory.createMatteBorder(0, 0, 2, 0, gold)
        }, BorderLayout.NORTH)
    }

    private fun button(text: String, action: () -> Unit) = JButton(text).apply {
        font = Font("SansSerif", Font.PLAIN, 15)
        background = darkGreen
        foreground = Color.WHITE
        isFocusPainted = false
        addActionListener { action() }
    }

    private fun menuButton(text: String, action: () -> Unit) = button(text, action).apply {
        preferredSize = Dimension(380, 46)
    }

    private fun mainMenu(): JPanel = page("Администрирование партий").apply {
        val menu = JPanel(GridLayout(0, 1, 10, 10))
        menu.add(menuButton("Новая игра") { createGame() })
        menu.add(menuButton("Продолжить незавершённую игру") { continueGame() })
        menu.add(menuButton("Статистика игроков") { showStatistics() })
        menu.add(menuButton("Общая история игр") { showGameHistory() })
        menu.add(menuButton("История ходов конкретной игры") { chooseMoveHistory() })
        menu.add(menuButton("Выйти") { dispose() })
        add(menu, BorderLayout.CENTER)
    }

    private fun createGame() {
        val count = JOptionPane.showInputDialog(
            this, "Выберите количество игроков", "Создание игры", JOptionPane.QUESTION_MESSAGE,
            null, (2..8).toList().toTypedArray(), 2,
        ) as? Int ?: return
        val players = mutableListOf<Player>()
        repeat(count) { index ->
            while (true) {
                val enteredName = JOptionPane.showInputDialog(this, "Введите имя ${index + 1}-го игрока (от 1 до 15 символов):") ?: return
                val name = enteredName.trim()
                if (name.length !in 1..15) {
                    message("Имя должно содержать от 1 до 15 символов.")
                    continue
                }
                if (players.any { it.name.equals(name, true) }) {
                    message("Имена игроков в одной партии должны различаться.")
                    continue
                }
                val id = statistics.findByName(name)?.playerId ?: java.util.UUID.randomUUID().toString()
                players += Player(id, name)
                break
            }
        }
        val created = GameSession(
            games.nextId(), players, CellFactory.createBoard(),
            CardFactory.createChanceDeck(), CardFactory.createCommunityChestDeck(),
        )
        showCardSetup(created)
    }

    private fun showCardSetup(created: GameSession) {
        val page = page("Настройка колод • игра ${created.id}")
        val body = JPanel(GridLayout(1, 2, 16, 0))
        val chance = reorderPanel("Шанс", created.chanceDeck)
        val chest = reorderPanel("Общественная казна", created.communityChestDeck)
        body.add(chance)
        body.add(chest)
        page.add(body, BorderLayout.CENTER)
        val actions = JPanel(FlowLayout(FlowLayout.CENTER, 12, 0))
        actions.add(button("Сохранить порядок и начать") {
            applyOrder(chance)
            applyOrder(chest)
            game = created
            gameFinished = false
            games.save(created)
            openGame()
        })
        actions.add(button("Оставить исходный порядок") {
            game = created
            gameFinished = false
            games.save(created)
            openGame()
        })
        actions.add(button("Отменить создание") { showScreen("main") })
        page.add(actions, BorderLayout.SOUTH)
        val screenName = "setup-${System.nanoTime()}"
        screens.add(page, screenName)
        showScreen(screenName)
    }

    private fun reorderPanel(title: String, deck: Deck): JPanel {
        val panel = JPanel(BorderLayout(6, 6))
        panel.border = BorderFactory.createTitledBorder(title)
        val model = DefaultListModel<String>()
        deck.cards.forEach { model.addElement("${it.number}. ${it.text}") }
        val list = JList(model).apply { selectionMode = ListSelectionModel.SINGLE_SELECTION }
        val controls = JPanel(GridLayout(2, 1, 4, 4))
        controls.add(button("Вверх") {
            val i = list.selectedIndex
            if (i > 0) {
                val value = model.remove(i)
                model.add(i - 1, value)
                list.selectedIndex = i - 1
            }
        })
        controls.add(button("Вниз") {
            val i = list.selectedIndex
            if (i >= 0 && i < model.size() - 1) {
                val value = model.remove(i)
                model.add(i + 1, value)
                list.selectedIndex = i + 1
            }
        })
        panel.add(JScrollPane(list), BorderLayout.CENTER)
        panel.add(controls, BorderLayout.EAST)
        panel.putClientProperty("deck", deck)
        panel.putClientProperty("model", model)
        return panel
    }

    private fun applyOrder(panel: JPanel) {
        val deck = panel.getClientProperty("deck") as Deck
        val model = panel.getClientProperty("model") as DefaultListModel<*>
        val order = (0 until model.size()).map { model.getElementAt(it).toString().substringBefore('.').toInt() }
        deck.reorder(order)
    }

    private fun continueGame() {
        val available = games.findAll().filter { it.active }
        if (available.isEmpty()) {
            message("Нет сохранённых незавершённых игр.")
            return
        }
        val labels = available.map { "${it.id} — ${it.players.joinToString(", ") { p -> p.name }}" }.toTypedArray()
        val selected = JOptionPane.showInputDialog(this, "Выберите игру", "Продолжить игру", JOptionPane.QUESTION_MESSAGE, null, labels, labels.first()) as? String ?: return
        val id = selected.substringBefore(" — ")
        game = games.findById(id)
        if (game == null) message("Не удалось восстановить игру.") else {
            gameFinished = false
            openGame()
        }
    }

    private fun openGame() {
        val current = game ?: return
        val page = page("Игра • ${current.id}")
        val split = JSplitPane(JSplitPane.HORIZONTAL_SPLIT).apply { resizeWeight = 0.36 }
        val left = JPanel(BorderLayout(8, 8)).apply { border = EmptyBorder(10, 0, 0, 0) }
        val details = JLabel().apply { font = Font("SansSerif", Font.PLAIN, 16) }
        left.add(details, BorderLayout.NORTH)
        val actions = JPanel(GridLayout(0, 1, 6, 6))
        left.add(JScrollPane(actions), BorderLayout.CENTER)
        val right = JPanel(BorderLayout(6, 6)).apply { background = cream }
        right.add(JLabel("Игроки", SwingConstants.CENTER).apply { background = cream; isOpaque = true }, BorderLayout.NORTH)
        val table = JTable().apply { autoCreateRowSorter = true }
        right.add(JScrollPane(table).apply {
            background = cream
            viewport.background = cream
        }, BorderLayout.CENTER)
        split.leftComponent = left
        split.rightComponent = right
        page.add(split, BorderLayout.CENTER)
        val footer = JPanel(FlowLayout(FlowLayout.CENTER, 10, 0))
        footer.add(button("История ходов") { showMoveHistory(current.id) })
        footer.add(button("В главное меню") { saveCurrent(); showScreen("main") })
        page.add(footer, BorderLayout.SOUTH)
        val screenName = "game-${System.nanoTime()}"
        screens.add(page, screenName)

        fun refresh() {
            val g = game ?: return
            if (!g.active) {
                showScreen("main")
                return
            }
            val player = g.currentPlayer
            details.text = "<html><b>Текущий игрок:</b> ${escape(player.name)}<br><br>" +
                "<b>Клетка:</b> ${escape(g.board.getCell(player.position).name)}<br><br>" +
                "<b>Баланс:</b> ${player.balance} М<br><br><b>Ход:</b> ${g.moveHistory.moveCount + 1}" +
                (if (player.inJail) "<br><br><b>В тюрьме:</b> ${player.jailTurns} ходов" else "") + "</html>"
            actions.removeAll()
            if (player.atFreeParking && !g.turnInProgress) {
                actions.add(JLabel("После бесплатной стоянки игрок пропускает ход."))
                actions.add(button("Пропустить ход") {
                    player.atFreeParking = false
                    recordMove(player, listOf("Игрок пропустил ход после бесплатной стоянки."))
                    advanceOrFinish()
                    refresh()
                })
            } else if (player.inJail && !g.turnInProgress) {
                actions.add(JLabel("Игрок в тюрьме. Пропущено ходов: ${player.jailTurns}"))
                actions.add(button("Бросить кубики") { jailTurn(); refresh() })
                if (player.balance >= GameRules.JAIL_FINE) actions.add(button("Заплатить штраф 50 М") { player.balance -= GameRules.JAIL_FINE; player.inJail = false; player.jailTurns = 0; saveCurrent(); refresh() })
                actions.add(button("Продать дом / заложить недвижимость") { propertyMenu(player); saveCurrent(); refresh() })
                actions.add(button("Объявить себя банкротом") { declareBankruptcy(player, null); refresh() })
            } else if (!g.turnInProgress) {
                actions.add(button("Бросить кубики и сделать ход") {
                    val dice = DiceDialog.show(this@MainWindow) ?: return@button
                    takeTurn(dice.first, dice.second)
                    refresh()
                })
                actions.add(button("Действия с недвижимостью") { propertyMenu(player); saveCurrent(); refresh() })
                actions.add(button("Объявить себя банкротом") {
                    if (confirm("Объявить ${player.name} банкротом?")) { declareBankruptcy(player, null); refresh() }
                })
            } else {
                actions.add(JLabel("Основное действие хода выполнено. Можно управлять недвижимостью."))
                actions.add(button("Действия с недвижимостью") { propertyMenu(player); saveCurrent(); refresh() })
                actions.add(button("Завершить ход") { advanceOrFinish(); refresh() })
            }
            actions.add(button("В главное меню") { saveCurrent(); showScreen("main") })
            val rows = g.players.filter { !it.bankrupt && it !== player }.map { p ->
                arrayOf<Any>(p.name, g.board.getCell(p.position).name, p.position, p.balance, if (p.inJail) "Тюрьма" else "В игре")
            }.toTypedArray()
            table.model = object : DefaultTableModel(rows, arrayOf("Имя", "Клетка", "№", "Баланс", "Статус")) {
                override fun isCellEditable(row: Int, column: Int) = false
            }
            actions.revalidate()
            actions.repaint()
            table.revalidate()
        }
        refresh()
        showScreen(screenName)
    }

    private fun jailTurn() {
        val g = game ?: return
        val player = g.currentPlayer
        val dice = DiceDialog.show(this) ?: return
        val descriptions = mutableListOf("Бросок кубиков: ${dice.first} + ${dice.second} = ${dice.first + dice.second}")
        if (dice.first == dice.second) {
            player.inJail = false
            player.jailTurns = 0
            descriptions += "Выпал дубль. Игрок вышел из тюрьмы."
            val passed = MovementService.move(player, dice.first + dice.second)
            if (passed) descriptions += "Игрок прошёл старт и получил ${GameRules.PASS_START_REWARD} М."
            resolveLanding(player, dice.first + dice.second, descriptions)
        } else {
            player.jailTurns++
            descriptions += "Игрок не выбросил дубль."
            if (player.jailTurns >= 3) {
                val options = arrayOf("Заплатить штраф 50 М", "Продать дом / заложить недвижимость", "Объявить себя банкротом")
                val choice = JOptionPane.showInputDialog(
                    this, "Три хода в тюрьме прошли без дубля. Нужно заплатить штраф или объявить себя банкротом.",
                    "Тюрьма", JOptionPane.WARNING_MESSAGE, null, options, options.first(),
                ) as? String ?: options[0]
                when (choice) {
                    options[0] -> handleBankDebt(player, GameRules.JAIL_FINE, "штраф за выход из тюрьмы", descriptions)
                    options[1] -> {
                        propertyMenu(player)
                        handleBankDebt(player, GameRules.JAIL_FINE, "штраф за выход из тюрьмы", descriptions)
                    }
                    options[2] -> declareBankruptcy(player, null, false, descriptions)
                }
                if (!player.bankrupt && player.balance >= 0 && player.inJail) {
                    // handleBankDebt either paid the fine or declared bankruptcy.
                    if (descriptions.any { it.contains("штраф за выход из тюрьмы") }) {
                        player.inJail = false
                        player.jailTurns = 0
                        val passed = MovementService.move(player, dice.first + dice.second)
                        if (passed) descriptions += "Игрок прошёл старт и получил ${GameRules.PASS_START_REWARD} М."
                        resolveLanding(player, dice.first + dice.second, descriptions)
                    }
                }
            }
        }
        recordMove(player, descriptions)
        advanceOrFinish()
    }

    private fun takeTurn(first: Int, second: Int) {
        val g = game ?: return
        val player = g.currentPlayer
        if (player.bankrupt || player.inJail) return
        val descriptions = mutableListOf("Бросок кубиков: $first + $second = ${first + second}")
        val passed = MovementService.move(player, first + second)
        if (passed) descriptions += "Игрок прошёл старт и получил ${GameRules.PASS_START_REWARD} М."
        resolveLanding(player, first + second, descriptions)
        g.turnInProgress = true
        recordMove(player, descriptions)
        if (player.bankrupt) advanceOrFinish()
    }

    private fun resolveLanding(player: Player, diceSum: Int, descriptions: MutableList<String>, depth: Int = 0) {
        val g = game ?: return
        if (depth > 4 || player.bankrupt) return
        val cell = g.board.getCell(player.position)
        descriptions += "Игрок оказался на клетке №${cell.number} «${cell.name}»."
        when (cell.type) {
            CellType.TAX -> {
                val amount = if (cell.number == 38) GameRules.SUPER_TAX else GameRules.TAX
                handleBankDebt(player, amount, "налог", descriptions)
            }
            CellType.GO_TO_JAIL -> {
                player.position = 10; player.inJail = true; player.jailTurns = 0
                descriptions += "Игрок отправлен в тюрьму."
            }
            CellType.CHANCE, CellType.COMMUNITY_CHEST -> {
                val deck = if (cell.type == CellType.CHANCE) g.chanceDeck else g.communityChestDeck
                val result = CardService().draw(player, deck)
                message("Карточка: ${result.card.text}")
                descriptions += "Вытянута карточка: ${result.card.text}"
                when (val card = result.card) {
                    is IncomeCard -> descriptions += "Получено ${card.amount} М."
                    is DeductionCard -> handleBankDebt(player, card.amount, "эффект карточки «${card.text}»", descriptions)
                    is SpecificMovementCard, is NearestMovementCard -> {
                        if (result.passedStart) descriptions += "Игрок прошёл старт и получил ${GameRules.PASS_START_REWARD} М."
                        if (result.movedToJail) descriptions += "Игрок отправлен в тюрьму."
                        if (!result.movedToJail) resolveLanding(player, diceSum, descriptions, depth + 1)
                    }
                }
            }
            CellType.STREET, CellType.RAILROAD, CellType.UTILITY -> {
                val property = cell.property ?: return
                val owner = property.owner
                if (owner == null) {
                    if (player.balance >= property.price) {
                        val answer = JOptionPane.showConfirmDialog(this, "Купить «${property.name}» за ${property.price} М?", "Недвижимость", JOptionPane.YES_NO_OPTION)
                        if (answer == JOptionPane.YES_OPTION) {
                            PropertyService.buy(player, property)
                            descriptions += "Куплена недвижимость «${property.name}» за ${property.price} М."
                        } else runAuction(property, descriptions)
                    } else {
                        descriptions += "Недостаточно денег для покупки «${property.name}»."
                        runAuction(property, descriptions)
                    }
                } else if (owner !== player && !property.mortgaged && !owner.bankrupt) {
                    val rent = when (property) {
                        is Street -> property.calculateRent()
                        is Railroad -> property.calculateRent()
                        is Utility -> property.calculateRent(diceSum)
                        else -> 0
                    }
                    if (rent > 0) handlePlayerDebt(player, owner, rent, descriptions)
                    descriptions += "Аренда за «${property.name}»: $rent М."
                }
            }
            CellType.FREE_PARKING -> { player.atFreeParking = true; descriptions += "Бесплатная стоянка." }
            else -> Unit
        }
    }

    private fun runAuction(property: Property, descriptions: MutableList<String>) {
        val g = game ?: return
        if (property.owner != null) return
        if (!confirm("Выставить «${property.name}» на аукцион?")) {
            descriptions += "Аукцион недвижимости «${property.name}» пропущен."
            return
        }

        val eligiblePlayers = g.players.filter { !it.bankrupt && it.balance > 0 }
        if (eligiblePlayers.isEmpty()) {
            descriptions += "Лот «${property.name}» остался без владельца: нет участников аукциона."
            return
        }

        val labels = eligiblePlayers.map { "${it.name} — баланс ${it.balance} М" }.toTypedArray()
        val selectedLabel = JOptionPane.showInputDialog(
            this,
            "Выберите игрока, который покупает лот «${property.name}».",
            "Аукцион",
            JOptionPane.QUESTION_MESSAGE,
            null,
            labels,
            labels.first(),
        ) as? String
        val bidder = eligiblePlayers.firstOrNull { selectedLabel == "${it.name} — баланс ${it.balance} М" }
        if (bidder == null) {
            descriptions += "Лот «${property.name}» остался без владельца после аукциона."
            return
        }

        var bid: Int
        while (true) {
            val entered = JOptionPane.showInputDialog(
                this,
                "Введите ставку для ${bidder.name} от 1 до ${bidder.balance} М.",
                "Ставка на аукционе",
                JOptionPane.QUESTION_MESSAGE,
            ) ?: run {
                descriptions += "Лот «${property.name}» остался без владельца после аукциона."
                return
            }
            val value = entered.trim().toIntOrNull()
            if (value != null && value in 1..bidder.balance) {
                bid = value
                break
            }
            message("Введите целое число от 1 до ${bidder.balance} М.")
        }

        bidder.balance -= bid
        bidder.addProperty(property)
        descriptions += "Результат аукциона: ${bidder.name} купил(а) «${property.name}» за $bid М."
        saveCurrent()
    }

    private fun handlePlayerDebt(debtor: Player, creditor: Player, amount: Int, descriptions: MutableList<String>) {
        while (debtor.balance < amount && !debtor.bankrupt) {
            val options = mutableListOf("Продать дом", "Заложить недвижимость", "Объявить себя банкротом")
            val selected = JOptionPane.showInputDialog(this, "Долг игроку ${creditor.name}: $amount М. Баланс: ${debtor.balance} М", "Долг", JOptionPane.WARNING_MESSAGE, null, options.toTypedArray(), options.first()) as? String ?: options[2]
            when (selected) {
                options[0] -> selectAndSellHouse(debtor)
                options[1] -> selectAndMortgage(debtor)
                options[2] -> { declareBankruptcy(debtor, creditor, false, descriptions); return }
            }
        }
        if (!debtor.bankrupt && debtor.balance >= amount) {
            DebtService.payToPlayer(debtor, creditor, amount)
            descriptions += "${debtor.name} заплатил ${creditor.name} $amount М."
        }
    }

    private fun handleBankDebt(player: Player, amount: Int, reason: String, descriptions: MutableList<String>) {
        while (player.balance < amount && !player.bankrupt) {
            val options = mutableListOf("Продать дом", "Заложить недвижимость", "Объявить себя банкротом")
            val selected = JOptionPane.showInputDialog(this, "Долг банку: $amount М ($reason). Баланс: ${player.balance} М", "Долг банку", JOptionPane.WARNING_MESSAGE, null, options.toTypedArray(), options.first()) as? String ?: options[2]
            when (selected) {
                options[0] -> selectAndSellHouse(player)
                options[1] -> selectAndMortgage(player)
                options[2] -> { declareBankruptcy(player, null, false, descriptions); return }
            }
        }
        if (!player.bankrupt && player.balance >= amount) {
            player.balance -= amount
            descriptions += "Уплачено $amount М: $reason."
        }
    }

    private fun selectAndSellHouse(player: Player) {
        val streets = player.streets().filter { it.houses > 0 }
        if (streets.isEmpty()) { message("Нет улиц с домами."); return }
        chooseProperty(streets.map { "${it.name} — ${it.houseCost / 2} М за дом (домов: ${it.houses})" to it }) {
            PropertyService.sellHouse(player, it as Street)
        }
    }

    private fun selectAndMortgage(player: Player) {
        val properties = DebtService.availableProperties(player)
        if (properties.isEmpty()) { message("Нет недвижимости, которую можно заложить."); return }
        chooseProperty(properties.map { "${it.name} — ${it.mortgageValue} М" to it }) {
            PropertyService.mortgage(player, it)
            appendToCurrentMove(player, "Заложена недвижимость «${it.name}», получено ${it.mortgageValue} М.")
        }
    }

    private fun declareBankruptcy(
        player: Player,
        creditor: Player?,
        recordAndAdvance: Boolean = true,
        moveDescriptions: MutableList<String>? = null,
    ) {
        if (game == null || player.bankrupt) return
        val descriptions = moveDescriptions ?: mutableListOf()
        descriptions += "Игрок ${player.name} объявил себя банкротом${if (creditor == null) " перед банком" else "; имущество передано ${creditor.name}"}."
        if (creditor == null) {
            val lots = bankruptcyService.bankruptToBank(player, player.properties.toList())
            lots.forEach { property -> runAuction(property, descriptions) }
        } else bankruptcyService.bankruptToPlayer(player, creditor)
        if (recordAndAdvance) {
            recordMove(player, descriptions)
            advanceOrFinish()
        }
    }

    private fun propertyMenu(player: Player) {
        val options = arrayOf("Построить дом", "Продать дом", "Заложить недвижимость", "Снять залог", "Список моей недвижимости")
        val choice = JOptionPane.showOptionDialog(this, "Баланс: ${player.balance} М", "Действия с недвижимостью", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0])
        val selected = options.getOrNull(choice) ?: return
        try {
            when (selected) {
                options[0] -> chooseProperty(player.streets().filter { GameRules.canBuild(player, it) }.map { "${it.name} — дом за ${it.houseCost} М (${it.houses}/5)" to it }) { PropertyService.buildHouse(player, it as Street) }
                options[1] -> selectAndSellHouse(player)
                options[2] -> selectAndMortgage(player)
                options[3] -> chooseProperty(player.properties.filter { it.mortgaged && player.balance >= it.unmortgageCost }.map { "${it.name} — снять залог за ${it.unmortgageCost} М" to it }) {
                    PropertyService.unmortgage(player, it)
                    appendToCurrentMove(player, "Снят залог с «${it.name}», уплачено ${it.unmortgageCost} М.")
                }
                options[4] -> message(if (player.properties.isEmpty()) "У игрока нет недвижимости." else player.properties.joinToString("\n") { p -> "${p.name}${if (p.mortgaged) " (заложена)" else ""}${if (p is Street) ", домов: ${p.houses}" else ""}" })
            }
        } catch (e: IllegalArgumentException) {
            message(e.message ?: "Это действие невозможно.")
        }
    }

    private fun chooseProperty(options: List<Pair<String, Property>>, action: (Property) -> Unit) {
        if (options.isEmpty()) { message("Нет недвижимости, подходящей для этого действия."); return }
        val labels = options.map { it.first }.toTypedArray()
        val selected = JOptionPane.showInputDialog(this, "Выберите недвижимость", "Недвижимость", JOptionPane.PLAIN_MESSAGE, null, labels, labels.first()) as? String ?: return
        val property = options.first { it.first == selected }.second
        try { action(property); saveCurrent() } catch (e: IllegalArgumentException) { message(e.message ?: "Действие невозможно") }
    }

    private fun recordMove(player: Player, descriptions: List<String>) {
        val g = game ?: return
        val pending = pendingMoveDescriptions.remove(player.name).orEmpty()
        g.moveHistory.add(Move(g.moveHistory.moveCount + 1, player.name, descriptions + pending))
        saveCurrent()
    }

    private fun appendToCurrentMove(player: Player, description: String) {
        val g = game ?: return
        val lastMove = g.moveHistory.moves.lastOrNull()
        if (g.turnInProgress && lastMove != null && lastMove.playerName == player.name) {
            g.moveHistory.moves[g.moveHistory.moves.lastIndex] = Move(
                lastMove.number,
                lastMove.playerName,
                lastMove.descriptions + description,
            )
            saveCurrent()
        } else {
            pendingMoveDescriptions.getOrPut(player.name) { mutableListOf() }.add(description)
            saveCurrent()
        }
    }

    private fun advanceOrFinish() {
        val g = game ?: return
        g.turnInProgress = false
        val winner = g.winner()
        if (winner != null) {
            if (!gameFinished) {
                gameFinished = true
                g.finish()
                history.save(GameSummary(g.id, g.players.map { it.name }, g.moveHistory.moveCount, winner.name))
                g.players.forEach { player ->
                    val stat = statistics.findByPlayerId(player.id) ?: PlayerStatistics(player.id, player.name)
                    stat.gamesPlayed++
                    if (player.id == winner.id) stat.wins++
                    statistics.save(stat)
                }
                moveHistory.save(g.moveHistory)
                games.delete(g.id)
                message("Партия завершена. Победитель: ${winner.name}.")
            }
            showScreen("main")
            return
        }
        g.moveToNextActivePlayer()
        saveCurrent()
    }

    private fun saveCurrent() {
        game?.let {
            games.save(it)
            moveHistory.save(it.moveHistory)
        }
    }

    private fun showStatistics() {
        val rows = statistics.findAll().map {
            arrayOf<Any>(it.playerName, it.gamesPlayed, it.wins)
        }.toTypedArray()
        showTable("Статистика игроков", arrayOf("Имя", "Количество игр", "Количество побед"), rows)
    }

    private fun showGameHistory() {
        val rows = history.findAll().map { arrayOf<Any>(it.gameId, it.playerNames.joinToString(", "), it.moveCount, it.winnerName) }.toTypedArray()
        showTable("История всех игр", arrayOf("№ игры", "Игроки", "Ходов", "Победитель"), rows)
    }

    private fun showTable(title: String, columns: Array<String>, rows: Array<Array<Any>>) {
        val page = page(title)
        page.add(JScrollPane(JTable(rows, columns).apply { autoCreateRowSorter = true }), BorderLayout.CENTER)
        page.add(button("Назад") { showScreen("main") }, BorderLayout.SOUTH)
        val screenName = "table-${System.nanoTime()}"
        screens.add(page, screenName)
        showScreen(screenName)
    }

    private fun chooseMoveHistory() {
        val allIds = (history.findAll().map { it.gameId } + games.findAll().map { it.id }).distinct()
        val id = if (allIds.isNotEmpty()) {
            JOptionPane.showInputDialog(this, "Выберите номер игры", "История ходов", JOptionPane.QUESTION_MESSAGE, null, allIds.toTypedArray(), allIds.first()) as? String
        } else JOptionPane.showInputDialog(this, "Введите номер игры")
        if (!id.isNullOrBlank()) showMoveHistory(id)
    }

    private fun showMoveHistory(id: String) {
        val h = moveHistory.findByGameId(id) ?: game?.takeIf { it.id == id }?.moveHistory
        if (h == null || h.moves.isEmpty()) { message("История ходов не найдена."); return }
        var index = 0
        val page = page("История ходов игры $id")
        val text = JTextArea().apply { isEditable = false; lineWrap = true; wrapStyleWord = true; font = Font("SansSerif", Font.PLAIN, 15) }
        fun refresh() {
            val move = h.moves[index]
            text.text = "Ход ${move.number} • ${move.playerName}\n\n${move.descriptions.joinToString("\n")}"
        }
        page.add(JScrollPane(text), BorderLayout.CENTER)
        val controls = JPanel(FlowLayout())
        controls.add(button("Предыдущий ход") { if (index > 0) index--; refresh() })
        controls.add(button("Назад") {
            if (game?.id == id && game?.active == true) openGame() else showScreen("main")
        })
        controls.add(button("Следующий ход") { if (index < h.moves.lastIndex) index++; refresh() })
        page.add(controls, BorderLayout.SOUTH)
        val screenName = "moves-${System.nanoTime()}"
        screens.add(page, screenName)
        refresh()
        showScreen(screenName)
    }

    private fun confirm(text: String): Boolean = JOptionPane.showConfirmDialog(this, text, "Monopoly", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION
    private fun message(text: String) = JOptionPane.showMessageDialog(this, text, "Monopoly", JOptionPane.INFORMATION_MESSAGE)
    private fun escape(text: String) = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}

private object DiceDialog {
    fun show(parent: Component): Pair<Int, Int>? {
        val panel = JPanel(GridLayout(0, 1, 6, 6))
        val first = JSpinner(SpinnerNumberModel(1, 1, 6, 1))
        val second = JSpinner(SpinnerNumberModel(1, 1, 6, 1))
        panel.add(JLabel("Первый кубик")); panel.add(first)
        panel.add(JLabel("Второй кубик")); panel.add(second)
        val result = JOptionPane.showConfirmDialog(parent, panel, "Бросок кубиков", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        return if (result == JOptionPane.OK_OPTION) (first.value as Int) to (second.value as Int) else null
    }
}
