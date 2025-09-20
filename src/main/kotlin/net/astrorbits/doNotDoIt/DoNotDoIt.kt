package net.astrorbits.doNotDoIt

import com.google.gson.JsonObject
import net.astrorbits.doNotDoIt.criteria.CriteriaManager
import net.astrorbits.doNotDoIt.inGame.Preparation
import net.astrorbits.doNotDoIt.inGame.GameStateManager
import org.bukkit.plugin.java.JavaPlugin

class DoNotDoIt : JavaPlugin() {

    companion object {
        lateinit var stateManager: GameStateManager
            private set
    }

    override fun onEnable() {
        saveDefaultConfig()
        CriteriaManager.loadFromJson(json = JsonObject(), plugin = this)

        stateManager = GameStateManager(this)
        stateManager.reset()

        Preparation.register(this)
        CriteriaManager.registerAll()
    }

    override fun onDisable() {
        stateManager.pause()
    }
}
