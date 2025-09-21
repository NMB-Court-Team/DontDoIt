package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.lib.math.vector.Vec3d
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
            val world = player.world
            val pos = Vec3d.fromLocation(player.location)
            val highestPos = pos.setY(world.getHighestBlockYAt(player.location).toDouble())
            if ((if (reversed) -1 else 1) * (highestPos.squaredDistanceTo(pos) - POS_MATCHING_DISTANCE) <= 0) {
                trigger(player)
                break
            }
        }
    }

    companion object {
        const val HEIGHTMAP_KEY = "heightmap"
        const val REVERSED_KEY = "reversed"
        const val POS_MATCHING_DISTANCE = 0.2 * 0.2
    }
}