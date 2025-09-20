package net.astrorbits.doNotDoIt.system

import net.astrorbits.doNotDoIt.DoNotDoIt
import net.astrorbits.doNotDoIt.criteria.Criteria
import net.astrorbits.doNotDoIt.team.TeamManager.teams
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType
import net.astrorbits.lib.text.LegacyText
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.ScoreboardManager
import org.bukkit.scoreboard.Team

object GameStateManager {
    private val LOGGER = DoNotDoIt.LOGGER

    var state: GameState = GameState.PAUSED
    private lateinit var tickTask: BukkitTask

    fun init() {
        tickTask = TaskBuilder(DoNotDoIt.instance, TaskType.Tick)
            .setTask { tick() }
            .runTask()
    }

    fun startGame() {
        state = GameState.RUNNING
        Bukkit.broadcast(LegacyText.toComponent("§a游戏开始！"))
    }

    fun endGame() {
        state = GameState.FINISHED
        Bukkit.broadcast(LegacyText.toComponent("§c游戏结束！"))
        val winner = teams.entries.maxByOrNull { it.value.life }?.value
        DoNotDoIt.server.broadcast(LegacyText.toComponent("§6游戏结束！胜利队伍: ${winner?.name}"))
    }

    fun pause() {
        state = GameState.PAUSED
        Bukkit.broadcast(LegacyText.toComponent("§e进入等待阶段"))
    }

    fun reset() {
        state = GameState.PREPARING
        Bukkit.broadcast(LegacyText.toComponent("§5重置"))
    }

    fun tick() {

    }

    fun isRunning(): Boolean = state == GameState.RUNNING
    fun isWaiting(): Boolean = state == GameState.PREPARING
}