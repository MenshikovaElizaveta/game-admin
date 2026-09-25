package monopoly.integration

import kotlin.test.Test
import kotlin.test.assertEquals
import monopoly.domain.model.GameSession
import monopoly.domain.model.Player
import monopoly.domain.model.PlayerStatistics
import monopoly.domain.model.card.CardFactory
import monopoly.domain.model.cell.CellFactory
import monopoly.domain.model.property.Railroad
import monopoly.domain.repository.PlayerStatisticsRepository
import monopoly.domain.service.PropertyService
import monopoly.infrastructure.memory.InMemoryGameRepository
import monopoly.infrastructure.memory.InMemoryMoveHistoryRepository

class GameIntegrationTest {
    @Test
    fun buyingPropertyConnectsPlayerAndBoardProperty() {
        val player = Player("1", "Саша")
        val board = CellFactory.createBoard()
        val property = board.getCell(1).property!!

        PropertyService.buy(player, property)

        assertEquals(player, property.owner)
        assertEquals(property, player.properties.single())
        assertEquals(property, board.getCell(1).property)
    }

    @Test
    fun railroadRentDependsOnNumberOfOwnedRailroads() {
        val sasha = Player("1", "Саша")
        val game = newGame(sasha)
        val first = game.board.getCell(5).property as Railroad
        val second = game.board.getCell(15).property as Railroad

        sasha.addProperty(first)
        sasha.addProperty(second)

        assertEquals(50, first.calculateRent())
    }

    @Test
    fun unfinishedGameCanBeSavedAndFoundById() {
        val repository = InMemoryGameRepository()
        val game = newGame(Player("1", "Саша"))
        repository.save(game)

        assertEquals(game, repository.findById(game.id))
        assertEquals(listOf(game), repository.findAll())
    }

    private fun newGame(first: Player): GameSession = GameSession(
        "game-1",
        listOf(first, Player("2", "Таня")),
        CellFactory.createBoard(),
        CardFactory.createChanceDeck(),
        CardFactory.createCommunityChestDeck(),
    )
}
