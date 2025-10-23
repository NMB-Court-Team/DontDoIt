package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.BlockInspectCandidate
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.criteria.inspect.InventoryItemInspectCandidate
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.max

class InteractBlockWithItemCriteria : Criteria(), Listener, BlockInspectCandidate, InventoryItemInspectCandidate {
    override val type = CriteriaType.INTERACT_BLOCK_WITH_ITEM
    lateinit var blockTypes: Set<Material>
    lateinit var itemTypes: Set<Material>
    var isBlockWildcard: Boolean = false
    var isItemWildcard: Boolean = false

    override fun getCandidateBlockTypes(): Set<Material> {
        return blockTypes
    }

    override fun canMatchAnyBlock(): Boolean {
        return isBlockWildcard
    }

    override fun getCandidateInventoryItemTypes(): Set<Material> {
        return itemTypes
    }

    override fun canMatchAnyInventoryItem(): Boolean {
        return isItemWildcard
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
    fun onInteractWithBlock(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        val item = event.item ?: ItemStack.empty()

        if (event.useInteractedBlock() == Event.Result.DENY) return // 避免潜行放方块时也触发

        if ((isBlockWildcard || block.type in blockTypes) &&
            (isItemWildcard || item.type in itemTypes)
        ) {
            trigger(event.player)
        }
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * max(getBlockMultiplier(context), getInventoryItemMultiplier(context))
    }

    companion object {
        const val BLOCK_TYPES_KEY = "block"
        const val ITEM_TYPES_KEY = "item"
    }
}