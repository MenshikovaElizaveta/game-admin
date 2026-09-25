package monopoly.domain.repository

import monopoly.domain.model.GameSession

interface GameRepository {
    fun save(game: GameSession)
    fun findById(id: String): GameSession?
    fun findAll(): List<GameSession>
    fun delete(id: String)
}
