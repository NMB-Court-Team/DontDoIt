package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectable
import net.astrorbits.dontdoit.system.CriteriaChangeReason
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.math.Duration
import net.astrorbits.lib.range.IntRange
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType

class LifeCountCriteria : Criteria(), InventoryInspectable {
    override val type: CriteriaType = CriteriaType.LIFE_COUNT
    var countRange: IntRange = IntRange.INFINITY
    var rangeReversed: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setIntRangeField(COUNT_RANGE_KEY, true) { countRange = it }
        data.setBoolField(RANGE_REVERSED_KEY, true) { rangeReversed = it }
    }

    override fun onBind(teamData: TeamData, reason: CriteriaChangeReason) {
        super.onBind(teamData, reason)
        if ((teamData.lifeCount in countRange) xor rangeReversed) {
            TaskBuilder(DontDoIt.instance, TaskType.Delayed(Duration.ticks(1.0)))
                .setTask { trigger(teamData) }
                .runTask()
        }
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * MULTIPLIER
    }

    companion object {
        const val COUNT_RANGE_KEY = "count"
        const val RANGE_REVERSED_KEY = "reversed"

        const val MULTIPLIER = 0.8
    }
}