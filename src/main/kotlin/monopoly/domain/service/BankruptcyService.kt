package monopoly.domain.service

import monopoly.domain.model.Player
import monopoly.domain.model.property.Property

object BankruptcyService {
    fun bankruptToBank(player: Player): List<Property> {
        removeAllHouses(player)
        val lots = player.properties.toList()
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
        player.streets.forEach { street ->
            while (street.houses > 0) street.sellHouse()
        }
    }
}
