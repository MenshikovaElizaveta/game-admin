package monopoly.domain.service

import monopoly.domain.model.Move
import monopoly.domain.model.Player

class MoveService {
    fun createMove(number: Int, player: Player, builder: MoveDescriptionBuilder): Move =
        Move(number, player.name, builder.result())
}
