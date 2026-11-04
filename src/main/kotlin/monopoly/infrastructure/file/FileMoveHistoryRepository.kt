package monopoly.infrastructure.file

import monopoly.domain.model.Move
import monopoly.domain.model.MoveHistory
import monopoly.domain.repository.MoveHistoryRepository
import java.io.File
import java.util.Properties

class FileMoveHistoryRepository(
    private val directory: File = File("data/move-history"),
) : MoveHistoryRepository {

    override fun save(history: MoveHistory) {
        directory.mkdirs()

        val properties = Properties()
        properties.setProperty("gameId", history.gameId.toString())
        properties.setProperty("count", history.moves.size.toString())

        history.moves.forEachIndexed { index, move ->
            properties.setProperty("$index.number", move.number.toString())
            properties.setProperty("$index.player", move.playerName)
            properties.setProperty(
                "$index.descriptions",
                move.descriptions.joinToString("\n"),
            )
        }

        val file = File(directory, "${history.gameId}.properties")
        file.outputStream().use { output ->
            properties.store(output, "История ходов")
        }
    }

    override fun findByGameId(gameId: Int): MoveHistory? {
        val file = File(directory, "$gameId.properties")
        if (!file.exists()) return null

        val properties = Properties()
        file.inputStream().use(properties::load)

        val result = MoveHistory(gameId)
        val count = properties.getProperty("count", "0").toInt()

        repeat(count) { index ->
            val descriptions = properties
                .getProperty("$index.descriptions", "")
                .split("\n")
                .filter { it.isNotBlank() }

            result.add(
                Move(
                    properties.getProperty("$index.number").toInt(),
                    properties.getProperty("$index.player"),
                    descriptions,
                ),
            )
        }

        return result
    }
}
