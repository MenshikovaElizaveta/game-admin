package monopoly.domain.model.property

import monopoly.domain.model.Player

class Railroad(
    override val number: Int,
    override val name: String,
    override val price: Int = 200,
) : Property {
    override var owner: Player? = null
    override var mortgaged: Boolean = false

    fun calculateRent(): Int = when (owner?.railroads?.size) {
        1 -> 25
        2 -> 50
        3 -> 100
        4 -> 200
        else -> 0
    }
}
