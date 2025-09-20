package net.astrorbits.doNotDoIt.team

import net.astrorbits.doNotDoIt.criteria.CriteriaData
import net.astrorbits.doNotDoIt.criteria.CriteriaType
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

data class TeamData(
    val name: String,
    val color: TeamColor,
    val members: MutableSet<UUID> = mutableSetOf(),
    var life: Int = 10,
    var criteriaData: CriteriaData? = null,
    var criteriaCompleted: Boolean = false,
    var dead: Boolean = false
) {
    fun setCriteria(plugin: JavaPlugin, criteriaData: CriteriaData) {
        this.criteriaData = criteriaData
        TeamManager.updateScoreboard(plugin)
    }
    fun loseLife(plugin: JavaPlugin, amount: Int = 1) {
        life -= amount
        if (life <= 0) {
            death(plugin)
        }
        TeamManager.updateScoreboard(plugin)
    }

    fun death(plugin: JavaPlugin){
        this.dead = true
        members.forEach { uuid ->
            val player = plugin.server.getPlayer(uuid)?:return
            player.isInvulnerable = true
            player.sendMessage("§c你的队伍已失败，进入旁观模式")
        }
    }

    fun isDead(): Boolean{ return this.dead }
}
