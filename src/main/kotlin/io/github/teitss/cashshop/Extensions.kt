package io.github.teitss.cashshop

import io.github.teitss.cashshop.config.CashShopConfig
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.channel.MessageReceiver
import org.spongepowered.api.text.chat.ChatType
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.serializer.TextSerializers

fun MessageReceiver.localizedMessage(msg: String, chatType: ChatType = ChatTypes.CHAT) {
    if (this is Player) {
        var message = CashShopConfig.messagesMap.get(msg)!!
        if(msg == "command.cashshop.funds") {
            message = message.replace("%funds%", CashManager.getPlayerCash(this).orElse(0).toString())
        }
        this.sendMessage(chatType, TextSerializers.formattingCode('&')
                .deserialize(message))
    }

    else
        this.sendMessage(TextSerializers.formattingCode('&')
                .deserialize(CashShopConfig.messagesMap.get(msg)!!))
}