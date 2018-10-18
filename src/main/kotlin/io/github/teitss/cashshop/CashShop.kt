package io.github.teitss.cashshop

import com.google.inject.Inject
import io.github.teitss.cashshop.commands.CashShopCommand
import io.github.teitss.cashshop.config.CashShopConfig
import io.github.teitss.cashshop.database.CashDAO
import io.github.teitss.cashshop.listeners.ClientConnectionListener
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GameStartingServerEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.scheduler.Task
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit

@Plugin(
        id="cashshop",
        name="CashShop",
        version="1.0.0",
        description = "A plugin to sell packages for special credits.",
        authors= arrayOf("Teits / Discord Teits#7663")
)
class CashShop @Inject constructor(
        val logger: Logger,
        val pluginContainer: PluginContainer,
        @ConfigDir(sharedRoot = false) val configDir: Path) {

    companion object {
        lateinit var instance: CashShop
    }

    lateinit var configManager: ConfigurationLoader<CommentedConfigurationNode>

    @Listener
    fun onInit(event: GameInitializationEvent) {
        instance = this
        logger.info("Checking configuration file...")
        configManager = HoconConfigurationLoader.builder().setPath(configDir.resolve("${this.pluginContainer.id}.conf")).build()
        CashShopConfig.setup(configDir, configManager)
        logger.info("Registering event listeners...")
        Sponge.getEventManager().registerListeners(this, ClientConnectionListener())
    }

    @Listener
    fun onServerStart(event: GameStartingServerEvent) {
        logger.info("Configuring logs directory...")
        File(configDir.toString() + "/logs").mkdirs()
        logger.info("Connecting to database...")
        CashDAO.setupDatabase()
        logger.info("Registering commands...")
        Sponge.getCommandManager().register(this, CashShopCommand().commandSpec, "cashshop", "donationshop")
        logger.info("Initializing scheduled tasks...")
        initTasks().forEach {
            it.submit(this)
        }
    }

    @Listener
    fun onGameReload(e: GameReloadEvent) {
        logger.info("Reloading plugin configuration...")
        CashShopConfig.load(configManager)
        logger.info("Restarting scheduled tasks")
        Sponge.getScheduler().getScheduledTasks(this).forEach { it.cancel() }
        initTasks().forEach {
            it.submit(this)
        }
    }

    private fun initTasks(): List<Task.Builder> {
        return mutableListOf<Task.Builder>().also {
            if(CashShopConfig.taskLog > 0)
                it.add(Task.builder()
                        .name("LogSaveTask")
                        .async()
                        .execute { _ -> LogRegistrar.registerRecords() }
                        .delay(CashShopConfig.taskLog, TimeUnit.SECONDS)
                        .interval(CashShopConfig.taskLog, TimeUnit.SECONDS))
            if(CashShopConfig.taskCash > 0)
                it.add(Task.builder()
                        .name("CashRefreshTask")
                        .async()
                        .execute { _ -> CashManager.refreshOnlinePlayersCash() }
                        .delay(CashShopConfig.taskCash, TimeUnit.SECONDS)
                        .interval(CashShopConfig.taskCash, TimeUnit.SECONDS))
        }

    }

}