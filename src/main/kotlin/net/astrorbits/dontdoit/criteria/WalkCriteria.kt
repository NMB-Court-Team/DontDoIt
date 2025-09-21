package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.team.TeamData
import java.util.UUID
import kotlin.math.floor
import kotlin.properties.Delegates

class WalkCriteria : Criteria() {
    override val type: CriteriaType = CriteriaType.WALK
    var walkDistance by Delegates.notNull<Int>()

    private val playerOnBindWalkDistance: MutableMap<UUID, Int> = mutableMapOf()

    override fun onBind(teamData: TeamData) {
        super.onBind(teamData)
    }




    override fun readData(data: Map<String, String>) {
        super.readData(data)
        val walkDistanceString = data[WALK_DISTANCE_KEY] ?: throw InvalidCriteriaException(this, "Missing key '$WALK_DISTANCE_KEY'")
        val walkDistanceM = try {
            walkDistanceString.toFloat()
        } catch (e: NumberFormatException) {
            throw InvalidCriteriaException(this, "Invalid walk distance: $walkDistanceString")
        }
        walkDistance = floor(walkDistanceM * 100).toInt()
    }

    companion object {
        const val WALK_DISTANCE_KEY = "distance"
    }
}