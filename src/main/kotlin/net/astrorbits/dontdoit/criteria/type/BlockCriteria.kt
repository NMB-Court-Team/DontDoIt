package net.astrorbits.dontdoit.criteria.type

import org.bukkit.Material

interface BlockCriteria {
    fun getCandidateBlockTypes(): Set<Material>
}