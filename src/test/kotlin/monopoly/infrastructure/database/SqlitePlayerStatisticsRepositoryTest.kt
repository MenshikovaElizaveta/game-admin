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
                playerName = "САША",
                gamesPlayed = 2,
                wins = 1
            )
        )

        repository.save(
            PlayerStatistics(
                playerName = "САША",
                gamesPlayed = 3,
                wins = 2
            )
        )

        val result = repository.findByName("САША")

        assertNotNull(result)
        assertEquals(1, repository.findAll().size)
        assertEquals(3, result.gamesPlayed)
        assertEquals(2, result.wins)
    }

    @Test
    fun differentNameCasesHaveDifferentIds() {
        val repository = createRepository()

        repository.save(PlayerStatistics("Саша", 2, 1))
        repository.save(PlayerStatistics("Таня", 3, 2))

        assertEquals(2, repository.findAll().size)
        assertNotNull(repository.findByName("Саша"))
        assertNotNull(repository.findByName("Таня"))
    }
}
