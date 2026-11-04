package monopoly.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals
import monopoly.domain.model.Player
import monopoly.domain.model.card.Deck
import monopoly.domain.model.card.CardFactory
import monopoly.domain.model.card.SpecificMovementCard

class CardServiceTest {
    @Test
    fun incomeCardChangesBalance() {
        val player = Player("Саша")
        val service = CardService()
        val cards = (1..10).map { number ->
            val description = if (number == 1) "Получите 50 М" else "Тест"
            val position = 1

            SpecificMovementCard(number, description, position, true)
        }

        val deck = Deck(cards)

        service.draw(player, deck)

        assertEquals(1500, player.balance)
    }

    @Test
    fun jailCardDoesNotGiveStartReward() {
        val player = Player("Саша")
        player.position = 36
        val service = CardService()
        val cards = (1..10).map { number ->
            val description = if (number == 1) "Отправляйтесь в тюрьму" else "Тест"
            val position = if (number == 1) 30 else 1
            val canCollectMoney = number != 1

            SpecificMovementCard(number, description, position, canCollectMoney)
        }

        val deck = Deck(cards)
        val result = service.draw(player, deck)

        assertEquals(10, player.position)
        assertEquals(true, player.inJail)
        assertEquals(false, result.passedStart)
    }

    @Test
    fun startCardCanGiveStartReward() {
        val player = Player("Саша")
        player.position = 10
        val service = CardService()
        val cards = (1..10).map { number ->
            val description = if (number == 1) "Отправляйтесь на Старт" else "Тест"
            val position = if (number == 1) 0 else 1

            SpecificMovementCard(number, description, position, true)
        }

        val deck = Deck(cards)
        val result = service.draw(player, deck)

        assertEquals(0, player.position)
        assertEquals(true, result.passedStart)
        assertEquals(1700, player.balance)
    }
}
