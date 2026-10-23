package monopoly.domain.model

import monopoly.domain.model.card.Deck
import monopoly.domain.model.cell.Board

class GameSession(
    val id: String,
    val players: List<Player>,
    val board: Board,
    val chanceDeck: Deck,
    val communityChestDeck: Deck,
) {
    var currentPlayerIndex: Int = 0
        private set
    var active: Boolean = true
        private set

    val moveHistory = MoveHistory(id)

    init {
        require(players.size in 2..8) { "В игре должно быть от 2 до 8 игроков." }
        require(players.map { it.name.lowercase() }.toSet().size == players.size) {
            "Имена игроков должны быть уникальными."
        }
    }

    val currentPlayer: Player
        get() = players[currentPlayerIndex]

    fun finish() {
        active = false
    }

    fun moveToNextActivePlayer(): Player? {
        val alive = players.filterNot { it.bankrupt }
        if (alive.size <= 1) return alive.firstOrNull()

        var index = (currentPlayerIndex + 1) % players.size
        repeat(players.size) {
            if (!players[index].bankrupt) {
                currentPlayerIndex = index
                return players[index]
            }
            index = (index + 1) % players.size
        }
        return null
    }

    fun winner(): Player? = players.filterNot { it.bankrupt }.singleOrNull()
}
