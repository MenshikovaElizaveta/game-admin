package monopoly.domain.model.property

import monopoly.domain.model.Player

class Utility(
    override val number: Int,
    override val name: String,
    override val price: Int = 150,
) : Property {
    override var owner: Player? = null
    override var mortgaged: Boolean = false

    fun calculateRent(diceSum: Int): Int = when (owner?.utilities?.size) {
        1 -> diceSum * 4
        2 -> diceSum * 10
        else -> 0
    }
}
