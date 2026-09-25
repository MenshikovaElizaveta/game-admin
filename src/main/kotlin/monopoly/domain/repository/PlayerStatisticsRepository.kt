package monopoly.domain.repository

import monopoly.domain.model.PlayerStatistics

interface PlayerStatisticsRepository {
    fun save(statistics: PlayerStatistics)
    fun findByPlayerId(playerId: String): PlayerStatistics?
    fun findByName(name: String): PlayerStatistics?
    fun findAll(): List<PlayerStatistics>
}
