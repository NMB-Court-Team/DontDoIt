package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.type.ItemCriteria
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent

class CraftItemCriteria : Criteria(), Listener, ItemCriteria {
    override val type: CriteriaType = CriteriaType.CRAFT_ITEM
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
    fun onCraftItem(event: CraftItemEvent) {
        val item = event.recipe.result
        val clicker = event.whoClicked
        if (clicker is Player && (isWildcard || item.type in itemTypes)) {
            trigger(clicker)
        }
    }

    companion object {
        const val ITEM_TYPES_KEY = "item"
    }
}