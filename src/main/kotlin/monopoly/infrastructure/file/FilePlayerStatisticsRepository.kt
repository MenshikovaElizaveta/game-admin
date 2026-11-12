package monopoly.infrastructure.file

import monopoly.domain.model.PlayerStatistics
import monopoly.domain.repository.PlayerStatisticsRepository
import java.io.File
import java.util.Properties

class FilePlayerStatisticsRepository(
    private val file: File = File("data/statistics.properties"),
) : PlayerStatisticsRepository {

    private fun load(): Properties {
        return Properties().also { properties ->
            if (file.exists()) {
                file.inputStream().use(properties::load)
            }
        }
    }

    private fun store(properties: Properties) {
        file.parentFile?.mkdirs()
        file.outputStream().use { output ->
            properties.store(output, "Статистика игроков")
        }
    }

    override fun save(statistics: PlayerStatistics) {
        val properties = load()
        val key = statistics.playerName.lowercase()

        properties.setProperty("$key.name", statistics.playerName)
        properties.setProperty("$key.games", statistics.gamesPlayed.toString(),)
        properties.setProperty("$key.wins", statistics.wins.toString(),)

        store(properties)
    }

    override fun findByName(name: String): PlayerStatistics? {
        val properties = load()
        val key = name.lowercase()

        val playerName = properties.getProperty("$key.name") ?: return null

        return PlayerStatistics(
            playerName,
            properties.getProperty("$key.games", "0").toInt(),
            properties.getProperty("$key.wins", "0").toInt()
        )
    }

    override fun findAll(): List<PlayerStatistics> {
        val properties = load()

        return properties.stringPropertyNames()
            .filter { it.endsWith(".name") }
            .mapNotNull { key ->
                findByName(key.removeSuffix(".name"))
            }
            .sortedBy { it.playerName.lowercase() }
    }
}
