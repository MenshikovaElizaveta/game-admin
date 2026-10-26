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
                update(connection, existing.playerId, statistics)
            }
        }
    }

    override fun findByPlayerId(playerId: String): PlayerStatistics? =
        database.connection().use { connection ->
            connection.prepareStatement(
                """
                SELECT player_id, player_name, games_played
                FROM player_statistics
                WHERE player_id = ?
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, playerId)

                statement.executeQuery().use { result ->
                    if (result.next()) {
                        PlayerStatistics(
                            playerId = result.getString("player_id"),
                            playerName = result.getString("player_name"),
                            gamesPlayed = result.getInt("games_played")
                        )
                    } else {
                        null
                    }
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
                SELECT player_id, player_name, games_played
                FROM player_statistics
                ORDER BY player_name COLLATE BINARY
                """.trimIndent()
            ).use { statement ->
                statement.executeQuery().use { result ->
                    val statistics = mutableListOf<PlayerStatistics>()

                    while (result.next()) {
                        statistics += PlayerStatistics(
                            playerId = result.getString("player_id"),
                            playerName = result.getString("player_name"),
                            gamesPlayed = result.getInt("games_played")
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
            SELECT player_id, player_name, games_played
            FROM player_statistics
            WHERE player_name = ? COLLATE BINARY
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, name)

            statement.executeQuery().use { result ->
                if (result.next()) {
                    PlayerStatistics(
                        playerId = result.getString("player_id"),
                        playerName = result.getString("player_name"),
                        gamesPlayed = result.getInt("games_played")
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
                player_id,
                player_name,
                games_played
            )
            VALUES(?, ?, ?)
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, statistics.playerId)
            statement.setString(2, statistics.playerName)
            statement.setInt(3, statistics.gamesPlayed)
            statement.executeUpdate()
        }
    }

    private fun update(
        connection: Connection,
        playerId: String,
        statistics: PlayerStatistics
    ) {
        connection.prepareStatement(
            """
            UPDATE player_statistics
            SET games_played = ?
            WHERE player_id = ?
            """.trimIndent()
        ).use { statement ->
            statement.setInt(1, statistics.gamesPlayed)
            statement.setString(2, playerId)
            statement.executeUpdate()
        }
    }
}
