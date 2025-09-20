package net.astrorbits.doNotDoIt.criteria

import com.google.gson.JsonObject
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class PickUpCriteria : CriteriaListener(), Listener {
    lateinit var material: Material

    override fun readFromJson(json: JsonObject) {
        material = Material.valueOf(json.get("item").asString)
        super.readFromJson(json)
    }

    override fun formatName(rawName: String): String {
        return rawName.replace(BLOCK_NAME_PATTERN, "<lang:${material.translationKey()}>")
    }

    @EventHandler
    fun onBreakBlock(event: BlockBreakEvent) {

        if (event.block.type == material) {
            trigger(event.player)
        }
    }

    companion object {
        const val BLOCK_NAME_PATTERN = "{block}"
    }
}