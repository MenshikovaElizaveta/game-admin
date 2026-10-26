package monopoly.infrastructure.database

import monopoly.domain.model.GameSummary
import monopoly.domain.repository.GameHistoryRepository

class SqliteGameHistoryRepository(
    private val database: SqliteDatabase = SqliteDatabase()
) : GameHistoryRepository {

    override fun save(summary: GameSummary) {
        database.connection().use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO game_history(
                    game_id,
                    player_names,
                    move_count,
                    winner_name
                )
                VALUES(?, ?, ?, ?)
                ON CONFLICT(game_id)
                DO UPDATE SET
                    player_names = excluded.player_names,
                    move_count = excluded.move_count,
                    winner_name = excluded.winner_name
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, summary.gameId)
                statement.setString(2, summary.playerNames.joinToString("\u001f"))
                statement.setInt(3, summary.moveCount)
                statement.setString(4, summary.winnerName)
                statement.executeUpdate()
            }
        }
    }

    override fun findAll(): List<GameSummary> =
        database.connection().use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(
                    """
                    SELECT game_id, player_names, move_count, winner_name
                    FROM game_history
                    ORDER BY game_id
                    """.trimIndent()
                ).use { result ->
                    val summaries = mutableListOf<GameSummary>()

                    while (result.next()) {
                        summaries += GameSummary(
                            result.getString("game_id"),
                            result.getString("player_names")
                                .split("\u001f")
                                .filter(String::isNotBlank),
                            result.getInt("move_count"),
                            result.getString("winner_name")
                        )
                    }

                    summaries
                }
            }
        }

    override fun findById(gameId: String): GameSummary? =
        findAll().firstOrNull { summary -> summary.gameId == gameId }
}
