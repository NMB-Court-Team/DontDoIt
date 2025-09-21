package net.astrorbits.dontdoit.criteria

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.astrorbits.lib.Identifier
import org.bukkit.Material

abstract class BlockCriteria : Criteria() {
    lateinit var blockTypes: Set<Material>
    var isWildcard: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        val entries = data.getCsvEntries(BLOCK_TYPES_KEY, AbsentBehavior.WILDCARD)

        val result = HashSet<Material>()
        for ((block, isTag, isReversed) in entries) {
            if (isReversed && result.isEmpty()) {
                result.addAll(Material.entries.filter { it.isBlock })
            }
            val blocks = HashSet<Material>()
            if (isTag) {
                val tagKey = Identifier.of(block).toKey()
                val tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK).tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this, "Invalid block tag: $block")
                blocks.addAll(tag.values().map { Material.matchMaterial(it.asString())!! })
            } else {
                val material = Material.matchMaterial(block)
                if (material == null || !material.isBlock) throw InvalidCriteriaException(this, "Invalid block: $block")
                blocks.add(material)
            }
            if (isReversed) {
                result.removeAll(blocks)
            } else {
                result.addAll(blocks)
            }
        }
        blockTypes = result

        isWildcard = entries.isWildcard
    }

    companion object {
        const val BLOCK_TYPES_KEY = "block"
    }
}