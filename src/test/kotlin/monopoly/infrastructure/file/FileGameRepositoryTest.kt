package monopoly.infrastructure.file

import monopoly.domain.model.GameSession
import monopoly.domain.model.Player
import monopoly.domain.model.Move
import monopoly.domain.model.card.CardFactory
import monopoly.domain.model.cell.CellFactory
import monopoly.domain.model.property.Street
import monopoly.domain.service.PropertyService
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FileGameRepositoryTest {
    @Test
    fun `game state survives repository recreation`() {
        val directory = Files.createTempDirectory("games-test").toFile()
        val repository = FileGameRepository(directory)
        val players = listOf(Player("p1", "Саша"), Player("p2", "Таня"))
        val board = CellFactory.createBoard()
        val game = GameSession("test-game", players, board, CardFactory.createChanceDeck(), CardFactory.createCommunityChestDeck())
        players[0].balance = 1234
        players[0].position = 24
        val property = board.getCell(24).property as Street
        PropertyService.buy(players[0], property)
        property.buildHouse()
        property.mortgaged = false
        val chanceOrder = listOf(2, 1, 3, 4, 5, 6, 7, 8, 9, 10)
        game.chanceDeck.reorder(chanceOrder)
        game.chanceDeck.draw()
        game.turnInProgress = true
        game.moveToNextActivePlayer()
        game.turnInProgress = true
        game.moveHistory.add(Move(1, "Саша", listOf("Проверочный ход")))
        repository.save(game)

        val restored = assertNotNull(FileGameRepository(directory).findById("test-game"))
        assertEquals("Таня", restored.currentPlayer.name)
        assertEquals(true, restored.turnInProgress)
        assertEquals(994, restored.players[0].balance)
        assertEquals(24, restored.players[0].position)
        assertEquals("Саша", restored.board.getCell(24).property?.owner?.name)
        assertEquals(1, (restored.board.getCell(24).property as Street).houses)
        assertEquals(1, restored.chanceDeck.currentIndex)
        assertEquals(chanceOrder, restored.chanceDeck.cards.map { it.number })
        assertEquals("Проверочный ход", restored.moveHistory.moves.single().descriptions.single())
    }

    @Test
    fun `game numbers continue after repository recreation`() {
        val directory = Files.createTempDirectory("game-numbers-test").toFile()
        val repository = FileGameRepository(directory)

        assertEquals("1", repository.nextId())
        assertEquals("2", repository.nextId())
        assertEquals("3", FileGameRepository(directory).nextId())
    }
}
