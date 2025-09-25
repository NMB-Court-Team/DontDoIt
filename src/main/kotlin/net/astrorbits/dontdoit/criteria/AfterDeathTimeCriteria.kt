package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.CriteriaChangeReason
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.UUID
import kotlin.properties.Delegates

class AfterDeathTimeCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.AFTER_DEATH_TIME
    var time: Int by Delegates.notNull()

    val deathTick: MutableMap<UUID, Int> = mutableMapOf()

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setIntField(TIME_KEY) { time = it }
    }

    override fun onBind(teamData: TeamData, reason: CriteriaChangeReason) {
        super.onBind(teamData, reason)
        for (player in teamData.members) {
            deathTick.remove(player.uniqueId)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        deathTick[event.player.uniqueId] = Bukkit.getCurrentTick()
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            val deathTick = this.deathTick[player.uniqueId] ?: continue
            val currentTick = Bukkit.getCurrentTick()
            if (currentTick - deathTick > time) {
                trigger(player)
                break
            }
        }
    }

    companion object {
        const val TIME_KEY = "time"
    }
}