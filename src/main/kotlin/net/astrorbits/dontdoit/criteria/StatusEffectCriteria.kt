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
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.potion.PotionEffectType

class StatusEffectCriteria : Criteria(), Listener, ImmediatelyTriggerInspector {
    override val type: CriteriaType = CriteriaType.STATUS_EFFECT
    lateinit var effectTypes: Set<PotionEffectType>
    var isWildcard: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setStatusEffectTypes(EFFECT_KEY) { potionEffectTypes, isWildcard ->
            this.effectTypes = potionEffectTypes
            this.isWildcard = isWildcard
        }
    }

    override fun onBind(teamData: TeamData, reason: CriteriaChangeReason) {
        super.onBind(teamData, reason)
        for (player in teamData.members) {
            if (shouldTrigger(player)) {
                TaskBuilder(DontDoIt.instance, TaskType.Delayed(Duration.ticks(1.0)))
                    .setTask { trigger(player) }
                    .runTask()
                break
            }
        }
    }

    @EventHandler
    fun onPlayerGainEffect(event: EntityPotionEffectEvent) {
        val player = event.entity as? Player ?: return
        if (isWildcard || event.newEffect?.type in effectTypes) {
            trigger(player)
        }
    }

    override fun shouldTrigger(player: Player): Boolean {
        return effectTypes.any { player.hasPotionEffect(it) } || isWildcard
    }

    companion object {
        const val EFFECT_KEY = "effect"
    }
}