package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.BlockInspectCandidate
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class PhysicalActionCriteria : Criteria(), Listener, BlockInspectCandidate {
    override val type = CriteriaType.PHYSICAL_ACTION
    lateinit var blockTypes: Set<Material>
    var isWildcard: Boolean = false

    override fun canMatchAnyBlock(): Boolean {
        return isWildcard
    }

    override fun getCandidateBlockTypes(): Set<Material> {
        return blockTypes
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setBlockTypes(BLOCK_TYPES_KEY) { blockTypes, isWildcard ->
            this.blockTypes = blockTypes
            this.isWildcard = isWildcard
        }
    }

    @EventHandler
    fun onPhysicalAction(event: PlayerInteractEvent) {
        if (event.action == Action.PHYSICAL) {
            if (isWildcard || event.clickedBlock?.type in blockTypes){
                trigger(event.player)
            }
        }
    }

    companion object {
        const val BLOCK_TYPES_KEY = "block"
    }
}