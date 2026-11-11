package monopoly.infrastructure.file

import monopoly.domain.model.PlayerStatistics
import monopoly.domain.repository.PlayerStatisticsRepository
import java.io.File
import java.util.Properties

class FilePlayerStatisticsRepository(private val file: File = File("data/statistics.properties")) : PlayerStatisticsRepository {
    private fun load() = Properties().also { if (file.exists()) file.inputStream().use(it::load) }
    private fun store(p: Properties) { file.parentFile?.mkdirs(); file.outputStream().use { p.store(it, "Статистика игроков") } }
    override fun save(statistics: PlayerStatistics) {
        val p = load(); val id = statistics.playerId
        p.setProperty("$id.name", statistics.playerName); p.setProperty("$id.games", statistics.gamesPlayed.toString()); p.setProperty("$id.wins", statistics.wins.toString()); store(p)
    }
    override fun findByPlayerId(playerId: String): PlayerStatistics? = load().let { p ->
        val name = p.getProperty("$playerId.name") ?: return null
        PlayerStatistics(playerId, name, p.getProperty("$playerId.games", "0").toInt(), p.getProperty("$playerId.wins", "0").toInt())
    }
    override fun findByName(name: String): PlayerStatistics? = findAll().firstOrNull { it.playerName.equals(name, true) }
    override fun findAll(): List<PlayerStatistics> = load().stringPropertyNames().filter { it.endsWith(".name") }.mapNotNull { key ->
        val id = key.removeSuffix(".name"); findByPlayerId(id)
    }.sortedBy { it.playerName.lowercase() }
}
