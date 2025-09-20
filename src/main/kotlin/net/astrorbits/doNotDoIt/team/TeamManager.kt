package net.astrorbits.doNotDoIt.team

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

object TeamManager {
    val teams = mutableMapOf<String, TeamData>()
    var mainScoreboard: org.bukkit.scoreboard.Scoreboard? = null

    fun createTeam(name: String, color: NamedTextColor): TeamData {
        val team = TeamData(name, color)
        teams[name] = team
        return team
    }

    fun getTeamOf(uuid: UUID): TeamData? =
        teams.values.find { uuid in it.members }

    /** 初始化计分板 */
    fun initScoreboard(plugin: JavaPlugin) {
        val board = plugin.server.scoreboardManager.newScoreboard
        val objective = board.registerNewObjective("dnDoIt", "dummy", Component.text("§6挑战状态"))
        objective.displaySlot = org.bukkit.scoreboard.DisplaySlot.SIDEBAR
        mainScoreboard = board
        updateScoreboard(plugin)
    }

    /** 更新计分板显示 */
    fun updateScoreboard(plugin: JavaPlugin) {
        mainScoreboard?.let { board ->
            val objective = board.getObjective("dnDoIt") ?: return
            teams.forEach { teamData ->
                val team = teamData.value
                val line = "§${team.color}${team.name}: ${team.life}❤"
                // 每队一行，如果重复要先删除
                val score = objective.getScore(team.name)
                score.score = team.life
            }
            // 更新给所有玩家
            plugin.server.onlinePlayers.forEach { it.scoreboard = board }
        }
    }
}

private fun String.toComponent(): () -> Component {
    return { Component.text(this) }
}
