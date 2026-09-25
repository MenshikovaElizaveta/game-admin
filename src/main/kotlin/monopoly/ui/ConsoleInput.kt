package monopoly.ui

class ConsoleInput {
    private val reader = System.`in`.bufferedReader()

    fun readLine(): String {
        return reader.readLine() ?: ""
    }
}
