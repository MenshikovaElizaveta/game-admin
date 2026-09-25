package monopoly.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import monopoly.domain.model.property.Utility

class UtilityTest {
    @Test
    fun oneUtilityChargesFourTimesDiceSum() {
        val player = Player("1", "Саша")
        val utility = Utility(12, "электростанция")
        player.addProperty(utility)

        assertEquals(28, utility.calculateRent(7))
    }

    @Test
    fun twoUtilitiesChargeTenTimesDiceSum() {
        val player = Player("1", "Саша")
        val first = Utility(12, "электростанция")
        val second = Utility(28, "водопровод")
        player.addProperty(first)
        player.addProperty(second)

        assertEquals(70, first.calculateRent(7))
    }
}
