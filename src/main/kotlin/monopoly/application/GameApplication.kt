package monopoly.application

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
    private val gameRepository = FileGameRepository()
    private val statisticsRepository = FilePlayerStatisticsRepository()
    private val gameHistoryRepository = FileGameHistoryRepository()
    private val moveHistoryRepository = FileMoveHistoryRepository()

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
