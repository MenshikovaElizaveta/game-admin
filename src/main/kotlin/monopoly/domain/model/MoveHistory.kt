package monopoly.domain.model

class MoveHistory(
    val gameId: Int,
    val moves: MutableList<Move> = mutableListOf(),
) {
    val moveCount: Int
        get() = moves.size

    fun add(move: Move) {
        moves += move
    }
}
