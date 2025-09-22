package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.MoveType
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.CriteriaChangeReason
import net.astrorbits.dontdoit.team.TeamData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.UUID
import kotlin.properties.Delegates

class MoveTimeCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.MOVE_TIME
    lateinit var moveType: MoveType
    var moveTime: Int by Delegates.notNull()
    var reversed: Boolean = false

    val startInStateTick: MutableMap<UUID, Int> = mutableMapOf()
    val prevInState: MutableMap<UUID, Boolean> = mutableMapOf()

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setField(MOVE_TYPE_KEY) { moveType = MoveType.valueOf(it) }
        data.setIntField(MOVE_TIME_KEY) { moveTime = it }
        data.setBoolField(REVERSED_KEY, true) { reversed = it }
        if (moveType === MoveType.Jump && !reversed) throw InvalidCriteriaException(this, "Definition 'type: jump' with 'reversed: false' is not supported")
    }

    override fun onUnbind(teamData: TeamData, reason: CriteriaChangeReason) {
        super.onUnbind(teamData, reason)
        for (player in teamData.members) {
            val uuid = player.uniqueId
            startInStateTick.remove(uuid)
            prevInState.remove(uuid)
        }
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            val uuid = player.uniqueId
            // 更新状态
            val prevInState = this.prevInState.computeIfAbsent(uuid) { reversed }
            val currentInState = isInState(player)
            if (!prevInState && currentInState) {
                startInStateTick[uuid] = Bukkit.getCurrentTick()
            } else if (prevInState && !currentInState) {
                startInStateTick.remove(uuid)
            }
            this.prevInState[uuid] = currentInState

            // 检查时间
            val startTick = startInStateTick[uuid] ?: continue
            val currentTick = Bukkit.getCurrentTick()
            if (currentTick - startTick > moveTime) {
                trigger(player)
                break
            }
        }
    }

    fun isInState(player: Player): Boolean {
        return moveType.isMoving(player) xor reversed
    }

    companion object {
        const val MOVE_TYPE_KEY = "type"
        const val MOVE_TIME_KEY = "time"
        const val REVERSED_KEY = "reversed"
    }
}

// isSprinting, reversed -> isInState
// true, false -> true
// true, true -> false
// false, false -> false
// false, true -> true
