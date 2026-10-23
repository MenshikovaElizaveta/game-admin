package monopoly.domain.model.card

class SpecificMovementCard(
    override val number: Int,
    override val text: String,
    val destination: Int,
    val passesStartReward: Boolean = true,
) : Card {
    override val type: CardType = CardType.SPECIFIC_MOVEMENT

    init {
        require(destination in 0..39)
    }
}
