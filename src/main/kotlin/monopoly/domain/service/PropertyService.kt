package monopoly.domain.service

import monopoly.domain.model.Player
import monopoly.domain.model.property.Property
import monopoly.domain.model.property.Street

object PropertyService {
    fun buy(player: Player, property: Property) {
        require(property.owner == null) { "Недвижимость уже занята." }
        require(player.balance >= property.price) { "Недостаточно денег." }
        player.balance -= property.price
        player.addProperty(property)
    }

    fun buildHouse(player: Player, street: Street) {
        require(GameRules.canBuild(player, street)) { "Нельзя построить дом на этой улице." }
        player.balance -= street.houseCost
        street.buildHouse()
    }

    fun sellHouse(player: Player, street: Street) {
        require(street.owner === player) { "Игрок не владеет улицей." }
        require(street.houses > 0) { "На улице нет домов." }
        street.sellHouse()
        player.balance += street.houseCost / 2
    }

    fun mortgage(player: Player, property: Property) {
        require(property.owner === player) { "Игрок не владеет недвижимостью." }
        require(GameRules.removableDebtProperty(property)) { "Эту недвижимость сейчас нельзя заложить." }
        property.mortgaged = true
        player.balance += property.mortgageValue
    }

    fun unmortgage(player: Player, property: Property) {
        require(property.owner === player) { "Игрок не владеет недвижимостью." }
        require(property.mortgaged) { "Недвижимость не заложена." }
        require(player.balance >= property.unmortgageCost) { "Недостаточно денег." }
        player.balance -= property.unmortgageCost
        property.mortgaged = false
    }
}
