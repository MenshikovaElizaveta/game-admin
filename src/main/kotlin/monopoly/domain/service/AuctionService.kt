package monopoly.domain.service

import monopoly.domain.model.Player
import monopoly.domain.model.property.Property

class AuctionService {
    fun auction(
        properties: List<Property>,
        players: List<Player> = emptyList(),
        readLine: ((String) -> String)? = null,
        onMessage: ((String) -> Unit)? = null,
    ) {
        if (properties.isEmpty()) return
        if (readLine == null) return

        properties.forEachIndexed { index, property ->
            while (true) {
                val line = readLine("Лот №${index + 1}: «${property.name}». Введите имя победителя и ставку или 0:")
                if (line.trim() == "0") {
                    property.owner = null
                    onMessage?.invoke("Лот «${property.name}» остался без владельца.")
                    break
                }
                val parts = line.trim().split(Regex("\\s+"))
                if (parts.size != 2) {
                    onMessage?.invoke("Введите: имя ставка, либо 0.")
                    continue
                }
                val player = players.firstOrNull { it.name.equals(parts[0], ignoreCase = true) }
                val bid = parts[1].toIntOrNull()
                if (player == null || player.bankrupt || bid == null || bid <= 0 || player.balance < bid) {
                    onMessage?.invoke("Некорректный игрок или ставка.")
                    continue
                }
                player.balance -= bid
                player.addProperty(property)
                onMessage?.invoke("«${property.name}» куплена игроком ${player.name} за $bid М.")
                break
            }
        }
    }

    fun auctionSingle(
        property: Property,
        players: List<Player>,
        requestBid: (Player, Property, Int) -> Int?,
        onMessage: ((String) -> Unit)? = null,
    ): Player? {
        if (property.owner != null) return property.owner
        val bidders = players.filter { !it.bankrupt && it.balance > 0 }
        if (bidders.isEmpty()) return null

        var highBid = 0
        var winner: Player? = null
        val passed = mutableSetOf<String>()
        var askedSinceLastBid = mutableSetOf<String>()

        while (true) {
            val bidder = bidders.firstOrNull {
                it.id !in passed && it.id !in askedSinceLastBid && it.balance > highBid
            } ?: break
            val bid = requestBid(bidder, property, highBid)
            if (bid == null || bid == 0) {
                passed += bidder.id
                askedSinceLastBid += bidder.id
                continue
            }
            if (bid <= highBid || bid > bidder.balance) {
                onMessage?.invoke("Ставка должна быть больше $highBid М и не превышать баланс ${bidder.balance} М.")
                continue
            }
            highBid = bid
            winner = bidder
            askedSinceLastBid = mutableSetOf(bidder.id)
        }

        if (winner != null) {
            winner.balance -= highBid
            winner.addProperty(property)
            onMessage?.invoke("«${property.name}» куплена игроком ${winner.name} за $highBid М.")
        } else {
            onMessage?.invoke("Лот «${property.name}» остался без владельца.")
        }
        return winner
    }
}
