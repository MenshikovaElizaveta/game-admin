package monopoly.domain.repository

import monopoly.domain.model.GameSummary

interface GameHistoryRepository {
    fun save(summary: GameSummary)
    fun findAll(): List<GameSummary>
    fun findById(gameId: String): GameSummary?
}
