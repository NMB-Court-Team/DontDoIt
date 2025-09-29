package net.astrorbits.dontdoit.criteria.inspect

import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.Material

interface InventoryItemInspectCandidate : InventoryInspectable {
    fun getSelfInventoryItemMatchingWeightMultiplier(context: InventoryInspectContext): Double {
        return DEFAULT_SELF_WEIGHT_MULTIPLIER
    }

    fun getOtherInventoryItemWeightMultiplier(context: InventoryInspectContext): Double {
        return DEFAULT_OTHER_WEIGHT_MULTIPLIER
    }


    fun getCandidateInventoryItemTypes(): Set<Material>

    fun canMatchAnyInventoryItem(): Boolean

    fun getInventoryItemMultiplier(context: InventoryInspectContext): Double {
        val items = getCandidateInventoryItemTypes()
        return if (context.selfInventoryItems.any { it in items } || canMatchAnyInventoryItem()) {
            getSelfInventoryItemMatchingWeightMultiplier(context)
        } else if (context.otherInventoryItems.any { it in items }) {
            getOtherInventoryItemWeightMultiplier(context)
        } else DEFAULT_NO_MATCHING_WEIGHT_MULTIPLIER
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getInventoryItemMultiplier(context)
    }

    companion object {
        const val DEFAULT_SELF_WEIGHT_MULTIPLIER: Double = 0.9
        const val DEFAULT_OTHER_WEIGHT_MULTIPLIER: Double = 1.2
        const val DEFAULT_NO_MATCHING_WEIGHT_MULTIPLIER: Double = 0.1
    }
}