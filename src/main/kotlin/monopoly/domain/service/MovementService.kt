package monopoly.domain.service

import monopoly.domain.model.Player

object MovementService {
    fun move(player: Player, steps: Int): Boolean {
        require(steps >= 0)
        val passed = GameRules.passedStart(player.position, steps)
        player.position = (player.position + steps) % GameRules.BOARD_SIZE
        if (passed) player.balance += GameRules.PASS_START_REWARD
        return passed
    }

    fun moveTo(player: Player, destination: Int, rewardForPassingStart: Boolean = true): Boolean {
        require(destination in 0 until GameRules.BOARD_SIZE)
        val movement = (destination - player.position + GameRules.BOARD_SIZE) % GameRules.BOARD_SIZE
        if (rewardForPassingStart) return move(player, movement)
        player.position = destination
        return false
    }
}
