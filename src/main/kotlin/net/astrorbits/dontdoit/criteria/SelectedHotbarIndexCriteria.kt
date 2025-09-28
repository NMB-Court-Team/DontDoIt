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
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent

class SelectedHotbarIndexCriteria : Criteria(), Listener, ImmediatelyTriggerInspector {
    override val type: CriteriaType = CriteriaType.SELECTED_HOTBAR_INDEX
    var index: Int = 0

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setIntField(SLOT_KEY, false) { index = it - 1 }
        if (index !in 0..8) throw InvalidCriteriaException(this, "Invalid slot: ${index + 1}")
    }

    override fun onBind(teamData: TeamData, reason: CriteriaChangeReason) {
        super.onBind(teamData, reason)
        for (player in teamData.members){
            if (shouldTrigger(player)) {
                TaskBuilder(DontDoIt.instance, TaskType.Delayed(Duration.ticks(1.0)))
                    .setTask { trigger(player) }
                    .runTask()
                break
            }
        }
    }

    @EventHandler
    fun onSelectedHotbarChange(event: PlayerItemHeldEvent) {
        if (event.newSlot == index) {
            trigger(event.player)
        }
    }

    override fun shouldTrigger(player: Player): Boolean {
        return player.inventory.heldItemSlot == index
    }

    companion object {
        const val SLOT_KEY = "slot"
    }
}