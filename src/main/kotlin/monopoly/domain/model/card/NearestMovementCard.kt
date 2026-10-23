package monopoly.domain.model.card

class NearestMovementCard(
    override val number: Int,
    override val text: String,
    val destinations: List<Int>,
    val passesStartReward: Boolean = true,
) : Card {
    override val type: CardType = CardType.NEAREST_MOVEMENT

    init {
        require(destinations.isNotEmpty())
        require(destinations.all { it in 0..39 })
    }
}
