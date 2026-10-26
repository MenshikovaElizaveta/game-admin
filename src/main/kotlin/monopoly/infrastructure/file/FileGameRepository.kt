package monopoly.infrastructure.file

import monopoly.domain.model.*
import monopoly.domain.model.card.CardFactory
import monopoly.domain.model.card.Deck
import monopoly.domain.model.cell.CellFactory
import monopoly.domain.model.property.*
import monopoly.domain.repository.GameRepository
import java.io.File
import java.util.Properties

class FileGameRepository(private val directory: File = File("data/games")) : GameRepository {
    private val sequenceFile = File(directory, "next-game-id.txt")

    @Synchronized
    override fun nextId(): String {
        directory.mkdirs()
        val saved = sequenceFile.takeIf { it.exists() }?.readText()?.trim()?.toIntOrNull()
        val minimumNext = (directory.listFiles { file -> file.extension == "properties" }
            ?.mapNotNull { it.nameWithoutExtension.toIntOrNull() }?.maxOrNull() ?: 0) + 1
        val next = maxOf(saved ?: 1, minimumNext)
        sequenceFile.parentFile?.mkdirs()
        sequenceFile.writeText((next + 1).toString())
        return next.toString()
    }
    override fun save(game: GameSession) {
        directory.mkdirs(); val p = Properties()
        p.setProperty("id", game.id); p.setProperty("active", game.active.toString()); p.setProperty("current", game.players.indexOf(game.currentPlayer).toString()); p.setProperty("turnInProgress", game.turnInProgress.toString())
        p.setProperty("players.count", game.players.size.toString())
        game.players.forEachIndexed { i, player ->
            val k = "player.$i."; p.setProperty(k+"id", player.id); p.setProperty(k+"name", player.name); p.setProperty(k+"balance", player.balance.toString()); p.setProperty(k+"position", player.position.toString()); p.setProperty(k+"jail", player.inJail.toString()); p.setProperty(k+"jailTurns", player.jailTurns.toString()); p.setProperty(k+"freeParking", player.atFreeParking.toString()); p.setProperty(k+"bankrupt", player.bankrupt.toString())
        }
        game.board.cells.forEach { cell -> cell.property?.let { prop ->
            val k = "property.${cell.number}."; p.setProperty(k+"owner", game.players.indexOfFirst { it === prop.owner }.toString()); p.setProperty(k+"mortgaged", prop.mortgaged.toString()); if (prop is Street) p.setProperty(k+"houses", prop.houses.toString())
        } }
        saveDeck(p, "chance", game.chanceDeck); saveDeck(p, "chest", game.communityChestDeck)
        p.setProperty("moves.count", game.moveHistory.moves.size.toString())
        game.moveHistory.moves.forEachIndexed { i, move -> p.setProperty("move.$i.number", move.number.toString()); p.setProperty("move.$i.player", move.playerName); p.setProperty("move.$i.descriptions", move.descriptions.joinToString("\n")) }
        File(directory, "${game.id}.properties").outputStream().use { p.store(it, "Сохранённая партия") }
    }
    private fun saveDeck(p: Properties, key: String, deck: Deck) { p.setProperty("$key.order", deck.cards.joinToString(",") { it.number.toString() }); p.setProperty("$key.index", deck.currentIndex.toString()) }
    override fun findById(id: String): GameSession? {
        val file = File(directory, "$id.properties"); if (!file.exists()) return null
        val p = Properties(); file.inputStream().use(p::load)
        val players = (0 until p.getProperty("players.count", "0").toInt()).map { i ->
            val k = "player.$i."; Player(p.getProperty(k+"id"), p.getProperty(k+"name")).also { player ->
                player.balance=p.getProperty(k+"balance").toInt(); player.position=p.getProperty(k+"position").toInt(); player.inJail=p.getProperty(k+"jail", "false").toBoolean(); player.jailTurns=p.getProperty(k+"jailTurns", "0").toInt(); player.atFreeParking=p.getProperty(k+"freeParking", "false").toBoolean(); player.bankrupt=p.getProperty(k+"bankrupt", "false").toBoolean()
            }
        }
        val board = CellFactory.createBoard(); val game = GameSession(id, players, board, CardFactory.createChanceDeck(), CardFactory.createCommunityChestDeck())
        board.cells.forEach { cell -> cell.property?.let { prop ->
            val k="property.${cell.number}."; val owner=p.getProperty(k+"owner", "-1").toInt(); if(owner in players.indices) players[owner].addProperty(prop)
            prop.mortgaged=p.getProperty(k+"mortgaged", "false").toBoolean(); if(prop is Street) repeat(p.getProperty(k+"houses", "0").toInt().coerceIn(0,5)) { prop.buildHouse() }
        } }
        restoreDeck(p, "chance", game.chanceDeck); restoreDeck(p, "chest", game.communityChestDeck)
        repeat(p.getProperty("moves.count", "0").toInt()) { i -> game.moveHistory.add(Move(p.getProperty("move.$i.number").toInt(), p.getProperty("move.$i.player"), p.getProperty("move.$i.descriptions", "").split("\n").filter { it.isNotBlank() })) }
        val current = p.getProperty("current", "0").toInt().coerceIn(players.indices)
        game.restoreCurrentPlayer(current)
        game.turnInProgress = p.getProperty("turnInProgress", "false").toBoolean()
        if (!p.getProperty("active", "true").toBoolean()) game.finish()
        return game
    }
    private fun restoreDeck(p: Properties, key: String, deck: Deck) {
        val order = p.getProperty("$key.order")?.split(",")?.mapNotNull(String::toIntOrNull)
        if (order?.size == 10) deck.reorder(order)
        repeat(p.getProperty("$key.index", "0").toInt().coerceIn(0,9)) { deck.draw() }
    }
    override fun findAll(): List<GameSession> = if (!directory.exists()) emptyList() else
        directory.listFiles { f -> f.extension == "properties" }?.mapNotNull { findById(it.nameWithoutExtension) }
            ?.sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE } ?: emptyList()
    override fun delete(id: String) { File(directory, "$id.properties").delete() }
}
