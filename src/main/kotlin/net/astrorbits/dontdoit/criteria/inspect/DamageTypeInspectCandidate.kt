package net.astrorbits.dontdoit.criteria.inspect

import org.bukkit.damage.DamageType

interface DamageTypeInspectCandidate : InventoryInspectable {
    fun getDamageTypeMatchingWeightMultiplier(): Double {
        return DEFAULT_WEIGHT_MULTIPLIER
    }

    fun getCandidateDamageTypes(): Set<DamageType>

    fun canMatchAnyDamageType(): Boolean

    fun getDamageTypeMultiplier(context: InventoryInspectContext): Double {
        val damageTypes = getCandidateDamageTypes()
        return if (context.availableDamageTypes.any { it in damageTypes } || canMatchAnyDamageType()) {
            getDamageTypeMatchingWeightMultiplier()
        } else 1.0
    }

    override fun modifyWeight(weight: Double, context: InventoryInspectContext): Double {
        return weight * getDamageTypeMultiplier(context)
    }

    companion object {
        const val DEFAULT_WEIGHT_MULTIPLIER: Double = 1.2
    }
}