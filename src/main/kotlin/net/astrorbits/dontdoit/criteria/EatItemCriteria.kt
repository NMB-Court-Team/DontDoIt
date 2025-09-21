package net.astrorbits.dontdoit.criteria

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

class EatItemCriteria : ItemCriteria(), Listener {
    override val type: CriteriaType = CriteriaType.EATEN_ITEM

    @EventHandler
    fun onPlayerConsumedItem(event: PlayerItemConsumeEvent) {
        val item = event.item
        if (isWildcard || (item.type in itemTypes && item.type.isEdible)) {
            trigger(event.player)
        }
    }
}