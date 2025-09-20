package net.astrorbits.doNotDoIt

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.astrorbits.doNotDoIt.criteria.CriteriaManager
import net.astrorbits.doNotDoIt.system.Preparation
import net.astrorbits.doNotDoIt.system.GameStateManager
import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStreamReader

class DoNotDoIt : JavaPlugin() {

    companion object {
        private var _instance: DoNotDoIt? = null

        val instance: DoNotDoIt
            get() = _instance ?: throw IllegalStateException("Should not get plugin instance when plugin is not enabled")
        val server: Server
            get() = instance.server

        const val PLUGIN_NAME = "DoNotDoIt"
        val LOGGER: Logger = LoggerFactory.getLogger(PLUGIN_NAME)

        lateinit var stateManager: GameStateManager
            private set

        const val CRITERIA_FILE_NAME = "criteria.json"
    }

    override fun onEnable() {
        _instance = this

        Configs.init()
        CriteriaManager.registerAll()
        CriteriaManager.loadFromJson(readCriteriaJson(), this)
        GlobalSettings.init(this)

        stateManager.reset()

        Preparation.register(this)
        server.pluginManager.registerEvents(EventListener, this)
    }

    override fun onDisable() {
        stateManager.pause()
    }

    fun readCriteriaJson(): JsonObject {
        val configFile = instance.dataPath.resolve(CRITERIA_FILE_NAME).toFile()

        if (!configFile.exists()) {
            LOGGER.info("Criteria definition file '$CRITERIA_FILE_NAME' not found, using default file in resource")
            configFile.parentFile.mkdirs()
            javaClass.getResourceAsStream(CRITERIA_FILE_NAME)?.use { inputStream ->
                configFile.outputStream().use { out ->
                    inputStream.copyTo(out)
                }
            } ?: LOGGER.error("Cannot find criteria definition file in resource")
        }

        LOGGER.info("Loading criteria definition file of '$CRITERIA_FILE_NAME'")
        configFile.inputStream().use { inputStream ->
            try {
                return JsonParser.parseReader(InputStreamReader(inputStream)).asJsonObject
            } catch (e: Exception) {
                LOGGER.error("Failed to read criteria definition file: ", e)
                e.printStackTrace()
            }
        }
        return JsonObject()
    }
}
