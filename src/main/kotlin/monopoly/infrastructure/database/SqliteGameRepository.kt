package monopoly.infrastructure.database

import monopoly.domain.model.GameSession
import monopoly.domain.repository.GameRepository
import monopoly.infrastructure.file.FileGameRepository
import java.io.File

class SqliteGameRepository(
    private val directory: File = File("data/game-snapshots"),
    private val database: SqliteDatabase = SqliteDatabase()
) : GameRepository {

    override fun nextId(): String =
        database.connection().use { connection ->
            connection.autoCommit = false

            try {
                val saved = connection.prepareStatement(
                    "SELECT value FROM metadata WHERE key = 'next_game_id'"
                ).use { statement ->
                    statement.executeQuery().use { result ->
                        if (result.next()) result.getInt(1) else 1
                    }
                }

                val maxExisting = connection.createStatement().use { statement ->
                    statement.executeQuery(
                        """
                        SELECT COALESCE(MAX(CAST(id AS INTEGER)), 0) + 1
                        FROM games
                        """.trimIndent()
                    ).use { result ->
                        result.next()
                        result.getInt(1)
                    }
                }

                val next = maxOf(saved, maxExisting)

                connection.prepareStatement(
                    """
                    INSERT INTO metadata(key, value)
                    VALUES('next_game_id', ?)
                    ON CONFLICT(key)
                    DO UPDATE SET value = excluded.value
                    """.trimIndent()
                ).use { statement ->
                    statement.setInt(1, next + 1)
                    statement.executeUpdate()
                }

                connection.commit()
                next.toString()
            } catch (exception: Exception) {
                connection.rollback()
                throw exception
            }
        }

    override fun save(game: GameSession) {
        directory.mkdirs()
        FileGameRepository(directory).save(game)

        val snapshot = File(directory, "${game.id}.properties").readBytes()

        database.connection().use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO games(id, payload)
                VALUES(?, ?)
                ON CONFLICT(id)
                DO UPDATE SET payload = excluded.payload
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, game.id)
                statement.setBytes(2, snapshot)
                statement.executeUpdate()
            }
        }
    }

    override fun findById(id: String): GameSession? {
        val bytes = database.connection().use { connection ->
            connection.prepareStatement(
                "SELECT payload FROM games WHERE id = ?"
            ).use { statement ->
                statement.setString(1, id)

                statement.executeQuery().use { result ->
                    if (result.next()) result.getBytes(1) else null
                }
            }
        } ?: return null

        directory.mkdirs()
        File(directory, "$id.properties").writeBytes(bytes)

        return FileGameRepository(directory).findById(id)
    }

    override fun findAll(): List<GameSession> {
        val ids = database.connection().use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(
                    "SELECT id FROM games ORDER BY CAST(id AS INTEGER)"
                ).use { result ->
                    val values = mutableListOf<String>()

                    while (result.next()) {
                        values += result.getString(1)
                    }

                    values
                }
            }
        }

        return ids.mapNotNull { id -> findById(id) }
    }

    override fun delete(id: String) {
        database.connection().use { connection ->
            connection.prepareStatement(
                "DELETE FROM games WHERE id = ?"
            ).use { statement ->
                statement.setString(1, id)
                statement.executeUpdate()
            }
        }

        File(directory, "$id.properties").delete()
    }
}
