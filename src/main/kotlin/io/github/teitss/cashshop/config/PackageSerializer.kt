package io.github.teitss.cashshop.config

import com.google.common.reflect.TypeToken
import ninja.leaping.configurate.ConfigurationNode

object PackageSerializer {

    fun deserialize(value: ConfigurationNode): Pair<String, CashPackage> {
        val id = value.getNode("id").string!!
        val price = value.getNode("price").int
        val commands = mutableListOf<String>().apply {
            value.getNode("commands").getList(TypeToken.of(String::class.java)).forEach {
                this.add(it)
            }
        }
        return id to CashPackage(id, price, commands)
    }

}