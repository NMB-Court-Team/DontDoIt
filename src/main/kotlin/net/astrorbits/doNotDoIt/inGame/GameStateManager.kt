package net.astrorbits.doNotDoIt.inGame

import net.astrorbits.doNotDoIt.team.TeamData
import net.astrorbits.doNotDoIt.team.TeamManager.teams
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class GameStateManager(val plugin: JavaPlugin) {
    var state: GameState = GameState.PAUSED

    fun startGame() {
        state = GameState.RUNNING
        Bukkit.broadcast(Component.text("§a游戏开始！"))
    }

    fun endGame() {
        state = GameState.FINISHED
        Bukkit.broadcast(Component.text("§c游戏结束！"))
        val winner = teams.entries.maxByOrNull { it.value.life }?.value
        plugin.server.broadcast(Component.text("§6游戏结束！胜利队伍: ${winner?.name}"))
    }

    fun pause() {
        state = GameState.PAUSED
        Bukkit.broadcast(Component.text("§e进入等待阶段"))
    }

    fun reset() {
        state = GameState.PREPARING
        Bukkit.broadcast(Component.text("§5重置"))
    }

    fun isRunning(): Boolean = state == GameState.RUNNING
    fun isWaiting(): Boolean = state == GameState.PREPARING
}