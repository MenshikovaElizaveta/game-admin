package monopoly.domain.model.cell

import monopoly.domain.model.property.PropertyColor
import monopoly.domain.model.property.Railroad
import monopoly.domain.model.property.Street
import monopoly.domain.model.property.Utility

object CellFactory {
    fun createBoard(): Board {
        val properties = mapOf(
            1 to Street(1, "Житная ул", 60, 50, listOf(2, 10, 30, 90, 160, 250), PropertyColor.BROWN),
            3 to Street(3, "Нагатинская ул", 60, 50, listOf(2, 10, 30, 90, 160, 250), PropertyColor.BROWN),
            5 to Railroad(5, "Рижская железная дорога"),
            6 to Street(6, "Варшавское шоссе", 100, 50, listOf(4, 20, 60, 180, 320, 450), PropertyColor.LIGHT_BLUE),
            8 to Street(8, "ул Огарева", 100, 50, listOf(4, 20, 60, 180, 320, 450), PropertyColor.LIGHT_BLUE),
            9 to Street(9, "Первая парковая ул", 120, 50, listOf(4, 20, 60, 180, 320, 450), PropertyColor.LIGHT_BLUE),
            11 to Street(11, "ул Полянка", 140, 100, listOf(6, 30, 90, 270, 400, 550), PropertyColor.PINK),
            12 to Utility(12, "электростанция"),
            13 to Street(13, "ул Сретенка", 140, 100, listOf(6, 30, 90, 270, 400, 550), PropertyColor.PINK),
            14 to Street(14, "Ростовская наб.", 160, 100, listOf(6, 30, 90, 270, 400, 550), PropertyColor.PINK),
            15 to Railroad(15, "Курская железная дорога"),
            16 to Street(16, "Рязанский проспект", 180, 100, listOf(8, 40, 100, 300, 450, 600), PropertyColor.ORANGE),
            18 to Street(18, "ул Вавилова", 180, 100, listOf(8, 40, 100, 300, 450, 600), PropertyColor.ORANGE),
            19 to Street(19, "Рублевское шоссе", 200, 100, listOf(8, 40, 100, 300, 450, 600), PropertyColor.ORANGE),
            21 to Street(21, "ул. Тверская", 220, 150, listOf(10, 50, 150, 450, 625, 750), PropertyColor.RED),
            23 to Street(23, "Пушкинская ул.", 220, 150, listOf(10, 50, 150, 450, 625, 750), PropertyColor.RED),
            24 to Street(24, "Площадь Маяковского", 240, 150, listOf(10, 50, 150, 450, 625, 750), PropertyColor.RED),
            25 to Railroad(25, "казанская железная дорога"),
            26 to Street(26, "ул. Грузинский вал", 260, 150, listOf(12, 60, 180, 500, 700, 900), PropertyColor.YELLOW),
            27 to Street(27, "Новинский бульвар", 260, 150, listOf(12, 60, 180, 500, 700, 900), PropertyColor.YELLOW),
            28 to Utility(28, "водопровод"),
            29 to Street(29, "Смоленская площадь", 280, 150, listOf(12, 60, 180, 500, 700, 900), PropertyColor.YELLOW),
            31 to Street(31, "ул. Щусева", 300, 200, listOf(16, 80, 220, 600, 800, 1000), PropertyColor.GREEN),
            32 to Street(32, "Гоголевский бульвар", 300, 200, listOf(16, 80, 220, 600, 800, 1000), PropertyColor.GREEN),
            34 to Street(34, "Кутузовский проспект", 320, 200, listOf(16, 80, 220, 600, 800, 1000), PropertyColor.GREEN),
            35 to Railroad(35, "Ленинградская железная дорога"),
            37 to Street(37, "ул Малая бронная", 350, 200, listOf(20, 100, 300, 750, 925, 1100), PropertyColor.BLUE),
            39 to Street(39, "ул Арбат", 400, 200, listOf(20, 100, 300, 750, 925, 1100), PropertyColor.BLUE),
        )

        val names = listOf(
            "старт", "Житная ул", "общественная казна", "Нагатинская ул",
            "Подаходный налог", "Рижская железная дорога", "Варшавское шоссе", "шанс",
            "ул Огарева", "Первая парковая ул", "Тюрьма", "ул Полянка",
            "электростанция", "ул Сретенка", "Ростовская наб.", "Курская железная дорога",
            "Рязанский проспект", "Общественная казна", "ул Вавилова", "Рублевское шоссе",
            "бесплатная стоянка", "ул. Тверская", "шанс", "Пушкинская ул.",
            "Площадь Маяковского", "казанская железная дорога", "ул. Грузинский вал",
            "Новинский бульвар", "водопровод", "Смоленская площадь",
            "Отправляйтесь в тюрьму", "ул. Щусева", "Гоголевский бульвар",
            "Общественная казна", "Кутузовский проспект", "Ленинградская железная дорога",
            "шанс", "ул Малая бронная", "сверхналог", "ул Арбат",
        )
        val types = listOf(
            CellType.START, CellType.STREET, CellType.COMMUNITY_CHEST, CellType.STREET,
            CellType.TAX, CellType.RAILROAD, CellType.STREET, CellType.CHANCE,
            CellType.STREET, CellType.STREET, CellType.JAIL, CellType.STREET,
            CellType.UTILITY, CellType.STREET, CellType.STREET, CellType.RAILROAD,
            CellType.STREET, CellType.COMMUNITY_CHEST, CellType.STREET, CellType.STREET,
            CellType.FREE_PARKING, CellType.STREET, CellType.CHANCE, CellType.STREET,
            CellType.STREET, CellType.RAILROAD, CellType.STREET, CellType.STREET,
            CellType.UTILITY, CellType.STREET, CellType.GO_TO_JAIL, CellType.STREET,
            CellType.STREET, CellType.COMMUNITY_CHEST, CellType.STREET, CellType.RAILROAD,
            CellType.CHANCE, CellType.STREET, CellType.TAX, CellType.STREET,
        )
        return Board((0..39).map { number -> Cell(number, names[number], types[number], properties[number]) })
    }
}
