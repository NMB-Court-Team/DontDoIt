package net.astrorbits.dontdoit

import net.astrorbits.dontdoit.criteria.system.CriteriaManager
import net.astrorbits.dontdoit.system.GameStateManager
import net.astrorbits.dontdoit.system.team.TeamManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.server.ServerLoadEvent

object EventListener : Listener {
    @EventHandler
    fun onServerLoad(event: ServerLoadEvent) {
        if (event.type == ServerLoadEvent.LoadType.STARTUP) {
            GameStateManager.init()
            TeamManager.init(DontDoIt.server)
            CriteriaManager.onServerLoadInit()
        }
    }
}