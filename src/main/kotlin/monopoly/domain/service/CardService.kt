package monopoly.domain.service

import monopoly.domain.model.Player
import monopoly.domain.model.card.*

data class CardResult(
    val card: Card,
    val passedStart: Boolean = false,
    val movedToJail: Boolean = false,
)

object CardService {
    fun drawAndApply(player: Player, deck: Deck): CardResult {
        return when (val card = deck.draw()) {
            is IncomeCard -> {
                player.balance += card.amount
                CardResult(card)
            }
            is DeductionCard -> CardResult(card)
            is SpecificMovementCard -> {
                if (card.destination == 30) {
                    player.position = 10
                    player.inJail = true
                    player.jailTurns = 0
                    CardResult(card, movedToJail = true)
                } else {
                    CardResult(
                        card,
                        passedStart = MovementService.moveTo(player, card.destination, card.passesStartReward),
                    )
                }
            }
            is NearestMovementCard -> {
                val destination = card.destinations
                    .sorted()
                    .firstOrNull { it > player.position }
                    ?: card.destinations.minOrNull()
                    ?: error("У карточки не указаны возможные клетки")
                CardResult(
                    card,
                    passedStart = MovementService.moveTo(player, destination, card.passesStartReward),
                )
            }
            else -> error("Неизвестный тип карточки")
        }
    }
}
