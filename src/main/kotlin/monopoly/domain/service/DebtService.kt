package monopoly.domain.service

import monopoly.domain.model.Player
import monopoly.domain.model.property.Property

object DebtService {
    fun payToBank(player: Player, amount: Int) {
        require(amount >= 0)
        require(player.balance >= amount) { "Недостаточно денег." }
        player.balance -= amount
    }

    fun payToPlayer(debtor: Player, creditor: Player, amount: Int) {
        require(amount >= 0)
        require(debtor.balance >= amount) { "Недостаточно денег." }
        debtor.balance -= amount
        creditor.balance += amount
    }

    fun availableProperties(player: Player): List<Property> =
        player.properties.filter(GameRules::removableDebtProperty)
}
