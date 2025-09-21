package net.astrorbits.dontdoit.criteria

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.astrorbits.lib.Identifier
import org.bukkit.Material

abstract class ItemCriteria : Criteria() {
    lateinit var itemTypes: Set<Material>
    var isWildcard: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        val entries = data.getCsvEntries(ITEM_TYPES_KEY, AbsentBehavior.WILDCARD)

        val result = HashSet<Material>()
        for ((item, isTag, isReversed) in entries) {
            if (isReversed && result.isEmpty()) {
                result.addAll(Material.entries.filter { it.isItem })
            }
            val items = HashSet<Material>()
            if (isTag) {
                val tagKey = Identifier.of(item).toKey()
                val tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this, "Invalid item tag: $item")
                items.addAll(tag.values().map { Material.matchMaterial(it.asString())!! })
            } else {
                val material = Material.matchMaterial(item)
                if (material == null || !material.isItem) throw InvalidCriteriaException(this, "Invalid item: $item")
                items.add(material)
            }
            if (isReversed) {
                result.removeAll(items)
            } else {
                result.addAll(items)
            }
        }
        itemTypes = result

        isWildcard = entries.isWildcard
    }

    companion object {
        const val ITEM_TYPES_KEY = "item"
    }
}