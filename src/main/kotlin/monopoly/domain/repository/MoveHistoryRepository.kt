package monopoly.domain.repository

import monopoly.domain.model.MoveHistory

interface MoveHistoryRepository {
    fun save(history: MoveHistory)
    fun findByGameId(gameId: String): MoveHistory?
}
