package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.ImmediatelyTriggerInspector
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.criteria.inspect.InventoryItemInspectCandidate
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.Material
import org.bukkit.entity.Player

class HoldingItemCriteria : Criteria(), InventoryItemInspectCandidate, ImmediatelyTriggerInspector {
    override val type: CriteriaType = CriteriaType.HOLDING_ITEM
    lateinit var mainhandItem: Set<Material>
    var isMainhandWildcard: Boolean = false
    lateinit var offhandItem: Set<Material>
    var isOffhandWildcard: Boolean = false

    override fun getCandidateInventoryItemTypes(): Set<Material> {
        return mainhandItem + offhandItem
    }

    override fun canMatchAnyInventoryItem(): Boolean {
        return isMainhandWildcard || isOffhandWildcard
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setItemTypes(MAINHAND_ITEM_KEY) { itemTypes, isWildcard ->
            mainhandItem = itemTypes
            isMainhandWildcard = isWildcard
        }
        data.setItemTypes(OFFHAND_ITEM_KEY) { itemTypes, isWildcard ->
            offhandItem = itemTypes
            isOffhandWildcard = isWildcard
        }
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
        val mainhandStack = player.inventory.itemInMainHand
        val offhandStack = player.inventory.itemInOffHand
        return (isMainhandWildcard || mainhandStack.type in mainhandItem)
            && (isOffhandWildcard || offhandStack.type in offhandItem)
    }

    override fun getSelfInventoryItemMatchingWeightMultiplier(context: InventoryInspectContext): Double {
        return 1.0
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getAnyTriggersMultiplier(bindTarget) { getInventoryItemMultiplier(context) }
    }

    companion object {
        const val MAINHAND_ITEM_KEY = "mainhand"
        const val OFFHAND_ITEM_KEY = "offhand"
    }
}