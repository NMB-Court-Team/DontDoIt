package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.WaitTimeMode
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.range.IntRange
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent
import java.util.*

class RespawnTimeCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.RESPAWN_TIME
    lateinit var waitTimeMode: WaitTimeMode
    var reviveTimeTicksRange: IntRange = IntRange.INFINITY
    var rangeReversed: Boolean = false

    val playerDeathTick: MutableMap<UUID, Int> = mutableMapOf()

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setField(WAIT_TIME_MODE_KEY) { waitTimeMode = WaitTimeMode.valueOf(it.uppercase()) }
        data.setIntRangeField(REVIVE_TIME_TICKS_RANGE_KEY, true) { reviveTimeTicksRange = it }
        data.setBoolField(RANGE_REVERSED_KEY, true) { rangeReversed = it }
    }

    override fun tick(teamData: TeamData) {
        if (waitTimeMode != WaitTimeMode.STAY) return
        for (player in teamData.members) {
            if (!player.isDead) continue
            val uuid = player.uniqueId
            val deathTick = playerDeathTick[uuid] ?: continue
            val currentTick = Bukkit.getCurrentTick()
            if (((currentTick - deathTick) in reviveTimeTicksRange) xor rangeReversed) {
                trigger(player)
                continue
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        playerDeathTick[event.player.uniqueId] = Bukkit.getCurrentTick()
    }

    @EventHandler
    fun onPlayerRevive(event: PlayerRespawnEvent) {
        if (waitTimeMode != WaitTimeMode.DELAY) return
        val uuid = event.player.uniqueId
        val deathTick = playerDeathTick[uuid] ?: return
        val currentTick = Bukkit.getCurrentTick()
        if (((currentTick - deathTick) in reviveTimeTicksRange) xor rangeReversed) {
            trigger(event.player)
        }
    }

    companion object {
        const val WAIT_TIME_MODE_KEY = "mode"
        const val REVIVE_TIME_TICKS_RANGE_KEY = "time"
        const val RANGE_REVERSED_KEY = "reversed"
    }
}
