package monopoly.domain.model.cell

import monopoly.domain.model.property.Property

class Cell(
    val number: Int,
    val name: String,
    val type: CellType,
    val property: Property?,
) {
    init {
        require(number in 0..39)
        if (type in setOf(CellType.STREET, CellType.RAILROAD, CellType.UTILITY)) {
            require(property != null)
            require(property.number == number)
            require(property.name == name)
        } else {
            require(property == null)
        }
    }
}
