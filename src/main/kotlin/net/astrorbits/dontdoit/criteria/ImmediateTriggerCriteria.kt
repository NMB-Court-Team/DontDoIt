package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.CriteriaChangeReason
import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.lib.math.Duration
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType

class ImmediateTriggerCriteria : Criteria() {
    override val type: CriteriaType = CriteriaType.IMMEDIATE_TRIGGER

    override fun onBind(teamData: TeamData, reason: CriteriaChangeReason) {
        super.onBind(teamData, reason)
        TaskBuilder(DontDoIt.instance, TaskType.Delayed(Duration.ticks(1.0)))
            .setTask { trigger(teamData) }
            .runTask()
    }
}