package monopoly.domain.model

import monopoly.domain.model.property.Property
import monopoly.domain.model.property.Railroad
import monopoly.domain.model.property.Street
import monopoly.domain.model.property.Utility

class Player(
    val id: String,
    val name: String,
) {
    var balance: Int = 1500
    var position: Int = 0
    var inJail: Boolean = false
    var jailTurns: Int = 0
    var atFreeParking: Boolean = false
    var bankrupt: Boolean = false

    val properties: MutableList<Property> = mutableListOf()
    val railroads: MutableList<Railroad> = mutableListOf()
    val utilities: MutableList<Utility> = mutableListOf()

    fun addProperty(property: Property) {
        if (!properties.contains(property)) properties += property
        when (property) {
            is Railroad -> if (!railroads.contains(property)) railroads += property
            is Utility -> if (!utilities.contains(property)) utilities += property
        }
        property.owner = this
    }

    fun removeProperty(property: Property) {
        properties.remove(property)
        when (property) {
            is Railroad -> railroads.remove(property)
            is Utility -> utilities.remove(property)
        }
        if (property.owner === this) property.owner = null
    }

    fun streets(): List<Street> = properties.filterIsInstance<Street>()
}
