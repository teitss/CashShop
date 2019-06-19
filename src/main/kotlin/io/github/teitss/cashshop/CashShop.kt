package io.github.teitss.cashshop

import com.google.inject.Inject
import io.github.teitss.cashshop.commands.CashShopCommand
import io.github.teitss.cashshop.config.CashShopConfig
import io.github.teitss.cashshop.database.CashRepository
import io.github.teitss.cashshop.listeners.ClientConnectionListener
import me.rojo8399.placeholderapi.PlaceholderService
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GameStartedServerEvent
import org.spongepowered.api.event.game.state.GameStartingServerEvent
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.scheduler.Task
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

@Plugin(
        id="cashshop",
        name="CashShop",
        version = "@pluginVersion@",
        description = "A plugin to sell packages for special credits.",
        authors = ["Teits / Discord Teits#7663"],
        dependencies = [Dependency(id = "placeholderapi")]
)
class CashShop @Inject constructor(
        val logger: Logger,
        val pluginContainer: PluginContainer,
        @ConfigDir(sharedRoot = false) val configDir: Path) {

    companion object {
        var instance: CashShop by Delegates.notNull()
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
        logger.info("Registering commands...")
        Sponge.getCommandManager().register(this, CashShopCommand().commandSpec, "cashshop", "donationshop")
        logger.info("Initializing scheduled tasks...")
        initTasks().forEach {
            it.submit(this.pluginContainer)
        }
        logger.info("Connecting to database...")
        CashRepository.setupDatabase()
    }

    @Listener
    fun onGameReload(e: GameReloadEvent) {
        logger.info("Reloading plugin configuration...")
        CashShopConfig.load(configManager)
        logger.info("Restarting scheduled tasks")
        Sponge.getScheduler().getScheduledTasks(this.pluginContainer).forEach { it.cancel() }
        initTasks().forEach {
            it.submit(this.pluginContainer)
        }
    }

    private fun initTasks(): List<Task.Builder> {
        return mutableListOf<Task.Builder>().apply {
            if(CashShopConfig.taskLog > 0) {
                this.add(Task.builder()
                        .name("LogSaveTask")
                        .async()
                        .execute { _ -> LogRegistrar.registerRecords() }
                        .delay(CashShopConfig.taskLog, TimeUnit.SECONDS)
                        .interval(CashShopConfig.taskLog, TimeUnit.SECONDS))
            }

            if(CashShopConfig.taskCash > 0) {
                this.add(Task.builder()
                        .name("CashRefreshTask")
                        .async()
                        .execute { _ -> CashManager.refreshOnlinePlayersCash() }
                        .delay(CashShopConfig.taskCash, TimeUnit.SECONDS)
                        .interval(CashShopConfig.taskCash, TimeUnit.SECONDS))
            }
        }

    }

    @Listener
    fun onServerStarted(e: GameStartedServerEvent) {
        Sponge.getServiceManager().provide(PlaceholderService::class.java).get().loadAll(Placeholders(), this)
                .stream().map { builder ->
                    return@map builder.description("Get player's cash amount.")
                }.map { builder ->
                    builder.author("Teits").version("1.1.0")
                }.forEach { builder ->
                    try {
                        builder.buildAndRegister()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
    }

}