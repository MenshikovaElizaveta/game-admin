package monopoly.application

import monopoly.domain.model.GameSession
import monopoly.infrastructure.memory.InMemoryGameRepository
import monopoly.infrastructure.memory.InMemoryGameStatisticsRepository
import monopoly.infrastructure.memory.InMemoryGameHistoryRepository
import monopoly.infrastructure.memory.InMemoryMoveHistoryRepository
import monopoly.ui.ConsoleInput
import monopoly.ui.ConsoleOutput
import monopoly.ui.GameMenu

class GameApplication(
    private val input: ConsoleInput = ConsoleInput(),
    private val output: ConsoleOutput = ConsoleOutput()
) {
    private val gameRepository = InMemoryGameRepository()
    private val statisticsRepository = InMemoryGameStatisticsRepository()
    private val gameHistoryRepository = InMemoryGameHistoryRepository()
    private val moveHistoryRepository = InMemoryMoveHistoryRepository()

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
