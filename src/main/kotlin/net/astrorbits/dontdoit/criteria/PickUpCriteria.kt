package net.astrorbits.dontdoit.criteria

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.astrorbits.lib.Identifier
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent

class PickUpCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.PICK_UP
    lateinit var itemTypes: List<Material>
    var isWildcard: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        val itemContent = data[ITEM_TYPES_KEY] ?: throw InvalidCriteriaException(this, "Missing key '$ITEM_TYPES_KEY'")
        val items = itemContent.replace("\n", "").split(",").map { it.trim() }
        val result = ArrayList<Material>()
        for (item in items) {
            if (item.startsWith("#")) {
                val tagKey = Identifier.of(item.removePrefix("#")).toKey()
                val tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this, "Invalid item tag: $item")
                result.addAll(tag.values().map { Material.matchMaterial(it.asString())!! })
            } else if (item == "*") {
                isWildcard = true
            } else {
                val material = Material.matchMaterial(item)
                if (material == null || !material.isItem) throw InvalidCriteriaException(this, "Invalid item: $item")
                result.add(material)
            }
        }
        itemTypes = result
    }

    @EventHandler
    fun onPlayerPickUpItem(event: EntityPickupItemEvent) {
        if (event.entity.type != EntityType.PLAYER) return
        val item = event.item.itemStack
        if (isWildcard || item.type in itemTypes) {
            trigger(event.entity as Player)
        }
    }

    companion object {
        const val ITEM_TYPES_KEY = "item"
    }
}