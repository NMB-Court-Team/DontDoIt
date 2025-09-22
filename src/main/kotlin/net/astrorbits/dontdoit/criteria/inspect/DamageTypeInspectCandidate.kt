package net.astrorbits.dontdoit.criteria.inspect

import org.bukkit.damage.DamageType

interface DamageTypeInspectCandidate : InventoryInspectable {
    fun getCandidateDamageTypes(): Set<DamageType>
}