package net.astrorbits.dontdoit.criteria.inspect

import org.bukkit.entity.EntityType

interface EntityInspectCandidate: InventoryInspectable {
    fun getEntityMatchingWeightMultiplier(): Double {
        return DEFAULT_WEIGHT_MULTIPLIER
    }

    fun getSurroundingEntityMatchingWeightMultiplier(): Double {
        return DEFAULT_SURROUNDING_WEIGHT_MULTIPLIER
    }

    fun getCandidateEntityTypes(): Set<EntityType>

    fun canMatchAnyEntity(): Boolean

    fun getEntityMultiplier(context: InventoryInspectContext): Double {
        val entities = getCandidateEntityTypes()
        return if (context.surroundingEntities.any { it in entities } || canMatchAnyEntity()) {
            getSurroundingEntityMatchingWeightMultiplier()
        } else if (context.allEntities.any { it in entities }) {
            getEntityMatchingWeightMultiplier()
        } else 1.0
    }

    override fun modifyWeight(weight: Double, context: InventoryInspectContext): Double {
        return weight * getEntityMultiplier(context)
    }

    companion object {
        const val DEFAULT_WEIGHT_MULTIPLIER: Double = 1.15
        const val DEFAULT_SURROUNDING_WEIGHT_MULTIPLIER: Double = 1.3
    }
}