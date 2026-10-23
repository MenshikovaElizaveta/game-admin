package monopoly.domain.model.card

class IncomeCard(
    override val number: Int,
    override val text: String,
    val amount: Int,
) : Card {
    override val type: CardType = CardType.INCOME
    init { require(amount >= 0) }
}
