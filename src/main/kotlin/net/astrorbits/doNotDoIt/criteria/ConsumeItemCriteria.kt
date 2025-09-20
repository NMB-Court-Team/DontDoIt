package net.astrorbits.doNotDoIt.criteria

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.*

class ConsumeItemCriteria : CriteriaListener(), Listener {
    @EventHandler
    fun onUsedItem(event: PlayerAnimationEvent){
        event.animationType
    }
}