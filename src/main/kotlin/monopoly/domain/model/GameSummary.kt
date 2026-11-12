package monopoly.domain.model

data class GameSummary(
    val gameId: Int,
    val playerNames: List<String>,
    val moveCount: Int,
    val winnerName: String,
)
