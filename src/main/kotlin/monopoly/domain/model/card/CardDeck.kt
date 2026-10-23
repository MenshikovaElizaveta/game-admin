package monopoly.domain.model.card

class CardDeck(cards: List<Card>) : Deck {
    override var cards: List<Card> = cards.toList()
        private set
    override var currentIndex: Int = 0
        private set

    init {
        require(cards.size == 10)
        require(cards.map { it.number }.toSet() == (1..10).toSet())
    }

    override fun draw(): Card {
        val card = cards[currentIndex]
        currentIndex = (currentIndex + 1) % cards.size
        return card
    }

    override fun reorder(order: List<Int>) {
        require(order.size == 10)
        require(order.toSet() == (1..10).toSet())
        cards = order.map { number -> cards.first { it.number == number } }
        currentIndex = 0
    }
}
