package monopoly.infrastructure.file

import monopoly.domain.model.Move
import monopoly.domain.model.MoveHistory
import monopoly.domain.repository.MoveHistoryRepository
import java.io.File
import java.util.Properties

class FileMoveHistoryRepository(private val directory: File = File("data/move-history")) : MoveHistoryRepository {
    override fun save(history: MoveHistory) {
        directory.mkdirs(); val p = Properties(); p.setProperty("gameId", history.gameId); p.setProperty("count", history.moves.size.toString())
        history.moves.forEachIndexed { i, move ->
            p.setProperty("$i.number", move.number.toString()); p.setProperty("$i.player", move.playerName)
            p.setProperty("$i.descriptions", move.descriptions.joinToString("\n"))
        }
        File(directory, "${history.gameId}.properties").outputStream().use { p.store(it, "История ходов") }
    }
    override fun findByGameId(gameId: String): MoveHistory? {
        val file = File(directory, "$gameId.properties"); if (!file.exists()) return null
        val p = Properties(); file.inputStream().use(p::load); val result = MoveHistory(gameId)
        repeat(p.getProperty("count", "0").toInt()) { i -> result.add(Move(p.getProperty("$i.number").toInt(), p.getProperty("$i.player"), p.getProperty("$i.descriptions", "").split("\n").filter { it.isNotBlank() })) }
        return result
    }
}
