package monopoly.infrastructure.memory

import monopoly.domain.model.GameSession
import monopoly.domain.repository.GameRepository

class InMemoryGameRepository : GameRepository {
    private val games = mutableMapOf<String, GameSession>()
    override fun save(game: GameSession) { games[game.id] = game }
    override fun findById(id: String): GameSession? = games[id]
    override fun findAll(): List<GameSession> = games.values.toList()
    override fun delete(id: String) { games.remove(id) }
}
