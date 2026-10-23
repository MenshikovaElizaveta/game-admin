package monopoly.domain.service

import monopoly.domain.model.Player
import monopoly.domain.model.property.Property
import monopoly.domain.model.property.PropertyColor
import monopoly.domain.model.property.Street

object GameRules {
    const val BOARD_SIZE = 40
    const val PASS_START_REWARD = 200
    const val INITIAL_BALANCE = 1500
    const val JAIL_FINE = 50
    const val TAX = 200
    const val SUPER_TAX = 100

    fun passedStart(previous: Int, movement: Int): Boolean = previous + movement >= BOARD_SIZE

    fun colorGroupSize(color: PropertyColor): Int = when (color) {
        PropertyColor.BROWN, PropertyColor.BLUE -> 2
        else -> 3
    }

    fun fullColorGroup(player: Player, street: Street): Boolean =
        player.streets().count { it.color == street.color } == colorGroupSize(street.color)

    fun canBuild(player: Player, street: Street): Boolean =
        street.owner === player && fullColorGroup(player, street) &&
            !street.mortgaged && street.houses < 5 && player.balance >= street.houseCost

    fun removableDebtProperty(property: Property): Boolean =
        !property.mortgaged && (property !is Street || property.houses == 0)
}
