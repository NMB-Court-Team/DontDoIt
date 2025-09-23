package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.ItemInspectCandidate
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.Material

class HoldingItemCriteria : Criteria(), ItemInspectCandidate {
    override val type: CriteriaType = CriteriaType.HOLDING_ITEM
    lateinit var mainhandItem: Set<Material>
    var isMainhandWildcard: Boolean = false
    lateinit var offhandItem: Set<Material>
    var isOffhandWildcard: Boolean = false

    override fun getCandidateItemTypes(): Set<Material> {
        return mainhandItem + offhandItem
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
            val mainhandStack = player.inventory.itemInMainHand
            val offhandStack = player.inventory.itemInOffHand
            if (mainhandStack.type in mainhandItem && offhandStack.type in offhandItem) {
                trigger(player)
                break
            }
        }
    }

    companion object {
        const val MAINHAND_ITEM_KEY = "mainhand"
        const val OFFHAND_ITEM_KEY = "offhand"
    }
}