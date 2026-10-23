package monopoly.system

import kotlin.test.Test
import kotlin.test.assertEquals
import monopoly.domain.model.GameSession
import monopoly.domain.model.Move
import monopoly.domain.model.Player
import monopoly.domain.model.PlayerStatistics
import monopoly.domain.model.card.CardFactory
import monopoly.domain.model.cell.CellFactory
import monopoly.domain.service.MovementService
import monopoly.infrastructure.memory.InMemoryGameHistoryRepository
import monopoly.infrastructure.memory.InMemoryGameRepository
import monopoly.infrastructure.memory.InMemoryGameStatisticsRepository
import monopoly.infrastructure.memory.InMemoryMoveHistoryRepository
import monopoly.domain.model.GameSummary

class GameSystemTest {
    @Test
    fun unfinishedGameScenarioCanBeSavedThenContinued() {
        val repository = InMemoryGameRepository()
        val moves = InMemoryMoveHistoryRepository()
        val sasha = Player("id-sasha", "Саша")
        val game = newGame(sasha)

        MovementService.move(sasha, 4)
        game.moveHistory.add(Move(1, "Саша", listOf("Первый ход")))
        repository.save(game)
        moves.save(game.moveHistory)

        val restored = repository.findById(game.id)!!
        restored.currentPlayer
        MovementService.move(restored.currentPlayer, 2)
        restored.moveHistory.add(Move(2, restored.currentPlayer.name, listOf("Продолжение игры")))
        moves.save(restored.moveHistory)

        assertEquals(2, moves.findByGameId(game.id)!!.moveCount)
        assertEquals(6, restored.players.first().position)
    }

    @Test
    fun finishedGameScenarioStoresStatisticsAndBothHistories() {
        val gameRepository = InMemoryGameRepository()
        val statisticsRepository = InMemoryGameStatisticsRepository()
        val gameHistoryRepository = InMemoryGameHistoryRepository()
        val moveHistoryRepository = InMemoryMoveHistoryRepository()
        val sasha = Player("id-sasha", "Саша")
        val tanya = Player("id-tanya", "Таня")
        val game = newGame(sasha, tanya)

        game.moveHistory.add(Move(1, "Саша", listOf("Первый ход")))
        sasha.bankrupt = true
        val winner = game.winner()!!

        statisticsRepository.save(PlayerStatistics(sasha.id, sasha.name, 1, 0))
        statisticsRepository.save(PlayerStatistics(tanya.id, tanya.name, 1, 1))
        gameHistoryRepository.save(GameSummary(game.id, game.players.map { it.name }, game.moveHistory.moveCount, winner.name))
        moveHistoryRepository.save(game.moveHistory)
        gameRepository.save(game)
        gameRepository.delete(game.id)

        assertEquals(1, statisticsRepository.findByPlayerId(tanya.id)!!.wins)
        assertEquals(tanya.name, gameHistoryRepository.findById(game.id)!!.winnerName)
        assertEquals(1, moveHistoryRepository.findByGameId(game.id)!!.moveCount)
        assertEquals(null, gameRepository.findById(game.id))
    }

    private fun newGame(first: Player, second: Player = Player("id-tanya", "Таня")): GameSession = GameSession(
        "game-1",
        listOf(first, second),
        CellFactory.createBoard(),
        CardFactory.createChanceDeck(),
        CardFactory.createCommunityChestDeck(),
    )
}
