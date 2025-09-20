package net.astrorbits.doNotDoIt.criteria

import com.google.gson.JsonObject
import org.bukkit.Material
import org.bukkit.event.Listener

class ConsumedItemCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.CONSUMED_ITEM
    lateinit var itemTypes: List<Material>

    override fun readData(json: JsonObject) {
        super.readData(json)
        val item =
    }
}