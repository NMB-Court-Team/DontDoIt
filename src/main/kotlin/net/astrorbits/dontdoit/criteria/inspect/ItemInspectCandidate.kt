package net.astrorbits.dontdoit.criteria.inspect

import org.bukkit.Material

interface ItemInspectCandidate : InventoryInspectable {


    fun getCandidateItemTypes(): Set<Material>

    companion object {

    }
}