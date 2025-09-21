package net.astrorbits.dontdoit.criteria

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BreakBlockCriteria : BlockCriteria(), Listener {
    override val type = CriteriaType.BREAK_BLOCK

    @EventHandler
    fun onBreakBlock(event: BlockBreakEvent) {
        val block = event.block
        if (isWildcard || block.type in blockTypes) {
            trigger(event.player)
        }
    }
}