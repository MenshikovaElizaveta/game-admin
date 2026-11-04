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
        val id = statistics.playerId

        properties.setProperty("$id.name", statistics.playerName)
        properties.setProperty(
            "$id.games",
            statistics.gamesPlayed.toString(),
        )
        properties.setProperty(
            "$id.wins",
            statistics.wins.toString(),
        )

        store(properties)
    }

    override fun findByPlayerId(
        playerId: String,
    ): PlayerStatistics? {
        return load().let { properties ->
            val name = properties.getProperty("$playerId.name")
                ?: return null

            PlayerStatistics(
                playerId,
                name,
                properties.getProperty("$playerId.games", "0").toInt(),
                properties.getProperty("$playerId.wins", "0").toInt(),
            )
        }
    }

    override fun findByName(name: String): PlayerStatistics? {
        return findAll().firstOrNull {
            it.playerName.equals(name, ignoreCase = true)
        }
    }

    override fun findAll(): List<PlayerStatistics> {
        return load()
            .stringPropertyNames()
            .filter { it.endsWith(".name") }
            .mapNotNull { key ->
                val id = key.removeSuffix(".name")
                findByPlayerId(id)
            }
            .sortedBy { it.playerName.lowercase() }
    }
}
