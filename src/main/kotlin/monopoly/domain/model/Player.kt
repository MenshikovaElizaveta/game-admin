package monopoly.domain.model

import monopoly.domain.model.property.Property
import monopoly.domain.model.property.Railroad
import monopoly.domain.model.property.Street
import monopoly.domain.model.property.Utility

class Player(
    val name: String,
) {
    var balance: Int = 1500
    var position: Int = 0
    var inJail: Boolean = false
    var jailTurns: Int = 0
    var atFreeParking: Boolean = false
    var bankrupt: Boolean = false

    val properties: MutableList<Property> = mutableListOf()

    val streets: List<Street>
        get() = properties.filterIsInstance<Street>()

    val railroads: List<Railroad>
        get() = properties.filterIsInstance<Railroad>()

    val utilities: List<Utility>
        get() = properties.filterIsInstance<Utility>()

    fun addProperty(property: Property) {
        if (!properties.contains(property)) properties += property
        property.owner = this
    }

    fun removeProperty(property: Property) {
        properties.remove(property)
        if (property.owner == this) property.owner = null
    }
}
