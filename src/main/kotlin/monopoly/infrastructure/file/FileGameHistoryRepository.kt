package monopoly.infrastructure.file

import monopoly.domain.model.GameSummary
import monopoly.domain.repository.GameHistoryRepository
import java.io.File
import java.util.Properties

class FileGameHistoryRepository(private val file: File = File("data/game-history.properties")) : GameHistoryRepository {
    private fun load() = Properties().also { if (file.exists()) file.inputStream().use(it::load) }
    override fun save(summary: GameSummary) {
        val p = load(); val prefix = "game.${summary.gameId}."
        p.setProperty(prefix + "players", summary.playerNames.joinToString("|"))
        p.setProperty(prefix + "moves", summary.moveCount.toString()); p.setProperty(prefix + "winner", summary.winnerName)
        file.parentFile?.mkdirs(); file.outputStream().use { p.store(it, "История игр") }
    }
    override fun findAll(): List<GameSummary> = load().stringPropertyNames().filter { it.startsWith("game.") && it.endsWith(".players") }.mapNotNull { key ->
        val id = key.removePrefix("game.").removeSuffix(".players"); findById(id)
    }.sortedBy { it.gameId.toIntOrNull() ?: Int.MAX_VALUE }
    override fun findById(gameId: String): GameSummary? = load().let { p ->
        val prefix = "game.$gameId."; val players = p.getProperty(prefix + "players") ?: return null
        GameSummary(gameId, players.split("|").filter { it.isNotBlank() }, p.getProperty(prefix + "moves", "0").toInt(), p.getProperty(prefix + "winner", "—"))
    }
}
