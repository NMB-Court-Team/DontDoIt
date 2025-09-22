package net.astrorbits.dontdoit.criteria

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.astrorbits.dontdoit.criteria.system.CriteriaType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class JumpCriteria : Criteria(), Listener {
    override val type = CriteriaType.JUMP

    @EventHandler
    fun onJump(event: PlayerJumpEvent) {
        trigger(event.player)
    }
}