package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.team.TeamData
import kotlin.properties.Delegates

class FallCriteria : Criteria() {
    override val type: CriteriaType = CriteriaType.FALL
    var distance: Double by Delegates.notNull()

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setDoubleField(DISTANCE_KEY) { distance = it }
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (player.fallDistance >= distance) {
                trigger(player)
                break
            }
        }
    }

    companion object {
        const val DISTANCE_KEY = "distance"
    }
}