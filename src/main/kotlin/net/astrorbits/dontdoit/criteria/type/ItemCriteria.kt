package net.astrorbits.dontdoit.criteria.type

import org.bukkit.Material

interface ItemCriteria {
    fun getCandidateItemTypes(): Set<Material>
}