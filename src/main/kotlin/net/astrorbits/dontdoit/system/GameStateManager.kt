package net.astrorbits.dontdoit.system

import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.team.TeamManager.teams
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType
import net.astrorbits.lib.text.LegacyText
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask

object GameStateManager {
    private val LOGGER = DontDoIt.LOGGER

    var state: GameState = GameState.PAUSED
    private lateinit var tickTask: BukkitTask

    fun init() {
        tickTask = TaskBuilder(DontDoIt.instance, TaskType.Tick)
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
        DontDoIt.server.broadcast(LegacyText.toComponent("§6游戏结束！胜利队伍: ${winner?.name}"))
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