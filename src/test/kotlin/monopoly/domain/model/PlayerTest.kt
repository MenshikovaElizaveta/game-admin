package monopoly.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import monopoly.domain.model.property.Railroad
import monopoly.domain.model.property.Utility

class PlayerTest {
    @Test
    fun playerStartsWithExpectedState() {
        val player = Player("1", "Саша")

        assertEquals(1500, player.balance)
        assertEquals(0, player.position)
        assertEquals(false, player.inJail)
        assertEquals(0, player.jailTurns)
        assertEquals(false, player.atFreeParking)
        assertEquals(false, player.bankrupt)
    }

    @Test
    fun playerStoresRailroadsAndUtilitiesInSeparateLists() {
        val player = Player("1", "Саша")
        val railroad = Railroad(5, "Рижская железная дорога")
        val utility = Utility(12, "электростанция")

        player.addProperty(railroad)
        player.addProperty(utility)

        assertEquals(listOf(railroad), player.railroads)
        assertEquals(listOf(utility), player.utilities)
        assertEquals(2, player.properties.size)
    }
}
