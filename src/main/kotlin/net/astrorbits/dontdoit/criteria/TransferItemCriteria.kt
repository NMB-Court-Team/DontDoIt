package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.InventoryItemInspectCandidate
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class TransferItemCriteria : Criteria(), Listener, InventoryItemInspectCandidate {
    override val type = CriteriaType.TRANSFER_ITEM
    lateinit var itemTypes: Set<Material>
    var isWildcard: Boolean = false
    lateinit var targetInventory: InventoryType
    var reversed: Boolean = false

    override fun getCandidateInventoryItemTypes(): Set<Material> {
        return itemTypes
    }

    override fun canMatchAnyInventoryItem(): Boolean {
        return isWildcard
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setItemTypes(ITEM_TYPES_KEY) { itemTypes, isWildcard ->
            this.itemTypes = itemTypes
            this.isWildcard = isWildcard
        }
        data.setField(TARGET_INVENTORY_KEY) { targetInventory = InventoryType.valueOf(it.uppercase()) }
        if (targetInventory in UNSUPPORTED_INVENTORY_TYPES) {
            throw InvalidCriteriaException(this, "Unsupported inventory type: ${targetInventory.name.lowercase()}")
        }
        data.setBoolField(REVERSED_KEY, true) { reversed = it }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val clickedInv = event.clickedInventory ?: return
        val topInv = event.view.topInventory

        val clickedItem = event.currentItem
        val cursorItem = event.cursor
        val hotbarItem = if (event.hotbarButton == -1) null else player.inventory.getItem(event.hotbarButton)
        val offhandItem = player.inventory.itemInOffHand

        val clickType = event.click

        // 双击收集物品和转移物品的检测非常麻烦，暂时做不到，就当work as intended得了
        if (!reversed) {  // 放入物品
            when (clickType) {
                // 在top放下指针上的物品
                ClickType.LEFT, ClickType.RIGHT -> {
                    if (isItemMatch(cursorItem) && topInv == clickedInv) {
                        trigger(player)
                    }
                }
                // 对着top按数字键
                ClickType.NUMBER_KEY -> {
                    if (isItemMatch(hotbarItem) && clickedInv == topInv) {
                        trigger(player)
                    }
                }
                // 对着top按F
                ClickType.SWAP_OFFHAND -> {
                    if (isItemMatch(offhandItem) && clickedItem == topInv) {
                        trigger(player)
                    }
                }
                // 在inventory点击物品
                ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> {
                    if (isItemMatch(clickedItem) && clickedInv != topInv) {
                        trigger(player)
                    }
                }
                else -> {}
            }
        } else {  // 拿出物品
            when (clickType) {
                ClickType.LEFT, ClickType.SHIFT_LEFT, ClickType.RIGHT, ClickType.SHIFT_RIGHT,
                ClickType.NUMBER_KEY, ClickType.SWAP_OFFHAND, ClickType.DROP, ClickType.CONTROL_DROP -> {
                    if (isItemMatch(clickedItem) && clickedInv == topInv) {
                        trigger(player)
                    }
                }
                else -> {}
            }
        }
    }

    fun isItemMatch(item: ItemStack?): Boolean {
        return item != null && !item.isEmpty && (isWildcard || item.type in itemTypes)
    }

    companion object {
        const val ITEM_TYPES_KEY = "item"
        const val TARGET_INVENTORY_KEY = "target_inventory"
        const val REVERSED_KEY = "reversed"

        private val UNSUPPORTED_INVENTORY_TYPES: Set<InventoryType> = setOf(
            InventoryType.CRAFTING,
            InventoryType.PLAYER,
            InventoryType.CREATIVE,
            InventoryType.LECTERN,
            InventoryType.COMPOSTER,
            InventoryType.CHISELED_BOOKSHELF,
            InventoryType.JUKEBOX,
            InventoryType.DECORATED_POT
        )
    }
}