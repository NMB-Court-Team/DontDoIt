package net.astrorbits.dontdoit.system

import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.criteria.system.CriteriaManager
import net.astrorbits.dontdoit.system.generate.GameAreaGenerator
import net.astrorbits.dontdoit.system.team.TeamManager
import net.astrorbits.lib.math.vector.Vec3d
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType
import net.astrorbits.lib.text.LegacyText
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

object GameStateManager {
    private val LOGGER = DontDoIt.LOGGER

    var state: GameState = GameState.PREPARING
        private set
    private var tickTask: BukkitTask? = null

    fun init() {
        tickTask = TaskBuilder(DontDoIt.instance, TaskType.Tick)
            .setTask { tick() }
            .runTask()
    }

    fun startGame(starter: Player) {
        state = GameState.RUNNING
        GameAreaGenerator.generate(Vec3d.fromLocation(starter.location).floor(), starter.world)
        CriteriaManager.updateYLevelCriteria(GameAreaGenerator.groundYLevel!!)
        CriteriaManager.updateUserDefinedCriteria(Preparation.customCriteriaNames)
        TeamManager.onStartGame()

        Bukkit.broadcast(LegacyText.toComponent("§a游戏开始！"))
    }

    fun endGame() {
        state = GameState.FINISHED
        val winner = TeamManager.getWinner()

        DontDoIt.server.broadcast(LegacyText.toComponent("§6游戏结束！胜利队伍: ${winner?.teamName}"))
    }

    fun reset() {
        state = GameState.PREPARING
        Preparation.onEnterPreparation()
        TeamManager.onEnterPreparation()

        Bukkit.broadcast(LegacyText.toComponent("§e已重置游戏状态"))
    }

    fun tick() {
        TeamManager.tick(state)
        if (isWaiting()) {
            Preparation.tick()
        }
    }

    fun onDisable() {
        tickTask?.cancel()
        tickTask = null
    }

    fun isRunning(): Boolean = state == GameState.RUNNING
    fun isWaiting(): Boolean = state == GameState.PREPARING
}