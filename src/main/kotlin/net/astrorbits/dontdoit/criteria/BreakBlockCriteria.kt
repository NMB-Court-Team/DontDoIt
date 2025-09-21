package net.astrorbits.dontdoit.criteria

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BreakBlockCriteria : Criteria(), Listener {
    override val type = CriteriaType.BREAK_BLOCK
    lateinit var blockTypes: Set<Material>
    var isWildcard: Boolean = false

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