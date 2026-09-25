package monopoly.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals
import monopoly.domain.model.Player
import monopoly.domain.model.property.PropertyColor
import monopoly.domain.model.property.Street

class BankruptcyServiceTest {
    @Test
    fun bankruptcyToPlayerTransfersAllPropertiesAndRemovesHouses() {
        val debtor = Player("1", "Саша")
        val creditor = Player("2", "Таня")
        val first = Street(1, "Житная ул", 60, 50, listOf(2, 10, 30, 90, 160, 250), PropertyColor.BROWN)
        val second = Street(3, "Нагатинская ул", 60, 50, listOf(2, 10, 30, 90, 160, 250), PropertyColor.BROWN)
        debtor.addProperty(first)
        debtor.addProperty(second)
        first.buildHouse()

        BankruptcyService().bankruptToPlayer(debtor, creditor)

        assertEquals(true, debtor.bankrupt)
        assertEquals(0, first.houses)
        assertEquals(0, debtor.properties.size)
        assertEquals(2, creditor.properties.size)
        assertEquals(creditor, first.owner)
    }
}
