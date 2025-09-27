package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.helper.WaitTimeMode
import net.astrorbits.dontdoit.system.CriteriaChangeReason
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.range.IntRange
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit

class CriteriaHoldTimeCriteria : Criteria() {
    override val type: CriteriaType = CriteriaType.CRITERIA_HOLD_TIME
    lateinit var waitTimeMode: WaitTimeMode
    var changeTimeTicksRange: IntRange = IntRange.INFINITY
    var rangeReversed: Boolean = false

    val bindTick: MutableMap<NamedTextColor, Int> = mutableMapOf()

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setField(WAIT_TIME_MODE_KEY) { waitTimeMode = WaitTimeMode.valueOf(it.uppercase()) }
        data.setIntRangeField(CHANGE_TIME_TICKS_RANGE_KEY, true) { changeTimeTicksRange = it }
        data.setBoolField(RANGE_REVERSED_KEY, true) { rangeReversed = it }
    }

    override fun onBind(teamData: TeamData, reason: CriteriaChangeReason) {
        super.onBind(teamData, reason)
        bindTick[teamData.color] = Bukkit.getCurrentTick()
    }

    override fun onUnbind(teamData: TeamData, reason: CriteriaChangeReason): Boolean {
        if (waitTimeMode == WaitTimeMode.DELAY && (reason == CriteriaChangeReason.AUTO || reason.isGuess())) {
            val bindTick = bindTick[teamData.color]
            if (bindTick != null) {
                val currentTick = Bukkit.getCurrentTick()
                if (((currentTick - bindTick) in changeTimeTicksRange) xor rangeReversed) {
                    trigger(teamData)
                    return false
                }
            }
        }
        return super.onUnbind(teamData, reason)
    }

    override fun tick(teamData: TeamData) {
        if (waitTimeMode != WaitTimeMode.STAY) return
        val bindTick = bindTick[teamData.color] ?: return
        val currentTick = Bukkit.getCurrentTick()
        if (((currentTick - bindTick) in changeTimeTicksRange) xor rangeReversed) {
            trigger(teamData)
        }
    }

    companion object {
        const val WAIT_TIME_MODE_KEY = "mode"
        const val CHANGE_TIME_TICKS_RANGE_KEY = "time"
        const val RANGE_REVERSED_KEY = "reversed"
    }
}