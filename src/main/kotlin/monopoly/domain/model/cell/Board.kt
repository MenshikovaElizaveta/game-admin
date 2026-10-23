package monopoly.domain.model.cell

class Board(cells: List<Cell>) {
    val cells: List<Cell> = cells.toList()

    init {
        require(this.cells.size == 40)
        require(this.cells.map { it.number } == (0..39).toList())
    }

    fun getCell(number: Int): Cell = cells[number]
}
