package io.github.teitss.cashshop.config

import io.github.teitss.cashshop.CashManager
import io.github.teitss.cashshop.CashShop
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.spongepowered.api.Sponge
import java.nio.file.Files
import java.nio.file.Path
import kotlin.properties.Delegates


object CashShopConfig {

    var taskCash: Long by Delegates.notNull()
    var taskLog: Long by Delegates.notNull()
    var cashDatabaseName: String by Delegates.notNull()
    var cashDatabaseAddress: String by Delegates.notNull()
    var cashDatabaseUser: String by Delegates.notNull()
    var cashDatabasePassword: String by Delegates.notNull()
    var messagesMap = hashMapOf<String, String>()

    fun setup(path: Path, configManager: ConfigurationLoader<CommentedConfigurationNode>) {
        if (Files.notExists(path.resolve("cashshop.conf")))
            install(path)
        load(configManager)
    }

    fun load(configManager: ConfigurationLoader<CommentedConfigurationNode>) {
        val configNode = configManager.load()
        taskCash = configNode.getNode("tasks", "cashRefreshInterval").getLong()
        taskLog = configNode.getNode("tasks", " logsSaveInterval").getLong()
        cashDatabaseName = configNode.getNode("database", "databaseName").getString().orEmpty()
        cashDatabaseAddress = configNode.getNode("database", "databaseAddress").getString().orEmpty()
        cashDatabaseUser = configNode.getNode("database", "databaseUser").getString().orEmpty()
        cashDatabasePassword = configNode.getNode("database", "databasePassword").getString().orEmpty()
        CashManager.clearPackageMap()
        for (node in configNode.getNode("packages").childrenList) {
            CashManager.addPackage(PackageSerializer.deserialize(node))
        }
        messagesMap.clear()
        for (node in configNode.getNode("messages").childrenMap) {
            messagesMap.put(node.key.toString(), node.value.string!!)
        }
        CashShop.instance.logger.info("Configuration successfully loaded.")
    }

    fun install(path: Path) {
        val configFile = Sponge.getAssetManager().getAsset(CashShop.instance, "cashshop.conf").get()
        configFile.copyToDirectory(path)
        CashShop.instance.logger.info("Configuration successfully installed.")
    }

}