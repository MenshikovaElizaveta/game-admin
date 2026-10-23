package monopoly.domain.model

class GameSummary(
    val gameId: String,
    val playerNames: List<String>,
    val moveCount: Int,
    val winnerName: String,
)
