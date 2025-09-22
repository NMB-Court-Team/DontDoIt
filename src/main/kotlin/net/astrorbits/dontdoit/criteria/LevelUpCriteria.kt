package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLevelChangeEvent

class LevelUpCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.LEVEL_UP

    @EventHandler
    fun onPlayerLevelUp(event: PlayerLevelChangeEvent) {
        if (event.newLevel - event.oldLevel > 0) {
            trigger(event.player)
        }
    }
}