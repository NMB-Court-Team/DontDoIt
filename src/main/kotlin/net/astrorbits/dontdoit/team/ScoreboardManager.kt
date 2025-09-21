package net.astrorbits.dontdoit.team

import net.astrorbits.dontdoit.DontDoIt
import net.kyori.adventure.text.Component
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot

class ScoreboardManager(val plugin: DontDoIt) {
    val tick = plugin.server.scheduler.scheduleSyncRepeatingTask(
        plugin,
        Runnable {
            // 每次 tick 要执行的逻辑
            updateSidebar(plugin)
        },
        0L,  // 延迟多久开始执行
        5L  // 间隔多少 tick 执行一次（20 tick = 1 秒）
    )
    fun updateSidebar(plugin: JavaPlugin) {
        val manager = plugin.server.scoreboardManager

        plugin.server.onlinePlayers.forEach { player ->
            val board = manager.newScoreboard
            val objective = board.registerNewObjective("dnDoIt", "dummy", Component.text("§6挑战状态"))
            objective.displaySlot = DisplaySlot.SIDEBAR

            TeamManager.teams.values.forEach { team ->
                // 显示任务状态，当前队伍的玩家可以选择不显示自己任务
                val taskInfo = team.currentCriteria?.displayName ?: "§7无任务"
                val line = "§${team.color} ${team.name}: $taskInfo"
                objective.getScore(line).score = 0 // score 值随意，只要每队一行
            }

            player.scoreboard = board
        }
    }

}