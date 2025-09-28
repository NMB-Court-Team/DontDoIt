package net.astrorbits.dontdoit.criteria

import io.papermc.paper.event.inventory.PaperInventoryMoveItemEvent
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.criteria.inspect.InventoryItemInspectCandidate
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class TransferItemCriteria : Criteria(), Listener, InventoryItemInspectCandidate {
    override val type = CriteriaType.TRANSFER_ITEM
    lateinit var fromTypes: Set<InventoryType>
    lateinit var toTypes: Set<InventoryType>
    lateinit var itemTypes: Set<Material>
    var isFromTypeWildcard: Boolean = false
    var isToTypeWildcard: Boolean = false
    var isItemWildcard: Boolean = false
    override fun getCandidateInventoryItemTypes(): Set<Material> {
        return itemTypes
    }

    override fun canMatchAnyInventoryItem(): Boolean {
        return isItemWildcard
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setItemTypes(ITEM_TYPES_KEY) { itemTypes, isWildcard ->
            this.itemTypes = itemTypes
            this.isItemWildcard = isWildcard
        }
        data.setInventoryTypes(FROM_TYPE_KEY) { fromTypes, isWildcard ->
            this.fromTypes = fromTypes
            this.isFromTypeWildcard = isWildcard
        }
        data.setInventoryTypes(TO_TYPE_KEY) { toTypes, isWildcard ->
            this.toTypes = toTypes
            this.isToTypeWildcard = isWildcard
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onMoveItem(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val clickedInv = event.clickedInventory ?: return
        val topInv = event.view.topInventory
        val bottomInv = event.view.bottomInventory

        val clickedItem = event.currentItem
        val cursorItem = event.cursor

        when (event.action) {
            InventoryAction.MOVE_TO_OTHER_INVENTORY -> {
                // shift 快捷转移
                val fromInv = clickedInv
                val toInv = if (fromInv == topInv) bottomInv else topInv

                if (checkMatch(clickedItem, fromInv.type, toInv.type)) {
                    trigger(player)
                }
            }
            InventoryAction.PLACE_ONE,
            InventoryAction.PLACE_SOME,
            InventoryAction.PLACE_ALL -> {
                // 放入物品
                val toInv = clickedInv
                if (checkMatch(cursorItem, null, toInv.type)) {
                    trigger(player)
                }
            }

            InventoryAction.PICKUP_ONE,
            InventoryAction.PICKUP_SOME,
            InventoryAction.PICKUP_ALL -> {
                // 从容器取出物品
                val fromInv = clickedInv
                if (checkMatch(clickedItem, fromInv.type, null)) {
                    trigger(player)
                }
            }
//TODO
//            InventoryAction.HOTBAR_SWAP,
//            InventoryAction.SWAP_WITH_CURSOR -> {
//                // 数字键 / 光标交换
//                if (clickedInv === topInv) {
//                    if (checkMatch(clickedItem, clickedInv.type, null) ||
//                        checkMatch(cursorItem, null, clickedInv.type)) {
//                        trigger(player)
//                    }
//                } else {
//                    if (checkMatch(clickedItem, null, topInv.type) ||
//                        checkMatch(cursorItem, topInv.type, null)) {
//                        trigger(player)
//                    }
//                }
//            }
            else -> {}
        }

    }
    fun checkMatch(item: ItemStack?, from: InventoryType?, to: InventoryType?): Boolean {
        if (item == null || item.type.isAir) return false
        if (item.type !in itemTypes) return false
        if (fromTypes.isNotEmpty() && (from == null || from !in fromTypes)) return false
        if (toTypes.isNotEmpty() && (to == null || to !in toTypes)) return false
        return true
    }

    companion object {
        const val FROM_TYPE_KEY = "from"
        const val TO_TYPE_KEY = "to"
        const val ITEM_TYPES_KEY = "item"
    }
}