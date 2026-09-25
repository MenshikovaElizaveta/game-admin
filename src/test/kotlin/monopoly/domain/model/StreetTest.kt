package monopoly.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import monopoly.domain.model.property.PropertyColor
import monopoly.domain.model.property.Street

class StreetTest {
    @Test
    fun streetSupportsMaximumFiveHouses() {
        val street = Street(1, "Житная ул", 60, 50, listOf(2, 10, 30, 90, 160, 250), PropertyColor.BROWN)

        repeat(5) { street.buildHouse() }

        assertEquals(5, street.houses)
        assertEquals(250, street.calculateRent())
    }

    @Test
    fun sellingHouseDecreasesHouseCount() {
        val street = Street(1, "Житная ул", 60, 50, listOf(2, 10, 30, 90, 160, 250), PropertyColor.BROWN)
        street.buildHouse()
        street.sellHouse()

        assertEquals(0, street.houses)
    }
}
