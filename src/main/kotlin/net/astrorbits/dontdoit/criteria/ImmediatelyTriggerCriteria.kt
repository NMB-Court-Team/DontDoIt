package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.ImmediatelyTriggerInspector
import net.astrorbits.dontdoit.system.CriteriaChangeReason
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.math.Duration
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType
import org.bukkit.entity.Player

class ImmediatelyTriggerCriteria : Criteria(), ImmediatelyTriggerInspector {
    override val type: CriteriaType = CriteriaType.IMMEDIATELY_TRIGGER

    override fun onBind(teamData: TeamData, reason: CriteriaChangeReason) {
        super.onBind(teamData, reason)
        TaskBuilder(DontDoIt.instance, TaskType.Delayed(Duration.ticks(1.0)))
            .setTask { trigger(teamData) }
            .runTask()
    }

    override fun shouldTrigger(player: Player): Boolean {
        return true
    }
}