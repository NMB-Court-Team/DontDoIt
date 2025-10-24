package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.BlockInspectCandidate
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.criteria.inspect.InventoryItemInspectCandidate
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
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
    private val recentBlockPlacers = mutableSetOf<UUID>()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        recentBlockPlacers += event.player.uniqueId
        val providingPlugin = JavaPlugin.getProvidingPlugin(InteractBlockWithItemCriteria::class.java)

        Bukkit.getScheduler().runTaskLater(providingPlugin, Runnable {
            recentBlockPlacers -= event.player.uniqueId
        }, 1L)
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onInteractWithBlock(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        val item = event.item ?: ItemStack.empty()

        // if (event.useInteractedBlock() == Event.Result.DENY) return // 666神秘上游永远返回ALLOW诗人握持

        val player = event.player
        // 排除“刚放置方块”的情况
        if (player.uniqueId in recentBlockPlacers) return

        // 允许 Paper 的潜行空手交互被触发
        event.setUseInteractedBlock(Event.Result.ALLOW)
        event.setUseItemInHand(Event.Result.ALLOW)


        // if (player.isSneaking && item.type.isBlock) return // 更改实现方法

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