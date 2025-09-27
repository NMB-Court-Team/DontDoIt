package net.astrorbits.dontdoit.criteria.inspect

import org.bukkit.Material

interface BlockInspectCandidate : InventoryInspectable {
    fun getBlockMatchingWeightMultiplier(): Double {
        return DEFAULT_WEIGHT_MULTIPLIER
    }

    fun getSurroundingBlockMatchingWeightMultiplier(): Double {
        return DEFAULT_SURROUNDING_WEIGHT_MULTIPLIER
    }

    fun getCandidateBlockTypes(): Set<Material>

    fun canMatchAnyBlock(): Boolean

    fun getBlockMultiplier(context: InventoryInspectContext): Double {
        val blocks = getCandidateBlockTypes()
        return if (context.surroundingBlocks.any { it in blocks } || canMatchAnyBlock()) {
            getSurroundingBlockMatchingWeightMultiplier()
        } else if (context.allBlocks.any { it in blocks }) {
            getBlockMatchingWeightMultiplier()
        } else 1.0
    }

    override fun modifyWeight(weight: Double, context: InventoryInspectContext): Double {
        return weight * getBlockMultiplier(context)
    }

    companion object {
        const val DEFAULT_WEIGHT_MULTIPLIER: Double = 1.15
        const val DEFAULT_SURROUNDING_WEIGHT_MULTIPLIER: Double = 1.3
    }
}