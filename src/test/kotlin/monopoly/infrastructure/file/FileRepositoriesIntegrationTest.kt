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

        FilePlayerStatisticsRepository(
            statisticsFile).save(PlayerStatistics("Саша", 4, 2)
        )

        FileGameHistoryRepository(gamesFile).save(
            GameSummary(1, listOf("Саша", "Таня"), 17, "Саша")
        )

        val moves = MoveHistory(1)
        moves.add(Move(
            1,
            "Саша",
            listOf("Бросок кубиков", "Покупка недвижимости")))
        FileMoveHistoryRepository(movesDirectory).save(moves)

        val restoredStatistics = assertNotNull(
            FilePlayerStatisticsRepository(statisticsFile).findByName("Саша")
        )
        assertEquals(4, restoredStatistics.gamesPlayed)
        assertEquals(2, restoredStatistics.wins)

        assertEquals(
            "Саша",
            FileGameHistoryRepository(gamesFile)
                .findAll()
                .single { it.gameId == 1 }
                .winnerName
        )

        assertEquals(
            listOf("Бросок кубиков", "Покупка недвижимости"),
            assertNotNull(FileMoveHistoryRepository(movesDirectory).findByGameId(1))
                .moves.single().descriptions
        )
    }
}
