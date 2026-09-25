package monopoly.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import monopoly.domain.model.card.CardFactory
import monopoly.domain.model.card.DeductionCard
import monopoly.domain.model.card.IncomeCard
import monopoly.domain.model.card.NearestMovementCard
import monopoly.domain.model.card.SpecificMovementCard

class CardDeckTest {
    @Test
    fun chanceDeckContainsRequiredTenCards() {
        val deck = CardFactory.createChanceDeck()

        assertEquals((1..10).toList(), deck.cards.map { it.number })
        assertEquals("Отправляйтесь на Старт", deck.cards[0].text)
        assertEquals("Получите дивиденды 50 М", deck.cards[9].text)
        assert(deck.cards[2] is NearestMovementCard)
        assert(deck.cards[4] is IncomeCard)
        assert(deck.cards[6] is DeductionCard)
        assert(deck.cards[8] is SpecificMovementCard)
    }

    @Test
    fun communityChestContainsRequiredTenCards() {
        val deck = CardFactory.createCommunityChestDeck()

        assertEquals("Отправляйтесь на Старт", deck.cards[0].text)
        assertEquals("Идите до ближайшей железнодорожной станции", deck.cards[7].text)
        assertEquals("Получите 200 М", deck.cards[9].text)
    }

    @Test
    fun deckIsCyclicAndCanBeReordered() {
        val deck = CardFactory.createChanceDeck()
        deck.reorder((10 downTo 1).toList())

        assertEquals(10, deck.draw().number)
        repeat(9) { deck.draw() }
        assertEquals(10, deck.draw().number)
    }
}
