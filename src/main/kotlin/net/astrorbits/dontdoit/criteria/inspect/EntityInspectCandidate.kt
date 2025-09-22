package net.astrorbits.dontdoit.criteria.inspect

import org.bukkit.entity.EntityType

interface EntityInspectCandidate: InventoryInspectable {
    fun getCandidateEntityTypes(): Set<EntityType>
}