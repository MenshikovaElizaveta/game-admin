package monopoly.infrastructure.file

import monopoly.domain.model.*
import monopoly.domain.model.card.CardFactory
import monopoly.domain.model.card.Deck
import monopoly.domain.model.cell.CellFactory
import monopoly.domain.model.property.*
import monopoly.domain.repository.GameRepository
import java.io.File
import java.util.Properties

class FileGameRepository(
    private val directory: File = File("data/games"),
) : GameRepository {
    private val sequenceFile = File(directory, "next-game-id.txt")

    @Synchronized
    override fun nextId(): Int {
        directory.mkdirs()

        val saved = sequenceFile
            .takeIf { it.exists() }
            ?.readText()
            ?.trim()
            ?.toIntOrNull()

        val minimumNext = (
                directory.listFiles { file ->
                    file.extension == "properties"
                }?.mapNotNull {
                    it.nameWithoutExtension.toIntOrNull()
                }?.maxOrNull() ?: 0
                ) + 1

        val next = maxOf(saved ?: 1, minimumNext)

        sequenceFile.parentFile?.mkdirs()
        sequenceFile.writeText((next + 1).toString())

        return next
    }

    override fun save(game: GameSession) {
        directory.mkdirs()
        val properties = Properties()

        properties.setProperty("id", game.id.toString())
        properties.setProperty("active", game.active.toString())
        properties.setProperty(
            "current",
            game.players.indexOf(game.currentPlayer).toString(),
        )
        properties.setProperty(
            "turnInProgress",
            game.turnInProgress.toString(),
        )
        properties.setProperty("players.count", game.players.size.toString())

        game.players.forEachIndexed { i, player ->
            val key = "player.$i."

            properties.setProperty(key + "id", player.id)
            properties.setProperty(key + "name", player.name)
            properties.setProperty(key + "balance", player.balance.toString())
            properties.setProperty(key + "position", player.position.toString())
            properties.setProperty(key + "jail", player.inJail.toString())
            properties.setProperty(
                key + "jailTurns",
                player.jailTurns.toString(),
            )
            properties.setProperty(
                key + "freeParking",
                player.atFreeParking.toString(),
            )
            properties.setProperty(key + "bankrupt", player.bankrupt.toString())
        }

        game.board.cells.forEach { cell ->
            cell.property?.let { property ->
                val key = "property.${cell.number}."
                val ownerIndex = game.players.indexOfFirst {
                    it === property.owner
                }

                properties.setProperty(key + "owner", ownerIndex.toString())
                properties.setProperty(
                    key + "mortgaged",
                    property.mortgaged.toString(),
                )

                if (property is Street) {
                    properties.setProperty(
                        key + "houses",
                        property.houses.toString(),
                    )
                }
            }
        }

        saveDeck(properties, "chance", game.chanceDeck)
        saveDeck(properties, "chest", game.communityChestDeck)

        properties.setProperty(
            "moves.count",
            game.moveHistory.moves.size.toString(),
        )

        game.moveHistory.moves.forEachIndexed { i, move ->
            properties.setProperty(
                "move.$i.number",
                move.number.toString(),
            )
            properties.setProperty("move.$i.player", move.playerName)
            properties.setProperty(
                "move.$i.descriptions",
                move.descriptions.joinToString("\n"),
            )
        }

        File(directory, "${game.id}.properties").outputStream().use {
            properties.store(it, "Сохранённая партия")
        }
    }

    private fun saveDeck(
        properties: Properties,
        key: String,
        deck: Deck,
    ) {
        properties.setProperty(
            "$key.order",
            deck.cards.joinToString(",") { it.number.toString() },
        )
        properties.setProperty(
            "$key.index",
            deck.currentIndex.toString(),
        )
    }

    override fun findById(id: Int): GameSession? {
        val file = File(directory, "$id.properties")
        if (!file.exists()) return null

        val properties = Properties()
        file.inputStream().use(properties::load)

        val players = (0 until
                properties.getProperty("players.count", "0").toInt()
                ).map { i ->
                val key = "player.$i."

                Player(
                    properties.getProperty(key + "id"),
                    properties.getProperty(key + "name"),
                ).also { player ->
                    player.balance = properties
                        .getProperty(key + "balance")
                        .toInt()
                    player.position = properties
                        .getProperty(key + "position")
                        .toInt()
                    player.inJail = properties
                        .getProperty(key + "jail", "false")
                        .toBoolean()
                    player.jailTurns = properties
                        .getProperty(key + "jailTurns", "0")
                        .toInt()
                    player.atFreeParking = properties
                        .getProperty(key + "freeParking", "false")
                        .toBoolean()
                    player.bankrupt = properties
                        .getProperty(key + "bankrupt", "false")
                        .toBoolean()
                }
            }

        val board = CellFactory.createBoard()
        val game = GameSession(
            id,
            players,
            board,
            CardFactory.createChanceDeck(),
            CardFactory.createCommunityChestDeck(),
        )

        board.cells.forEach { cell ->
            cell.property?.let { property ->
                val key = "property.${cell.number}."
                val ownerIndex = properties
                    .getProperty(key + "owner", "-1")
                    .toInt()

                if (ownerIndex in players.indices) {
                    players[ownerIndex].addProperty(property)
                }

                property.mortgaged = properties
                    .getProperty(key + "mortgaged", "false")
                    .toBoolean()

                if (property is Street) {
                    val houses = properties
                        .getProperty(key + "houses", "0")
                        .toInt()
                        .coerceIn(0, 5)

                    repeat(houses) {
                        property.buildHouse()
                    }
                }
            }
        }

        restoreDeck(properties, "chance", game.chanceDeck)
        restoreDeck(properties, "chest", game.communityChestDeck)

        repeat(properties.getProperty("moves.count", "0").toInt()) { i ->
            val descriptions = properties
                .getProperty("move.$i.descriptions", "")
                .split("\n")
                .filter { it.isNotBlank() }

            game.moveHistory.add(
                Move(
                    properties.getProperty("move.$i.number").toInt(),
                    properties.getProperty("move.$i.player"),
                    descriptions,
                ),
            )
        }

        val current = properties
            .getProperty("current", "0")
            .toInt()
            .coerceIn(players.indices)

        game.restoreCurrentPlayer(current)
        game.turnInProgress = properties
            .getProperty("turnInProgress", "false")
            .toBoolean()

        if (!properties.getProperty("active", "true").toBoolean()) {
            game.finish()
        }

        return game
    }

    private fun restoreDeck(
        properties: Properties,
        key: String,
        deck: Deck,
    ) {
        val order = properties
            .getProperty("$key.order")
            ?.split(",")
            ?.mapNotNull(String::toIntOrNull)

        if (order?.size == 10) {
            deck.reorder(order)
        }

        val index = properties
            .getProperty("$key.index", "0")
            .toInt()
            .coerceIn(0, 9)

        repeat(index) {
            deck.draw()
        }
    }

    override fun findAll(): List<GameSession> {
        if (!directory.exists()) return emptyList()

        return directory
            .listFiles { file -> file.extension == "properties" }
            ?.mapNotNull { file ->
                file.nameWithoutExtension.toIntOrNull()?.let(::findById)
            }
            ?.sortedBy { it.id }
            ?: emptyList()
    }

    override fun delete(id: Int) {
        File(directory, "$id.properties").delete()
    }
}
