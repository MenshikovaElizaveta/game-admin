package monopoly.domain.model.card

class Deck(cards: List<Card>) {
    var cards: List<Card> = cards.toList()
        private set
    var currentIndex: Int = 0
        private set

    init {
        require(cards.size == 10)
        require(cards.map { it.number }.toSet() == (1..10).toSet())
    }

    fun draw(): Card {
        val card = cards[currentIndex]
        currentIndex = (currentIndex + 1) % cards.size
        return card
    }

    fun reorder(order: List<Int>) {
        require(order.size == 10)
        require(order.toSet() == (1..10).toSet())
        cards = order.map { number -> cards.first { it.number == number } }
        currentIndex = 0
    }
}
