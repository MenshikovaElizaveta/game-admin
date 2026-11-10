package monopoly.infrastructure.file

import monopoly.domain.model.GameSummary
import monopoly.domain.repository.GameHistoryRepository
import java.io.File
import java.util.Properties

class FileGameHistoryRepository(
    private val file: File = File("data/game-history.properties"),
) : GameHistoryRepository {

    private fun load(): Properties {
        return Properties().also { properties ->
            if (file.exists()) {
                file.inputStream().use(properties::load)
            }
        }
    }

    override fun save(summary: GameSummary) {
        val properties = load()
        val prefix = "game.${summary.gameId}."

        properties.setProperty(
            prefix + "players",
            summary.playerNames.joinToString("|"),
        )
        properties.setProperty(
            prefix + "moves",
            summary.moveCount.toString(),
        )
        properties.setProperty(prefix + "winner", summary.winnerName)

        file.parentFile?.mkdirs()
        file.outputStream().use { output ->
            properties.store(output, "История игр")
        }
    }

    override fun findAll(): List<GameSummary> {
        val properties = load()

        return properties.stringPropertyNames()
            .filter { key ->
                key.startsWith("game.") && key.endsWith(".players")
            }
            .mapNotNull { key ->
                val id = key
                    .removePrefix("game.")
                    .removeSuffix(".players")
                    .toIntOrNull() ?: return@mapNotNull null

                val prefix = "game.$id."
                val players = properties.getProperty(prefix + "players")

                GameSummary(
                    id,
                    players.split("|").filter { it.isNotBlank() },
                    properties.getProperty(prefix + "moves", "0").toInt(),
                    properties.getProperty(prefix + "winner", "—"),
                )
            }
            .sortedBy { it.gameId }
    }
}
