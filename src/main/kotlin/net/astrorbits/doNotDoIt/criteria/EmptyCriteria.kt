package net.astrorbits.doNotDoIt.criteria

import com.google.gson.JsonObject
import org.bukkit.Material
import org.bukkit.event.Listener

class EmptyCriteria : Criteria(), Listener {
    lateinit var types: List<Material>

    override fun readData(json: JsonObject) {
        val list = mutableListOf<Material>()
        if (json.has("params")) {
            val params = json.getAsJsonArray("params")
            for (el in params) {
                list.add(Material.valueOf(el.asString.uppercase()))
            }
        }
        types = list
        super.readData(json)
    }
}