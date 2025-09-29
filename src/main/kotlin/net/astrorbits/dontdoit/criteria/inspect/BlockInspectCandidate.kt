package net.astrorbits.dontdoit.criteria.inspect

import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.Material

interface BlockInspectCandidate : InventoryInspectable {
    fun getBlockMatchingWeightMultiplier(context: InventoryInspectContext): Double {
        return DEFAULT_WEIGHT_MULTIPLIER
    }

    fun getSurroundingBlockMatchingWeightMultiplier(context: InventoryInspectContext): Double {
        return DEFAULT_SURROUNDING_WEIGHT_MULTIPLIER
    }

    fun getCandidateBlockTypes(): Set<Material>

    fun canMatchAnyBlock(): Boolean

    fun getBlockMultiplier(context: InventoryInspectContext): Double {
        val blocks = getCandidateBlockTypes()
        return if (context.surroundingBlocks.any { it in blocks } || canMatchAnyBlock()) {
            getSurroundingBlockMatchingWeightMultiplier(context)
        } else if (context.allBlocks.any { it in blocks }) {
            getBlockMatchingWeightMultiplier(context)
        } else DEFAULT_NO_MATCHING_WEIGHT_MULTIPLIER
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getBlockMultiplier(context)
    }

    companion object {
        const val DEFAULT_WEIGHT_MULTIPLIER: Double = 1.2
        const val DEFAULT_SURROUNDING_WEIGHT_MULTIPLIER: Double = 0.9
        const val DEFAULT_NO_MATCHING_WEIGHT_MULTIPLIER: Double = 0.1
    }
}