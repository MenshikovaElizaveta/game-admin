package monopoly.infrastructure.database

import monopoly.domain.model.PlayerStatistics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.io.File

class SqlitePlayerStatisticsRepositoryTest {
    private val databaseFile = File("build/monopoly-test.db")

    private fun createRepository(): SqlitePlayerStatisticsRepository {
        databaseFile.parentFile?.mkdirs()
        databaseFile.delete()

        return SqlitePlayerStatisticsRepository(
            SqliteDatabase(databaseFile.path)
        )
    }

    @Test
    fun identicalNamesHaveSamePlayerId() {
        val repository = createRepository()

        repository.save(
            PlayerStatistics(
                playerId = "id-1",
                playerName = "САША",
                gamesPlayed = 2
            )
        )

        repository.save(
            PlayerStatistics(
                playerId = "id-2",
                playerName = "САША",
                gamesPlayed = 3
            )
        )

        val result = repository.findByName("САША")

        assertNotNull(result)
        assertEquals("id-1", result.playerId)
        assertEquals(1, repository.findAll().size)
    }

    @Test
    fun differentNameCasesHaveDifferentIds() {
        val repository = createRepository()

        repository.save(
            PlayerStatistics(
                playerId = "id-1",
                playerName = "САША",
                gamesPlayed = 2
            )
        )

        repository.save(
            PlayerStatistics(
                playerId = "id-2",
                playerName = "сАША",
                gamesPlayed = 3
            )
        )

        val uppercase = repository.findByName("САША")
        val mixedCase = repository.findByName("сАША")

        assertNotNull(uppercase)
        assertNotNull(mixedCase)
        assertEquals("id-1", uppercase.playerId)
        assertEquals("id-2", mixedCase.playerId)
        assertEquals(2, repository.findAll().size)
    }
}
