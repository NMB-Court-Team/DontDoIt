package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.BlockInspectCandidate
import net.astrorbits.dontdoit.criteria.inspect.ItemInspectCandidate
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class InteractBlockWithItemCriteria : Criteria(), Listener, BlockInspectCandidate, ItemInspectCandidate {
    override val type = CriteriaType.INTERACT_BLOCK_WITH_ITEM
    lateinit var blockTypes: Set<Material>
    lateinit var itemTypes: Set<Material>
    var isBlockWildcard: Boolean = false
    var isItemWildcard: Boolean = false

    override fun getCandidateBlockTypes(): Set<Material> {
        return blockTypes
    }

    override fun getCandidateItemTypes(): Set<Material> {
        return itemTypes
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setBlockTypes(BLOCK_TYPES_KEY) { blockTypes, isWildcard ->
            this.blockTypes = blockTypes
            this.isBlockWildcard = isWildcard
        }
        data.setItemTypes(ITEM_TYPES_KEY) { itemTypes, isWildcard ->
            this.itemTypes = itemTypes
            this.isItemWildcard = isWildcard
        }
    }

    @EventHandler
    fun onPlaceBlock(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        val item = event.item ?: ItemStack.empty()
        if ((isBlockWildcard || block.type in blockTypes) &&
            (isItemWildcard || (item.type in itemTypes))
        ) {
            trigger(event.player)
        }
    }

    companion object {
        const val BLOCK_TYPES_KEY = "block"
        const val ITEM_TYPES_KEY = "item"
    }
}