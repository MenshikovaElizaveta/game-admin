package monopoly.infrastructure.database

import monopoly.domain.model.GameSummary
import monopoly.domain.model.Move
import monopoly.domain.model.MoveHistory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import java.io.File

class SqliteGameHistoryRepositoryTest {
    private val databaseFile = File("build/test-game-history.db")
    private val database = SqliteDatabase(databaseFile.path)
    private val gameHistory = SqliteGameHistoryRepository(database)
    private val moveHistory = SqliteMoveHistoryRepository(database)

    @BeforeEach
    fun cleanDatabase() {
        database.connection().use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("DELETE FROM moves")
                statement.executeUpdate("DELETE FROM move_history")
                statement.executeUpdate("DELETE FROM game_history")
            }
        }
    }

    @Test
    fun gameHistoryAndMoveHistorySurviveRepositoryReads() {
        gameHistory.save(
            GameSummary(
                "test-history",
                listOf("Саша", "Таня"),
                1,
                "Таня"
            )
        )

        val history = MoveHistory("test-history")

        history.add(
            Move(
                1,
                "Саша",
                listOf(
                    "Бросил кубики",
                    "Купил улицу"
                )
            )
        )

        moveHistory.save(history)

        assertEquals(
            "Таня",
            gameHistory.findById("test-history")!!.winnerName
        )

        assertEquals(
            listOf(
                "Бросил кубики",
                "Купил улицу"
            ),
            moveHistory
                .findByGameId("test-history")!!
                .moves
                .single()
                .descriptions
        )
    }
}
