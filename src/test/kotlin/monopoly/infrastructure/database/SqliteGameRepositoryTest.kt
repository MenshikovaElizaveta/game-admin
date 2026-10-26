package monopoly.infrastructure.database

import monopoly.domain.model.GameSession
import monopoly.domain.model.Move
import monopoly.domain.model.Player
import monopoly.domain.model.card.CardFactory
import monopoly.domain.model.cell.CellFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SqliteGameRepositoryTest {
    private val database = SqliteDatabase("build/test-game.db")
    private val snapshotDirectory = File("build/test-game-snapshots")
    private val games = SqliteGameRepository(
        directory = snapshotDirectory,
        database = database
    )

    @BeforeEach
    fun cleanDatabase() {
        database.connection().use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("DELETE FROM moves")
                statement.executeUpdate("DELETE FROM move_history")
                statement.executeUpdate("DELETE FROM game_history")
                statement.executeUpdate("DELETE FROM games")
                statement.executeUpdate("DELETE FROM metadata")
                statement.executeUpdate("DELETE FROM player_statistics")
                statement.executeUpdate("DELETE FROM unfinished_games")
            }
        }

        snapshotDirectory.listFiles()?.forEach { file ->
            file.delete()
        }
    }

    @Test
    fun unfinishedGameSnapshotCanBeLoadedFromSqlite() {
        val game = GameSession(
            "test-game",
            listOf(
                Player("p1", "Саша"),
                Player("p2", "Таня")
            ),
            CellFactory.createBoard(),
            CardFactory.createChanceDeck(),
            CardFactory.createCommunityChestDeck()
        )

        game.players.first().balance = 1234

        game.moveHistory.add(
            Move(
                1,
                "Саша",
                listOf("Тестовый ход")
            )
        )

        games.save(game)

        val restored = games.findById("test-game")!!

        assertEquals(
            1234,
            restored.players.first().balance
        )

        assertEquals(
            "Саша",
            restored.moveHistory.moves.single().playerName
        )

        assertEquals(
            listOf("test-game"),
            games.findAll().map { it.id }
        )

        games.delete("test-game")

        assertNull(games.findById("test-game"))
    }
}
