package monopoly.infrastructure.file

import monopoly.domain.model.GameSummary
import monopoly.domain.model.Move
import monopoly.domain.model.MoveHistory
import monopoly.domain.model.PlayerStatistics
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FileRepositoriesIntegrationTest {
    @Test
    fun `statistics game history and move history survive repository recreation`() {
        val directory = Files.createTempDirectory("monopoly-repositories").toFile()
        val statisticsFile = directory.resolve("statistics.properties")
        val gamesFile = directory.resolve("history.properties")
        val movesDirectory = directory.resolve("moves")

        FilePlayerStatisticsRepository(statisticsFile).save(PlayerStatistics("player-1", "Саша", 4, 2))
        FileGameHistoryRepository(gamesFile).save(GameSummary("game-1", listOf("Саша", "Таня"), 17, "Саша"))
        val moves = MoveHistory("game-1")
        moves.add(Move(1, "Саша", listOf("Бросок кубиков", "Покупка недвижимости")))
        FileMoveHistoryRepository(movesDirectory).save(moves)

        val restoredStatistics = assertNotNull(FilePlayerStatisticsRepository(statisticsFile).findByPlayerId("player-1"))
        assertEquals(4, restoredStatistics.gamesPlayed)
        assertEquals(2, restoredStatistics.wins)
        assertEquals("Саша", assertNotNull(FileGameHistoryRepository(gamesFile).findById("game-1")).winnerName)
        assertEquals(
            listOf("Бросок кубиков", "Покупка недвижимости"),
            assertNotNull(FileMoveHistoryRepository(movesDirectory).findByGameId("game-1")).moves.single().descriptions,
        )
    }
}
