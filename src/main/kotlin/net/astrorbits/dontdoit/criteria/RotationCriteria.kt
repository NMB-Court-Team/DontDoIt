package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.system.CriteriaType
import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.lib.range.FloatRange

class RotationCriteria : Criteria() {
    override val type: CriteriaType = CriteriaType.ROTATION
    var yawRange: FloatRange = FloatRange.INFINITY
    var yawReversed: Boolean = false
    var pitchRange: FloatRange = FloatRange.INFINITY
    var pitchReversed: Boolean = false

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (((player.yaw in yawRange) xor yawReversed) && ((player.pitch in pitchRange) xor pitchReversed)) {
                trigger(player)
                break
            }
        }
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setFloatRangeField(YAW_RANGE_KEY, true) { yawRange = it }
        data.setBoolField(YAW_REVERSED_KEY, true) { yawReversed = it }
        data.setFloatRangeField(PITCH_RANGE_KEY, true) { pitchRange = it }
        data.setBoolField(PITCH_REVERSED_KEY, true) { pitchReversed = it }
    }

    companion object {
        const val YAW_RANGE_KEY = "yaw"
        const val YAW_REVERSED_KEY = "yaw_reversed"
        const val PITCH_RANGE_KEY = "pitch"
        const val PITCH_REVERSED_KEY = "pitch_reversed"
    }
}