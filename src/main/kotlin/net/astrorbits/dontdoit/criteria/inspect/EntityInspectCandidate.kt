package net.astrorbits.dontdoit.criteria.inspect

import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.entity.EntityType

interface EntityInspectCandidate: InventoryInspectable {
    fun getEntityMatchingWeightMultiplier(context: InventoryInspectContext): Double {
        return DEFAULT_WEIGHT_MULTIPLIER
    }

    fun getSurroundingEntityMatchingWeightMultiplier(context: InventoryInspectContext): Double {
        return DEFAULT_SURROUNDING_WEIGHT_MULTIPLIER
    }

    fun getCandidateEntityTypes(): Set<EntityType>

    fun canMatchAnyEntity(): Boolean

    fun getEntityMultiplier(context: InventoryInspectContext): Double {
        val entities = getCandidateEntityTypes()
        return if (context.surroundingEntities.any { it in entities } || canMatchAnyEntity()) {
            getSurroundingEntityMatchingWeightMultiplier(context)
        } else if (context.allEntities.any { it in entities }) {
            getEntityMatchingWeightMultiplier(context)
        } else DEFAULT_NO_MATCHING_WEIGHT_MULTIPLIER
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getEntityMultiplier(context)
    }

    companion object {
        const val DEFAULT_WEIGHT_MULTIPLIER: Double = 1.2
        const val DEFAULT_SURROUNDING_WEIGHT_MULTIPLIER: Double = 0.9
        const val DEFAULT_NO_MATCHING_WEIGHT_MULTIPLIER: Double = 0.1
    }
}