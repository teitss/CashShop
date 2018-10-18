package io.github.teitss.cashshop.commands

import io.github.teitss.cashshop.CashManager
import io.github.teitss.cashshop.LogRegistrar
import io.github.teitss.cashshop.localizedMessage
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import java.util.*

class CashShopCommand {

    val cashCommandSpec = CommandSpec.builder()
            .permission("cashshop.command.cashshop")
            .executor { src, _ ->
                if(src is Player) {
                    src.localizedMessage("command.cashshop.funds")
                }
                CommandResult.success()
            }
            .build()

    val commandSpec = CommandSpec.builder()
            .permission("cashshop.command.cashshop")
            .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("package"))))
            .child(cashCommandSpec, "saldo", "cash")
            .executor { src, args ->
                if(src is Player) {
                    val packID = args.getOne<String>(Text.of("package")).get()
                    val optPack = CashManager.getPackage(packID)

                    if (!optPack.isPresent) {
                        src.localizedMessage("command.cashshop.itemnotfound")
                        return@executor CommandResult.success()
                    }
                    val pack = optPack.get()
                    val playerCash = CashManager.getPlayerCash(src).orElse(0)

                    if (playerCash < pack.price) {
                        src.localizedMessage("command.cashshop.insufficientfunds")
                        return@executor CommandResult.success()
                    }

                    CashManager.removeCashFromPlayer(src, pack.price)
                    LogRegistrar.mapRecord(Date(),
                            "ENTRADA = Jogador: ${src.name} | UUID: ${src.uniqueId} | Pacote: ${pack.id} | Preço: C$${pack.price}")
                    pack.commands.forEach {
                        if(it.contains("player||")) {
                            Sponge.getCommandManager().process(src,
                                    it.split("player||")[1]
                                            .replace("%player%", src.name))
                        }
                        else {
                            Sponge.getCommandManager().process(Sponge.getServer().console,
                                    it.replace("%player%", src.name)
                            )
                        }
                    }
                    LogRegistrar.mapRecord(Date(),
                            "SAÍDA = Jogador: ${src.name} | UUID: ${src.uniqueId} | Pacote: ${pack.id} | Preço: C$${pack.price}")
                    src.localizedMessage("command.cashshop.success")

                }

                CommandResult.success()
            }
            .build()

}