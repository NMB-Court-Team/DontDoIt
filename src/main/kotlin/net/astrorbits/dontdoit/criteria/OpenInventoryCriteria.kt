package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.criteria.inspect.InventoryItemInspectCandidate
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType

class OpenInventoryCriteria : Criteria(), Listener, InventoryItemInspectCandidate {
    override val type: CriteriaType = CriteriaType.OPEN_INVENTORY
    lateinit var inventoryTypes: Set<InventoryType>
    lateinit var itemTypes: Set<Material>
    var isItemWildcard: Boolean = false
    var isInventoryWildcard: Boolean = false

    override fun getCandidateInventoryItemTypes(): Set<Material> {
        return itemTypes
    }

    override fun canMatchAnyInventoryItem(): Boolean {
        return isItemWildcard
    }

    override fun getOtherInventoryItemWeightMultiplier(context: InventoryInspectContext): Double {
        return 1.0
    }

    override fun getSelfInventoryItemMatchingWeightMultiplier(context: InventoryInspectContext): Double {
        return SELF_INVENTORY_MULTIPLIER
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setInventoryTypes(INVENTORY_TYPE) { inventoryTypes, isWildcard ->
            this.inventoryTypes = inventoryTypes
            this.isInventoryWildcard = isWildcard
        }
        data.setItemTypes(ITEM_TYPES_KEY) { itemTypes, isWildcard ->
            this.itemTypes = itemTypes
            this.isItemWildcard = isWildcard
        }
    }

    @EventHandler
    fun onPlayerOpenInventory(event: InventoryOpenEvent) {
        val inv = event.inventory
        val player = event.player as Player
        if (isInventoryWildcard || (inv.type in inventoryTypes)) {
            if(isItemWildcard || (player.inventory.itemInMainHand.type in itemTypes)){
                trigger(player)
            }
        }
    }

    companion object {
        const val ITEM_TYPES_KEY = "item"
        const val INVENTORY_TYPE = "target_inventory"

        const val SELF_INVENTORY_MULTIPLIER = 1.15
    }
}