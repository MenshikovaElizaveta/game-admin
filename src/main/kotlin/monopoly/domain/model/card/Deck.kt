package monopoly.domain.model.card

interface Deck {
    val cards: List<Card>
    val currentIndex: Int
    fun draw(): Card
    fun reorder(order: List<Int>)
}
