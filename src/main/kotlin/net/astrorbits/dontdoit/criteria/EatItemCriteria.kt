package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.system.CriteriaType
import net.astrorbits.dontdoit.criteria.type.ItemCriteria
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

class EatItemCriteria : Criteria(), Listener, ItemCriteria {
    override val type: CriteriaType = CriteriaType.EAT_ITEM
    lateinit var itemTypes: Set<Material>
    var isWildcard: Boolean = false

    override fun getCandidateItemTypes(): Set<Material> {
        return itemTypes
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setItemTypes(ITEM_TYPES_KEY) { itemTypes, isWildcard ->
            this.itemTypes = itemTypes
            this.isWildcard = isWildcard
        }
    }

    @EventHandler
    fun onPlayerConsumedItem(event: PlayerItemConsumeEvent) {
        val item = event.item
        if (isWildcard || (item.type in itemTypes && item.type.isEdible)) {
            trigger(event.player)
        }
    }

    companion object {
        const val ITEM_TYPES_KEY = "item"
    }
}