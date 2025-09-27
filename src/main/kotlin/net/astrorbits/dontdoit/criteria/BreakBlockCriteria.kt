package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.BlockInspectCandidate
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BreakBlockCriteria : Criteria(), Listener, BlockInspectCandidate {
    override val type = CriteriaType.BREAK_BLOCK
    lateinit var blockTypes: Set<Material>
    var isWildcard: Boolean = false

    override fun getCandidateBlockTypes(): Set<Material> {
        return blockTypes
    }

    override fun canMatchAnyBlock(): Boolean {
        return isWildcard
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setBlockTypes(BLOCK_TYPES_KEY) { blockTypes, isWildcard ->
            this.blockTypes = blockTypes
            this.isWildcard = isWildcard
        }
    }

    @EventHandler
    fun onBreakBlock(event: BlockBreakEvent) {
        val block = event.block
        if (isWildcard || block.type in blockTypes) {
            trigger(event.player)
        }
    }

    companion object {
        const val BLOCK_TYPES_KEY = "block"
    }
}