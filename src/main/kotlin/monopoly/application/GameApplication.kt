package monopoly.application

import monopoly.infrastructure.database.*
import monopoly.ui.ConsoleInput
import monopoly.ui.ConsoleOutput
import monopoly.ui.GameMenu
import monopoly.ui.gui.MainWindow
import javax.swing.SwingUtilities

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

    fun run(args: Array<String> = emptyArray()) {
        if (args.contains("gui")) {
            SwingUtilities.invokeLater {
                MainWindow().isVisible = true
            }
        } else {
            menu.showMainMenu()
        }
    }
}
