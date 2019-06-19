package io.github.teitss.cashshop

import me.rojo8399.placeholderapi.Placeholder
import me.rojo8399.placeholderapi.Source
import me.rojo8399.placeholderapi.Token
import org.spongepowered.api.entity.living.player.Player

class Placeholders {

    @Placeholder(id = "cashshop")
    fun amount(@Source player: Player, @Token tokens: String): Any {
        val playerCash = CashManager.getPlayerCash(player).orElse(0)
        val parsedTokens = tokens.split("_")
        when (parsedTokens[0]) {
            "amount" -> return playerCash
            else -> return "0"
        }
    }

}