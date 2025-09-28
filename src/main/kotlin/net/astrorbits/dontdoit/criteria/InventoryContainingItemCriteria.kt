package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.ImmediatelyTriggerInspector
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.criteria.inspect.InventoryItemInspectCandidate
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.Material
import org.bukkit.entity.Player

class InventoryContainingItemCriteria : Criteria(), InventoryItemInspectCandidate, ImmediatelyTriggerInspector {
    override val type: CriteriaType = CriteriaType.INVENTORY_CONTAINING_ITEM
    lateinit var itemTypes: Set<Material>
    var isWildcard: Boolean = false
    var reversed: Boolean = false

    override fun getCandidateInventoryItemTypes(): Set<Material> {
        return itemTypes
    }

    override fun canMatchAnyInventoryItem(): Boolean {
        return isWildcard
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (shouldTrigger(player)) {
                trigger(player)
                break
            }
        }
    }

    override fun shouldTrigger(player: Player): Boolean {
        return (player.inventory.contents.any { it?.type in itemTypes } || isWildcard) xor reversed
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setItemTypes(ITEM_TYPES_KEY) { itemTypes, isWildcard ->
            this.itemTypes = itemTypes
            this.isWildcard = isWildcard
        }
        data.setBoolField(REVERSED_KEY, true) { reversed = it }
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getAnyTriggersMultiplier(bindTarget) { getInventoryItemMultiplier(context) }
    }

    companion object {
        const val ITEM_TYPES_KEY = "item"
        const val REVERSED_KEY = "reversed"
    }
}