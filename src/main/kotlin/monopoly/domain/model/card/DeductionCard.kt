package monopoly.domain.model.card

class DeductionCard(
    override val number: Int,
    override val text: String,
    val amount: Int,
) : Card {
    override val type: CardType = CardType.DEDUCTION
    init { require(amount >= 0) }
}
