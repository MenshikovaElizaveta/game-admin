package monopoly.infrastructure.memory

import monopoly.domain.model.GameSummary
import monopoly.domain.repository.GameHistoryRepository

class InMemoryGameHistoryRepository : GameHistoryRepository {
    private val history = mutableListOf<GameSummary>()
    override fun save(summary: GameSummary) { history += summary }
    override fun findAll(): List<GameSummary> = history.toList()
    override fun findById(gameId: String): GameSummary? = history.firstOrNull { it.gameId == gameId }
}
