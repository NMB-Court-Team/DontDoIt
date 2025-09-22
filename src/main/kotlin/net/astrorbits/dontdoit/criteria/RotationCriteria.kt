package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.lib.range.FloatRange

class RotationCriteria : Criteria() {
    override val type: CriteriaType = CriteriaType.Rotation
    var yawRange: FloatRange = FloatRange.INFINITY
    var pitchRange: FloatRange = FloatRange.INFINITY
    var reversed: Boolean = false

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if ( ( player.yaw in yawRange && player.pitch in pitchRange ) xor reversed) {
                trigger(player)
                break
            }
        }
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setFloatRangeField(YAW_RANGE_KEY, true) { yawRange = it }
        data.setFloatRangeField(PITCH_RANGE_KEY, true) { pitchRange = it }
        data.setBoolField(REVERSED_KEY, true) { reversed = it }
    }

    companion object {
        const val YAW_RANGE_KEY = "yaw"
        const val PITCH_RANGE_KEY = "pitch"
        const val REVERSED_KEY = "reversed"
    }
}