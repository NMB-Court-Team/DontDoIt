package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.ImmediatelyTriggerInspector
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.range.FloatRange
import org.bukkit.entity.Player

class RotationCriteria : Criteria(), ImmediatelyTriggerInspector {
    override val type: CriteriaType = CriteriaType.ROTATION
    var yawRange: FloatRange = FloatRange.INFINITY
    var yawReversed: Boolean = false
    var pitchRange: FloatRange = FloatRange.INFINITY
    var pitchReversed: Boolean = false

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (shouldTrigger(player)) {
                trigger(player)
                break
            }
        }
    }

    override fun shouldTrigger(player: Player): Boolean {
        return ((player.yaw in yawRange) xor yawReversed)
            && ((player.pitch in pitchRange) xor pitchReversed)
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setFloatRangeField(YAW_RANGE_KEY, true) { yawRange = it }
        data.setBoolField(YAW_REVERSED_KEY, true) { yawReversed = it }
        data.setFloatRangeField(PITCH_RANGE_KEY, true) { pitchRange = it }
        data.setBoolField(PITCH_REVERSED_KEY, true) { pitchReversed = it }
    }

    fun isYawOnly(): Boolean {
        return (pitchRange.max > 90 && pitchRange.min < -90 && !pitchReversed)
            || (pitchRange.max - pitchRange.min <= 0 && pitchReversed)
    }

    // 控制一下概率，纯yaw的词条是完全自爆，而且一写会写四个（说的就是面朝东南西北这四个词条）
    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getAnyTriggersMultiplier(bindTarget) { if (isYawOnly()) YAW_ONLY_MULTIPLIER else 1.0 }
    }

    companion object {
        const val YAW_RANGE_KEY = "yaw"
        const val YAW_REVERSED_KEY = "yaw_reversed"
        const val PITCH_RANGE_KEY = "pitch"
        const val PITCH_REVERSED_KEY = "pitch_reversed"

        const val YAW_ONLY_MULTIPLIER = 0.25
    }
}