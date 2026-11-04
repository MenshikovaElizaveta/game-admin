package monopoly.domain.repository

import monopoly.domain.model.GameSession

interface GameRepository {
    fun save(game: GameSession)
    fun nextId(): Int
    fun findById(id: Int): GameSession?
    fun findAll(): List<GameSession>
    fun delete(id: Int)
}
