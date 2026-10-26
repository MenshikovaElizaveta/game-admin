package monopoly.application

import monopoly.infrastructure.database.SqliteDatabase
import monopoly.infrastructure.database.SqliteGameHistoryRepository
import monopoly.infrastructure.database.SqliteGameRepository
import monopoly.infrastructure.database.SqliteMoveHistoryRepository
import monopoly.infrastructure.database.SqlitePlayerStatisticsRepository
import monopoly.infrastructure.file.FileGameRepository
import monopoly.infrastructure.file.FilePlayerStatisticsRepository
import monopoly.infrastructure.file.FileGameHistoryRepository
import monopoly.infrastructure.file.FileMoveHistoryRepository
import monopoly.ui.ConsoleInput
import monopoly.ui.ConsoleOutput
import monopoly.ui.GameMenu

class GameApplication(
    private val input: ConsoleInput = ConsoleInput(),
    private val output: ConsoleOutput = ConsoleOutput()
) {
    private val database = SqliteDatabase()
    private val gameRepository = SqliteGameRepository(database = database)
    private val statisticsRepository = SqlitePlayerStatisticsRepository(database)
    private val gameHistoryRepository = SqliteGameHistoryRepository(database)
    private val moveHistoryRepository = SqliteMoveHistoryRepository(database)

    private val menu = GameMenu(
        input,
        output,
        gameRepository,
        statisticsRepository,
        gameHistoryRepository,
        moveHistoryRepository
    )

    fun run() {
        menu.showMainMenu()
    }
}
