package monopoly.domain.service

import monopoly.domain.model.Player
import monopoly.domain.model.property.Property

class BankruptcyService {
    fun bankruptToBank(player: Player, properties: List<Property>): List<Property> {
        removeAllHouses(player)
        val lots = properties.filter { it.owner === player }.toList()
        lots.forEach { player.removeProperty(it) }
        player.bankrupt = true
        player.balance = 0
        return lots
    }

    fun bankruptToPlayer(player: Player, creditor: Player) {
        removeAllHouses(player)
        player.properties.toList().forEach { property ->
            player.removeProperty(property)
            creditor.addProperty(property)
        }
        player.bankrupt = true
        player.balance = 0
    }

    private fun removeAllHouses(player: Player) {
        player.streets().forEach { street ->
            while (street.houses > 0) street.sellHouse()
        }
    }
}
