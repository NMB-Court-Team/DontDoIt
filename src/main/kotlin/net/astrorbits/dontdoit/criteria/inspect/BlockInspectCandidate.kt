package net.astrorbits.dontdoit.criteria.inspect

import org.bukkit.Material

interface BlockInspectCandidate : InventoryInspectable {
    fun getCandidateBlockTypes(): Set<Material>
}