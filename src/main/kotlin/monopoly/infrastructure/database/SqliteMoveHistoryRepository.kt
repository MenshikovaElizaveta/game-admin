package monopoly.infrastructure.database

import monopoly.domain.model.Move
import monopoly.domain.model.MoveHistory
import monopoly.domain.repository.MoveHistoryRepository

class SqliteMoveHistoryRepository(
    private val database: SqliteDatabase = SqliteDatabase()
) : MoveHistoryRepository {

    override fun save(history: MoveHistory) {
        database.connection().use { connection ->
            connection.autoCommit = false

            try {
                connection.prepareStatement(
                    """
                    INSERT INTO move_history(game_id, move_count)
                    VALUES(?, ?)
                    ON CONFLICT(game_id)
                    DO UPDATE SET move_count = excluded.move_count
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, history.gameId)
                    statement.setInt(2, history.moveCount)
                    statement.executeUpdate()
                }

                connection.prepareStatement(
                    "DELETE FROM moves WHERE game_id = ?"
                ).use { statement ->
                    statement.setString(1, history.gameId)
                    statement.executeUpdate()
                }

                connection.prepareStatement(
                    """
                    INSERT INTO moves(
                        game_id,
                        number,
                        player_name,
                        descriptions
                    )
                    VALUES(?, ?, ?, ?)
                    """.trimIndent()
                ).use { statement ->
                    history.moves.forEach { move ->
                        statement.setString(1, history.gameId)
                        statement.setInt(2, move.number)
                        statement.setString(3, move.playerName)
                        statement.setString(
                            4,
                            move.descriptions.joinToString("\u001e")
                        )
                        statement.addBatch()
                    }

                    statement.executeBatch()
                }

                connection.commit()
            } catch (exception: Exception) {
                connection.rollback()
                throw exception
            }
        }
    }

    override fun findByGameId(gameId: String): MoveHistory? =
        database.connection().use { connection ->
            val exists = connection.prepareStatement(
                "SELECT 1 FROM move_history WHERE game_id = ?"
            ).use { statement ->
                statement.setString(1, gameId)

                statement.executeQuery().use { result ->
                    result.next()
                }
            }

            if (!exists) {
                return@use null
            }

            val history = MoveHistory(gameId)

            connection.prepareStatement(
                """
                SELECT number, player_name, descriptions
                FROM moves
                WHERE game_id = ?
                ORDER BY number
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, gameId)

                statement.executeQuery().use { result ->
                    while (result.next()) {
                        history.add(
                            Move(
                                result.getInt("number"),
                                result.getString("player_name"),
                                result.getString("descriptions")
                                    .split("\u001e")
                                    .filter(String::isNotBlank)
                            )
                        )
                    }
                }
            }

            history
        }
}
