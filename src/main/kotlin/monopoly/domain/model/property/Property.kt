package monopoly.domain.model.property

import monopoly.domain.model.Player

interface Property {
    val number: Int
    val name: String
    val price: Int
    var owner: Player?
    var mortgaged: Boolean

    val mortgageValue: Int
        get() = price / 2

    val unmortgageCost: Int
        get() = (price * 11) / 10
}
