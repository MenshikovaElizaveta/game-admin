package monopoly.infrastructure.database

import monopoly.domain.model.PlayerStatistics
import monopoly.domain.repository.PlayerStatisticsRepository
import java.sql.Connection

class SqlitePlayerStatisticsRepository(
    private val database: SqliteDatabase = SqliteDatabase()
) : PlayerStatisticsRepository {

    override fun save(statistics: PlayerStatistics) {
        database.connection().use { connection ->
            val existing = findByName(connection, statistics.playerName)

            if (existing == null) {
                insert(connection, statistics)
            } else {
                update(connection, statistics)
            }
        }
    }

    override fun findByName(name: String): PlayerStatistics? =
        database.connection().use { connection ->
            findByName(connection, name)
        }

    override fun findAll(): List<PlayerStatistics> =
        database.connection().use { connection ->
            connection.prepareStatement(
                """
                SELECT player_name, games_played, games_won
                FROM player_statistics
                ORDER BY player_name
                """.trimIndent()
            ).use { statement ->
                statement.executeQuery().use { result ->
                    val statistics = mutableListOf<PlayerStatistics>()

                    while (result.next()) {
                        statistics += PlayerStatistics(
                            playerName = result.getString("player_name"),
                            gamesPlayed = result.getInt("games_played"),
                            wins = result.getInt("games_won")
                        )
                    }

                    statistics
                }
            }
        }

    private fun findByName(
        connection: Connection,
        name: String
    ): PlayerStatistics? =
        connection.prepareStatement(
            """
            SELECT player_name, games_played, games_won
            FROM player_statistics
            WHERE player_name = ?
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, name)

            statement.executeQuery().use { result ->
                if (result.next()) {
                    PlayerStatistics(
                        playerName = result.getString("player_name"),
                        gamesPlayed = result.getInt("games_played"),
                        wins = result.getInt("games_won")
                    )
                } else {
                    null
                }
            }
        }

    private fun insert(
        connection: Connection,
        statistics: PlayerStatistics
    ) {
        connection.prepareStatement(
            """
            INSERT INTO player_statistics(
                player_name,
                games_played,
                games_won
            )
            VALUES(?, ?, ?)
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, statistics.playerName)
            statement.setInt(2, statistics.gamesPlayed)
            statement.setInt(3, statistics.wins)
            statement.executeUpdate()
        }
    }

    private fun update(
        connection: Connection,
        statistics: PlayerStatistics
    ) {
        connection.prepareStatement(
            """
            UPDATE player_statistics
            SET games_played = ?,
                games_won = ?
            WHERE player_name = ?
            """.trimIndent()
        ).use { statement ->
            statement.setInt(1, statistics.gamesPlayed)
            statement.setInt(2, statistics.wins)
            statement.setString(3, statistics.playerName)
            statement.executeUpdate()
        }
    }
}
