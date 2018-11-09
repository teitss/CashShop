package io.github.teitss.cashshop

import io.github.teitss.cashshop.config.CashPackage
import io.github.teitss.cashshop.database.CashRepository
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.entity.living.player.User
import org.spongepowered.api.scheduler.Task
import java.util.*
import java.util.concurrent.TimeUnit

object CashManager {

    // Map caching players credits.
    private val cashMap = hashMapOf<UUID, Int>()
    //Map caching configured packages.
    private val packageMap = hashMapOf<String, CashPackage>()

    fun clearPackageMap() {
        packageMap.clear()
    }

    fun removePlayerFromMap(player: Player) {
        cashMap.remove(player.uniqueId)
    }

    fun addPlayerToMap(player: Player, cash: Int) {
        cashMap.put(player.uniqueId, cash)
    }

    fun getPackage(id: String): Optional<CashPackage> {
        val pack = packageMap[id]
        return if (pack != null) Optional.of(pack) else Optional.empty()
    }

    fun addPackage(pair: Pair<String, CashPackage>) {
        packageMap.put(pair.first , pair.second)
    }

    fun getPlayerCash(player: Player): Optional<Int> {
        val cash = cashMap[player.uniqueId]
        return if (cash != null) Optional.of(cash) else Optional.empty()
    }

    fun removeCashFromPlayer(player: Player, value: Int) {
        val cash = cashMap[player.uniqueId]!! - value
        cashMap.replace(player.uniqueId, cash)
        save(player, cash)
    }

    fun addCashToPlayer(player: User, value: Int) {
        if(cashMap.containsKey(player.uniqueId)) {
            val cash = cashMap[player.uniqueId]!! + value
            cashMap.replace(player.uniqueId, cash)
            save(player, cash)
        } else {
            if (Sponge.getServer().getPlayer(player.uniqueId).isPresent)
                cashMap[player.uniqueId] = value
            Task.builder()
                    .name("CashAddTask")
                    .execute { _ ->
                        val dbCash = CashRepository.selectPlayerCredits(player).orElse(0)
                        CashRepository.updatePlayerCredits(player, dbCash + value)
                    }
                    .delay(3, TimeUnit.MILLISECONDS)
                    .async()
                    .submit(CashShop.instance)
        }

    }

    fun save(player: User, cash: Int) {
        Task.builder()
                .name("CashSaveTask")
                .execute { _ ->
                    CashRepository.updatePlayerCredits(player, cash)
                }
                .delay(3, TimeUnit.MILLISECONDS)
                .async()
                .submit(CashShop.instance)
    }

    //ONLY USE THIS inside a async task
    fun refreshPlayerCash(player: Player) {
        val cash = CashRepository.selectPlayerCredits(player).get()
        cashMap.replace(player.uniqueId, cash)
    }

    //ONLY USE THIS inside a async task
    fun refreshOnlinePlayersCash() {
        Sponge.getServer().onlinePlayers.forEach {player ->
            CashRepository.selectPlayerCredits(player).ifPresent {
                cashMap[player.uniqueId] = it
            }
        }
        CashShop.instance.logger.info("Os créditos dos jogadores foram atualizados.")
    }

}
