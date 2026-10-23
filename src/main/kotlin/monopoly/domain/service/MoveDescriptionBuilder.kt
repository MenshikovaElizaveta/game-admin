package monopoly.domain.service

class MoveDescriptionBuilder {
    private val descriptions = mutableListOf<String>()

    fun diceRoll(first: Int, second: Int) { descriptions += "Бросок кубиков: $first + $second = ${first + second}" }
    fun movedTo(cellName: String, cellNumber: Int) { descriptions += "Переход на клетку №$cellNumber «$cellName»" }
    fun passedStart() { descriptions += "Игрок прошёл клетку «старт» и получил 200 М" }
    fun cellEffect(text: String) { descriptions += text }
    fun cardDrawn(text: String) { descriptions += "Вытянута карточка: $text" }
    fun cardEffect(text: String) { descriptions += "Эффект карточки: $text" }
    fun boughtProperty(name: String, price: Int) { descriptions += "Куплена недвижимость «$name» за $price М" }
    fun builtHouse(name: String, amount: Int) { descriptions += "Построен дом на «$name» за $amount М" }
    fun soldHouse(name: String, amount: Int) { descriptions += "Продан дом на «$name», получено $amount М" }
    fun mortgaged(name: String, amount: Int) { descriptions += "Заложена недвижимость «$name», получено $amount М" }
    fun unmortgaged(name: String, amount: Int) { descriptions += "Снят залог с «$name», уплачено $amount М" }
    fun paid(amount: Int, reason: String) { descriptions += "Уплачено $amount М: $reason" }
    fun received(amount: Int, reason: String) { descriptions += "Получено $amount М: $reason" }
    fun bankruptcy(name: String) { descriptions += "Игрок $name объявил себя банкротом" }
    fun freeParking() { descriptions += "Игрок оказался на бесплатной стоянке" }
    fun jail(turn: Int) { descriptions += "Игрок находится в тюрьме. Ход в тюрьме №$turn" }
    fun result(): List<String> = descriptions.toList()
}
