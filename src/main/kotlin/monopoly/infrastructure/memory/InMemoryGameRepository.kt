package monopoly.infrastructure.memory

import monopoly.domain.model.GameSession
import monopoly.domain.repository.GameRepository

class InMemoryGameRepository : GameRepository {
    private val games = mutableMapOf<Int, GameSession>()
    private var nextGameNumber = 1

    override fun nextId(): Int {
        while (games.containsKey(nextGameNumber)) {
            nextGameNumber++
        }
        return nextGameNumber++
    }

    override fun save(game: GameSession) {
        games[game.id] = game
    }

    override fun findById(id: Int): GameSession? {
        return games[id]
    }

    override fun findAll(): List<GameSession> {
        return games.values.toList()
    }

    override fun delete(id: Int) {
        games.remove(id)
    }
}
