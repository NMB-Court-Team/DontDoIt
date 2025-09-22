package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.system.CriteriaType
import net.astrorbits.dontdoit.criteria.type.ItemCriteria
import net.astrorbits.dontdoit.team.TeamData
import org.bukkit.Material

class InventoryContainingItemCriteria : Criteria(), ItemCriteria {
    override val type: CriteriaType = CriteriaType.INVENTORY_CONTAINING_ITEM
    lateinit var itemTypes: Set<Material>
    var isWildcard: Boolean = false

    override fun getCandidateItemTypes(): Set<Material> {
        return itemTypes
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (player.inventory.contents.any { it?.type in itemTypes }) {
                trigger(player)
                break
            }
        }
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setItemTypes(ITEM_TYPES_KEY) { itemTypes, isWildcard ->
            this.itemTypes = itemTypes
            this.isWildcard = isWildcard
        }
    }

    companion object {
        const val ITEM_TYPES_KEY = "item"
    }
}