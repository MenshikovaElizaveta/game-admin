package monopoly.domain.model

data class PlayerStatistics(
    val playerName: String,
    var gamesPlayed: Int = 0,
    var wins: Int = 0,
)
