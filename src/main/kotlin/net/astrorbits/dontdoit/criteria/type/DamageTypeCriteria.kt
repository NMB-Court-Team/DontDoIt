package net.astrorbits.dontdoit.criteria.type

import org.bukkit.damage.DamageType

interface DamageTypeCriteria {
    fun getCandidateDamageTypes(): Set<DamageType>
}