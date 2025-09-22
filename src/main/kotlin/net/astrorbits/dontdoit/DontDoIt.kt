package net.astrorbits.dontdoit

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.astrorbits.dontdoit.criteria.system.CriteriaManager
import net.astrorbits.dontdoit.system.CriteriaCommand
import net.astrorbits.dontdoit.system.Preparation
import net.astrorbits.dontdoit.system.GameStateManager
import net.astrorbits.dontdoit.system.TitleManager
import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DontDoIt : JavaPlugin() {
    override fun onEnable() {
        _instance = this
        stateManager = GameStateManager //是这样吗

        Configs.init()
        CriteriaManager.init(this)
        GlobalSettings.init(this)
        TitleManager.init(this)
        stateManager.reset()

        Preparation.register(this)
        server.pluginManager.registerEvents(EventListener, this)
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()
            CriteriaCommand.register(registrar)
        }
    }

    override fun onDisable() {
        stateManager.onDisable()
    }

    companion object {
        private var _instance: DontDoIt? = null

        val instance: DontDoIt
            get() = _instance ?: throw IllegalStateException("Should not get plugin instance when plugin is not enabled")
        val server: Server
            get() = instance.server

        const val PLUGIN_NAME = "DoNotDoIt"
        val LOGGER: Logger = LoggerFactory.getLogger(PLUGIN_NAME)

        lateinit var stateManager: GameStateManager
            private set
    }
}
