package monopoly.domain.model

class PlayerStatistics(
    val playerId: String,
    val playerName: String,
    var gamesPlayed: Int = 0,
    var wins: Int = 0,
)
