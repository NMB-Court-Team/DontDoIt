package net.astrorbits.doNotDoIt

import net.astrorbits.doNotDoIt.system.GameStateManager
import net.astrorbits.doNotDoIt.team.TeamManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent

object EventListener : Listener {
    @EventHandler
    fun onServerLoad(event: ServerLoadEvent) {
        if (event.type == ServerLoadEvent.LoadType.STARTUP) {
            GameStateManager.init()
            TeamManager.init(DoNotDoIt.server)
        }
    }
}