package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.lib.range.IntRange
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent
import java.util.*

class RespawnTimeCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.RESPAWN_TIME
    lateinit var mode: Mode
    var reviveTimeTicksRange: IntRange = IntRange.INFINITY
    var rangeReversed: Boolean = false

    val playerDeathServerTick: MutableMap<UUID, Int> = mutableMapOf()

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setField(MODE_KEY) { mode = Mode.valueOf(it.uppercase()) }
        data.setIntRangeField(REVIVE_TIME_TICKS_RANGE_KEY, true) { reviveTimeTicksRange = it }
        data.setBoolField(RANGE_REVERSED_KEY, true) { rangeReversed = it }
    }

    override fun tick(teamData: TeamData) {
        if (mode != Mode.STAY_DEATH_TIME) return
        for (player in teamData.members) {
            val uuid = player.uniqueId
            val deathTick = playerDeathServerTick[uuid] ?: continue
            val currentTick = Bukkit.getCurrentTick()
            if (((currentTick - deathTick) in reviveTimeTicksRange) xor rangeReversed) {
                trigger(player)
                continue
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        playerDeathServerTick[event.player.uniqueId] = Bukkit.getCurrentTick()
    }

    @EventHandler
    fun onPlayerRevive(event: PlayerRespawnEvent) {
        if (mode != Mode.RESPAWN_DELAY) return
        val uuid = event.player.uniqueId
        val deathTick = playerDeathServerTick[uuid] ?: return
        val currentTick = Bukkit.getCurrentTick()
        if (((currentTick - deathTick) in reviveTimeTicksRange) xor rangeReversed) {
            trigger(event.player)
        }
    }

    enum class Mode {
        RESPAWN_DELAY, STAY_DEATH_TIME
    }

    companion object {
        const val MODE_KEY = "mode"
        const val REVIVE_TIME_TICKS_RANGE_KEY = "time"
        const val RANGE_REVERSED_KEY = "time_reversed"
    }
}
