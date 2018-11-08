package io.github.teitss.cashshop.listeners

import io.github.teitss.cashshop.CashManager
import io.github.teitss.cashshop.CashShop
import io.github.teitss.cashshop.database.CashRepository
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.scheduler.Task
import java.util.concurrent.TimeUnit

class ClientConnectionListener {

    @Listener
    fun onConnect(e: ClientConnectionEvent.Join) {
        Task.builder()
                .name("LoadCash")
                .execute { _ ->
                    CashRepository.selectPlayerCredits(e.targetEntity).ifPresent { cash ->
                        CashManager.addPlayerToMap(e.targetEntity, cash)
                    }
                }
                .delay(3, TimeUnit.MILLISECONDS)
                .async()
                .submit(CashShop.instance)
    }

    @Listener
    fun onDisconnect(e: ClientConnectionEvent.Disconnect) {
        CashManager.removePlayerFromMap(e.targetEntity)
    }

}
