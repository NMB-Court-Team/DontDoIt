package net.astrorbits.dontdoit.system.generate

import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.lib.config.Config
import net.astrorbits.lib.config.ConfigData
import org.bukkit.Material

class BlockGenerationConfigData(
    key: String,
    val blockType: Material,
    value: List<GenerationEntry>,
    defaultValue: List<GenerationEntry>
) : ConfigData<BlockGeneration>(key, BlockGeneration(blockType, value), BlockGeneration(blockType, defaultValue)) {
    constructor(key: String, blockType: Material, defaultValue: List<GenerationEntry>) : this(key, blockType, defaultValue, defaultValue)

    override fun getFromConfig(config: Config): BlockGeneration? {
        if (key !in config) return null
        val generationEntries: MutableList<GenerationEntry> = mutableListOf()
        val entries = config.yamlConfig.getMapList(key).map { map -> map.mapKeys { it.key.toString() }.mapValues { it.value.toString() } }
        for (data in entries) {
            try {
                val entry = GenerationEntry.fromMap(data)
                generationEntries.add(entry)
            } catch (e: IllegalArgumentException) {
                DontDoIt.LOGGER.warn("Invalid block generation data: ", e)
            }
        }
        return BlockGeneration(blockType, generationEntries)
    }
}