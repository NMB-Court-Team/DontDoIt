package net.astrorbits.doNotDoIt.criteria

import com.google.gson.JsonObject
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BreakBlockCriteria : Criteria(), Listener {
    override val type = CriteriaType.BREAK_BLOCK
    lateinit var blockTypes: List<Material>

    @EventHandler
    fun onBreakBlock(event: BlockBreakEvent) {
        val player = event.player
        if (event.block.type in blockTypes) {
            trigger(player)
        }
    }

    override fun readData(json: JsonObject) {
        super.readData(json)

        val blockString = json.get("block").asString
        if (blockString.startsWith("#")) {
            BlockTypeTagKeys.


        } else {
            val blockType = Material.getMaterial(blockString) ?: throw IllegalArgumentException("Invalid block")
            blockTypes = listOf(blockType)
        }
    }
}