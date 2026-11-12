package monopoly.infrastructure.memory

import monopoly.domain.model.MoveHistory
import monopoly.domain.repository.MoveHistoryRepository

class InMemoryMoveHistoryRepository : MoveHistoryRepository {
    private val histories = mutableMapOf<Int, MoveHistory>()

    override fun save(history: MoveHistory) {
        histories[history.gameId] = history
    }

    override fun findByGameId(gameId: Int): MoveHistory? = histories[gameId]
}
