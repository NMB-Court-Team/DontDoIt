package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.MoveType
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.CriteriaChangeReason
import net.astrorbits.dontdoit.system.team.TeamData
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

    private val startTick: MutableMap<UUID, Int> = mutableMapOf()
    private val lastState: MutableMap<UUID, Boolean> = mutableMapOf()

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setField(MOVE_TYPE_KEY) { moveType = MoveType.valueOf(it) }
        data.setIntField(MOVE_TIME_KEY) { moveTime = it }
        data.setBoolField(REVERSED_KEY, true) { reversed = it }

        if (moveType == MoveType.Jump && !reversed) {
            throw InvalidCriteriaException(this, "Jump 不能设置 reversed = false")
        }
    }

    override fun onUnbind(teamData: TeamData, reason: CriteriaChangeReason): Boolean {
        for (player in teamData.members) {
            startTick.remove(player.uniqueId)
            lastState.remove(player.uniqueId)
        }
        return super.onUnbind(teamData, reason)
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            val uuid = player.uniqueId
            val currentState = isInState(player)
            val prevState = lastState[uuid]

            // 初始化
            if (prevState == null) {
                lastState[uuid] = currentState
                if (currentState) startTick[uuid] = Bukkit.getCurrentTick()
                continue
            }

            // 状态变化
            if (!prevState && currentState) {
                startTick[uuid] = Bukkit.getCurrentTick()
            } else if (prevState && !currentState) {
                startTick.remove(uuid)
            }
            lastState[uuid] = currentState

            // 检查时间
            val start = startTick[uuid] ?: continue
            if (Bukkit.getCurrentTick() - start >= moveTime) {
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
