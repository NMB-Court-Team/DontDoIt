package net.astrorbits.dontdoit.criteria.inspect

import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.damage.DamageType

interface DamageTypeInspectCandidate : InventoryInspectable {
    fun getDamageTypeMatchingWeightMultiplier(context: InventoryInspectContext): Double {
        return DEFAULT_WEIGHT_MULTIPLIER
    }

    fun getCandidateDamageTypes(): Set<DamageType>

    fun canMatchAnyDamageType(): Boolean

    fun getDamageTypeMultiplier(context: InventoryInspectContext): Double {
        val damageTypes = getCandidateDamageTypes()
        return if (context.availableDamageTypes.any { it in damageTypes } || canMatchAnyDamageType()) {
            getDamageTypeMatchingWeightMultiplier(context)
        } else 1.0
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getDamageTypeMultiplier(context)
    }

    companion object {
        const val DEFAULT_WEIGHT_MULTIPLIER: Double = 1.15
    }
}