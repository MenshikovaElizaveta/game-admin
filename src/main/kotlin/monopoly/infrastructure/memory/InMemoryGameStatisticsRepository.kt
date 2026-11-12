package monopoly.infrastructure.memory

import monopoly.domain.model.PlayerStatistics
import monopoly.domain.repository.PlayerStatisticsRepository

class InMemoryGameStatisticsRepository : PlayerStatisticsRepository {

    private val statistics = mutableMapOf<String, PlayerStatistics>()

    override fun save(statistics: PlayerStatistics) {
        this.statistics[statistics.playerName.lowercase()] = statistics
    }

    override fun findByName (name: String): PlayerStatistics? {
        return statistics[name.lowercase()]
    }

    override fun findAll(): List<PlayerStatistics> {
        return  statistics.values.toList()
    }
}
