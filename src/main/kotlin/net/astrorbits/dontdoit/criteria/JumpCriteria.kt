package net.astrorbits.dontdoit.criteria

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.CriteriaChangeReason
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.UUID

class JumpCriteria : Criteria(), Listener {
    override val type = CriteriaType.JUMP
    var count: Int = 1
    var jumpCount = mutableMapOf<UUID, Int>()

    override fun onBind(teamData: TeamData, reason: CriteriaChangeReason) {
        super.onBind(teamData, reason)
        for (player in teamData.members){
            jumpCount[player.uniqueId] = 0
        }
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setIntField(COUNT_KEY, true) { count = it }
    }

    @EventHandler
    fun onJump(event: PlayerJumpEvent) {
        val player = event.player
        val currentCount = jumpCount.getOrPut(player.uniqueId) { 0 }
        val newCount = currentCount + 1
        jumpCount[player.uniqueId] = newCount

        if (newCount >= count) {
            trigger(player)
        }
    }

    companion object {
        const val COUNT_KEY = "count"
    }
}