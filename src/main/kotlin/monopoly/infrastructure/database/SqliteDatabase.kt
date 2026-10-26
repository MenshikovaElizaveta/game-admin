package monopoly.infrastructure.database

import java.io.File
import java.sql.Connection
import java.sql.DriverManager

class SqliteDatabase(
    private val databasePath: String = "data/monopoly.db"
) {
    init {
        Class.forName("org.sqlite.JDBC")
        File(databasePath).parentFile?.mkdirs()
    }

    fun connection(): Connection {
        val connection = DriverManager.getConnection("jdbc:sqlite:$databasePath")

        try {
            connection.createStatement().use { statement ->
                statement.execute("PRAGMA foreign_keys = ON")
            }
            createTables(connection)
            return connection
        } catch (exception: Exception) {
            connection.close()
            throw exception
        }
    }

    private fun createTables(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS player_statistics (
                    player_id TEXT PRIMARY KEY,
                    player_name TEXT NOT NULL COLLATE BINARY UNIQUE,
                    games_played INTEGER NOT NULL DEFAULT 0,
                    games_won INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS metadata (
                    key TEXT PRIMARY KEY,
                    value INTEGER NOT NULL
                )
                """.trimIndent()
            )

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS games (
                    id TEXT PRIMARY KEY,
                    payload BLOB NOT NULL
                )
                """.trimIndent()
            )

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS game_history (
                    game_id TEXT PRIMARY KEY,
                    player_names TEXT NOT NULL,
                    move_count INTEGER NOT NULL,
                    winner_name TEXT NOT NULL
                )
                """.trimIndent()
            )

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS move_history (
                    game_id TEXT PRIMARY KEY,
                    move_count INTEGER NOT NULL
                )
                """.trimIndent()
            )

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS moves (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    game_id TEXT NOT NULL,
                    number INTEGER NOT NULL,
                    player_name TEXT NOT NULL,
                    descriptions TEXT NOT NULL,
                    FOREIGN KEY (game_id)
                        REFERENCES move_history(game_id)
                        ON DELETE CASCADE
                )
                """.trimIndent()
            )

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS unfinished_games (
                    game_id TEXT PRIMARY KEY,
                    game_data BLOB NOT NULL,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """.trimIndent()
            )
        }
    }
}
