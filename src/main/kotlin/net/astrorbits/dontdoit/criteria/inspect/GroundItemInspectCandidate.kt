package net.astrorbits.dontdoit.criteria.inspect

import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.Material

interface GroundItemInspectCandidate : InventoryInspectable {
    fun getGroundItemMatchingWeightMultiplier(context: InventoryInspectContext): Double {
        return DEFAULT_WEIGHT_MULTIPLIER
    }

    fun getSurroundingGroundItemWeightMultiplier(context: InventoryInspectContext): Double {
        return DEFAULT_SURROUNDING_WEIGHT_MULTIPLIER
    }

    fun getCandidateGroundItemTypes(): Set<Material>

    fun canMatchAnyGroundItem(): Boolean

    fun getInventoryItemMultiplier(context: InventoryInspectContext): Double {
        val items = getCandidateGroundItemTypes()
        return if (context.surroundingGroundItems.any { it in items } || canMatchAnyGroundItem()) {
            getSurroundingGroundItemWeightMultiplier(context)
        } else if (context.allGroundItems.any { it in items }) {
            getGroundItemMatchingWeightMultiplier(context)
        } else DEFAULT_NO_MATCHING_WEIGHT_MULTIPLIER
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getInventoryItemMultiplier(context)
    }

    companion object {
        const val DEFAULT_WEIGHT_MULTIPLIER: Double = 1.2
        const val DEFAULT_SURROUNDING_WEIGHT_MULTIPLIER: Double = 0.9
        const val DEFAULT_NO_MATCHING_WEIGHT_MULTIPLIER: Double = 0.5
    }
}