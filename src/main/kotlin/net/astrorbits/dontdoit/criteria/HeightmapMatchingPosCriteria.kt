package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.HeightMap

class HeightmapMatchingPosCriteria : Criteria() {
    override val type: CriteriaType = CriteriaType.HEIGHTMAP_MATCHING_POS
    lateinit var heightMap: HeightMap
    var reversed: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setField(HEIGHTMAP_KEY) { value ->
            val heightMapValue = HeightMap.valueOf(value.uppercase())
            if (heightMapValue == HeightMap.OCEAN_FLOOR_WG || heightMapValue == HeightMap.WORLD_SURFACE_WG) {
                throw InvalidCriteriaException(this, "Heightmap for worldgen is not allowed")
            }
            heightMap = heightMapValue
        }
        data.setBoolField(REVERSED_KEY, true) { reversed = it }
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if ((player.eyeLocation.y >= player.location.toHighestLocation(heightMap).y) xor reversed) {
                trigger(player)
                break
            }
        }
    }

    companion object {
        const val HEIGHTMAP_KEY = "heightmap"
        const val REVERSED_KEY = "reversed"
    }
}