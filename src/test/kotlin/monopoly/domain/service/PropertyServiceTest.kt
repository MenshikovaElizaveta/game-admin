package monopoly.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals
import monopoly.domain.model.Player
import monopoly.domain.model.property.PropertyColor
import monopoly.domain.model.property.Street

class PropertyServiceTest {
    private fun street(number: Int, color: PropertyColor = PropertyColor.BROWN) =
        Street(number, "Улица $number", 60, 50, listOf(2, 10, 30, 90, 160, 250), color)

    @Test
    fun buyingStreetChangesBalanceAndOwner() {
        val player = Player("1", "Саша")
        val property = street(1)

        PropertyService.buy(player, property)

        assertEquals(1440, player.balance)
        assertEquals(player, property.owner)
    }

    @Test
    fun mortgageGivesHalfOfPrice() {
        val player = Player("1", "Саша")
        val property = street(1)
        player.addProperty(property)

        PropertyService.mortgage(player, property)

        assertEquals(1530, player.balance)
        assertEquals(true, property.mortgaged)
    }

    @Test
    fun unmortgageCostsOneHundredTenPercent() {
        val player = Player("1", "Саша")
        val property = street(1)
        player.addProperty(property)
        PropertyService.mortgage(player, property)

        PropertyService.unmortgage(player, property)

        assertEquals(1464, player.balance)
        assertEquals(false, property.mortgaged)
    }
}
