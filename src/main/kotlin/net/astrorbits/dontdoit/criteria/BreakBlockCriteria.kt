package net.astrorbits.dontdoit.criteria

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.astrorbits.lib.Identifier
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BreakBlockCriteria : Criteria(), Listener {
    override val type = CriteriaType.BREAK_BLOCK
    lateinit var blockTypes: List<Material>

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        val blockContent = data[BLOCK_TYPES_KEY] ?: throw InvalidCriteriaException(this, "Missing key '$BLOCK_TYPES_KEY'")
        val blocks = blockContent.replace("\n", "").split(",").map { it.trim() }
        val result = ArrayList<Material>()
        for (block in blocks) {
            if (block.startsWith("#")) {
                val tagKey = Identifier.of(block.removePrefix("#")).toKey()
                val tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK).tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this, "Invalid block tag: $block")
                result.addAll(tag.values().map { Material.matchMaterial(it.asString())!! })
            } else {
                val material = Material.matchMaterial(block)
                if (material == null || !material.isBlock) throw InvalidCriteriaException(this, "Invalid block: $block")
                result.add(material)
            }
        }
        blockTypes = result
    }

    @EventHandler
    fun onBreakBlock(event: BlockBreakEvent) {
        val block = event.block
        if (block.type in blockTypes) {
            trigger(event.player)
        }
    }

    companion object {
        const val BLOCK_TYPES_KEY = "block"
    }
}