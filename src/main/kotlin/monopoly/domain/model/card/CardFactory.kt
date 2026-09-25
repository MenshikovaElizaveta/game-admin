package monopoly.domain.model.card

object CardFactory {
    fun createChanceDeck(): Deck = CardDeck(
        listOf(
            SpecificMovementCard(1, "Отправляйтесь на Старт", 0),
            SpecificMovementCard(2, "Отправляйтесь на Бесплатную стоянку", 20),
            NearestMovementCard(3, "Идите до следующей клетки Общественной казны", listOf(2, 17, 33)),
            SpecificMovementCard(4, "Идите на ул. Арбат", 39),
            IncomeCard(5, "Банк возвращает вам 50 М", 50),
            IncomeCard(6, "Вы выиграли в лотерею 150 М", 150),
            DeductionCard(7, "Ремонт недвижимости: заплатите 100 М", 100),
            DeductionCard(8, "Заплатите 40 М на ремонт", 40),
            SpecificMovementCard(9, "Отправляйтесь в тюрьму", 30, passesStartReward = false),
            IncomeCard(10, "Получите дивиденды 50 М", 50),
        ),
    )

    fun createCommunityChestDeck(): Deck = CardDeck(
        listOf(
            SpecificMovementCard(1, "Отправляйтесь на Старт", 0),
            SpecificMovementCard(2, "Отправляйтесь в тюрьму", 30, passesStartReward = false),
            DeductionCard(3, "Заплатите страховку 10 М", 10),
            IncomeCard(4, "Получите возврат налога 100 М", 100),
            DeductionCard(5, "Заплатите 15 М за обучение", 15),
            IncomeCard(6, "Получите призовые 25 М", 25),
            DeductionCard(7, "Заплатите 20 М на благотворительность", 20),
            NearestMovementCard(8, "Идите до ближайшей железнодорожной станции", listOf(5, 15, 25, 35)),
            DeductionCard(9, "Заплатите штраф 50 М", 50),
            IncomeCard(10, "Получите 200 М", 200),
        ),
    )
}
