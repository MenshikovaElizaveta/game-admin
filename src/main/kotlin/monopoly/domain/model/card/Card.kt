package monopoly.domain.model.card

interface Card {
    val number: Int
    val text: String
    val type: CardType
}
