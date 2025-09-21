package net.astrorbits.dontdoit.criteria

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent

class PickUpCriteria : ItemCriteria(), Listener {
    override val type: CriteriaType = CriteriaType.PICK_UP

    @EventHandler
    fun onPlayerPickUpItem(event: EntityPickupItemEvent) {
        if (event.entity.type != EntityType.PLAYER) return
        val item = event.item.itemStack
        if (isWildcard || item.type in itemTypes) {
            trigger(event.entity as Player)
        }
    }
}