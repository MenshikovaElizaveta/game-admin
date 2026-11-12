package monopoly.domain.model

data class Move(
    val number: Int,
    val playerName: String,
    val descriptions: List<String>,
)
