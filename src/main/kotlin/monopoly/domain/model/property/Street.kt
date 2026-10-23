package monopoly.domain.model.property

import monopoly.domain.model.Player

class Street(
    override val number: Int,
    override val name: String,
    override val price: Int,
    val houseCost: Int,
    val rent: List<Int>,
    val color: PropertyColor,
) : Property {
    override var owner: Player? = null
    override var mortgaged: Boolean = false
    var houses: Int = 0
        private set

    init {
        require(rent.size == 6)
        require(price > 0)
        require(houseCost > 0)
    }

    fun buildHouse() {
        require(!mortgaged) { "На заложенной улице нельзя строить дом." }
        require(houses < 5) { "На улице уже 5 домов." }
        houses++
    }

    fun sellHouse() {
        require(houses > 0) { "На улице нет домов." }
        houses--
    }

    fun calculateRent(): Int = rent[houses]
}
