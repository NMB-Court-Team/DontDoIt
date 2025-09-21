package net.astrorbits.dontdoit.criteria

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.astrorbits.lib.Identifier
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

class ConsumedItemCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.CONSUMED_ITEM
    lateinit var itemTypes: List<Material>

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        val itemContent = data["item"] ?: throw InvalidCriteriaException(this, "Missing item key")
        val items = itemContent.replace("\n", "").split(",").map { it.trim() }
        val result = ArrayList<Material>()
        for (item in items) {
            if (item.startsWith("#")) {
                val tagKey = Identifier.of(item.removePrefix("#")).toKey()
                val tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this, "Invalid item tag: $item")
                result.addAll(tag.values().map { Material.matchMaterial(it.asString())!! })
            } else {
                val material = Material.matchMaterial(item)
                if (material == null || !material.isItem) throw InvalidCriteriaException(this, "Invalid item: $item")
                result.add(material)
            }
        }
        itemTypes = result
    }

    @EventHandler
    fun onPlayerConsumedItem(event: PlayerItemConsumeEvent) {
        val item = event.item
        if (item.type in itemTypes) {
            trigger(event.player)
        }
    }
}