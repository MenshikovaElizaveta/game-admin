package monopoly.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals
import monopoly.domain.model.Player

class MovementServiceTest {
    @Test
    fun passingStartGivesTwoHundred() {
        val player = Player("1", "Саша")
        player.position = 35

        MovementService.move(player, 6)

        assertEquals(1700, player.balance)
        assertEquals(1, player.position)
    }

    @Test
    fun movementToJailDoesNotGiveStartReward() {
        val player = Player("1", "Саша")
        player.position = 36

        MovementService.moveTo(player, 30, rewardForPassingStart = false)

        assertEquals(1500, player.balance)
        assertEquals(30, player.position)
    }
}
