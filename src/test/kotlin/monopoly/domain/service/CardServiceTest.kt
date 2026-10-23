package monopoly.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals
import monopoly.domain.model.Player
import monopoly.domain.model.card.CardDeck
import monopoly.domain.model.card.CardFactory
import monopoly.domain.model.card.SpecificMovementCard

class CardServiceTest {
    @Test
    fun incomeCardChangesBalance() {
        val player = Player("1", "Саша")
        val service = CardService()
        val deck = CardDeck((1..10).map { number -> if (number == 1) monopoly.domain.model.card.IncomeCard(number, "Получите 50 М", 50) else SpecificMovementCard(number, "Тест", 1) })

        service.draw(player, deck)

        assertEquals(1550, player.balance)
    }

    @Test
    fun jailCardDoesNotGiveStartReward() {
        val player = Player("1", "Саша")
        player.position = 36
        val service = CardService()
        val deck = CardDeck((1..10).map { number -> SpecificMovementCard(number, if (number == 1) "Отправляйтесь в тюрьму" else "Тест", if (number == 1) 30 else 1, if (number == 1) false else true) })

        val result = service.draw(player, deck)

        assertEquals(30, player.position)
        assertEquals(true, player.inJail)
        assertEquals(false, result.passedStart)
    }

    @Test
    fun startCardCanGiveStartReward() {
        val player = Player("1", "Саша")
        player.position = 10
        val service = CardService()
        val deck = CardDeck((1..10).map { number -> SpecificMovementCard(number, if (number == 1) "Отправляйтесь на Старт" else "Тест", if (number == 1) 0 else 1, true) })

        val result = service.draw(player, deck)

        assertEquals(0, player.position)
        assertEquals(true, result.passedStart)
        assertEquals(1700, player.balance)
    }
}
