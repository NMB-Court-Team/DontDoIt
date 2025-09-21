package net.astrorbits.dontdoit.criteria.type

import org.bukkit.entity.EntityType

interface EntityCriteria {
    fun getCandidateEntityTypes(): Set<EntityType>
}