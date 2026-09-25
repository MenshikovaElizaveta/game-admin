package monopoly.infrastructure.memory

import monopoly.domain.model.PlayerStatistics
import monopoly.domain.repository.PlayerStatisticsRepository

class InMemoryGameStatisticsRepository : PlayerStatisticsRepository {
    private val statistics = mutableMapOf<String, PlayerStatistics>()
    override fun save(statistics: PlayerStatistics) { this.statistics[statistics.playerId] = statistics }
    override fun findByPlayerId(playerId: String): PlayerStatistics? = statistics[playerId]
    override fun findByName(name: String): PlayerStatistics? =
        statistics.values.firstOrNull { it.playerName.equals(name, ignoreCase = true) }
    override fun findAll(): List<PlayerStatistics> = statistics.values.toList()
}
