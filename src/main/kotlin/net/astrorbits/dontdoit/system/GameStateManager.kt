package net.astrorbits.dontdoit.system

import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.criteria.system.CriteriaManager
import net.astrorbits.dontdoit.system.generate.GameAreaGenerator
import net.astrorbits.dontdoit.system.team.TeamData.Companion.PLAYER_NAME_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamData.Companion.TEAM_NAME_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamManager
import net.astrorbits.lib.math.vector.Vec3d
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType
import net.astrorbits.lib.text.SimpleTextBuilder
import net.astrorbits.lib.text.TextHelper.format
import net.astrorbits.lib.text.TitleHelper
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Difficulty
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
        TeamManager.onGameStart()

        Bukkit.broadcast(Configs.START_GAME_MESSAGE.get())
        TitleHelper.broadcastTitle(Title.title(
            Configs.START_GAME_MESSAGE.get(),
            Component.empty(),
            5, 50, 10
        ))
        Bukkit.getOnlinePlayers().forEach { player ->
            player.isInvulnerable = false
            player.allowFlight = false
            player.inventory.clear()
        }
    }

    const val TRIGGER_COUNT_PLACEHOLDER = "count"

    fun endGame() {
        state = GameState.FINISHED
        val winner = TeamManager.getWinner()
        if (winner == null) {
            Bukkit.broadcast(Configs.DRAW_GAME_MESSAGE.get())
            TitleHelper.broadcastTitle(Title.title(
                Configs.DRAW_GAME_MESSAGE.get(),
                Component.empty(),
                0, 100, 20
            ))
        } else {
            Bukkit.broadcast(Configs.END_GAME_ANNOUNCE.get().format(TEAM_NAME_PLACEHOLDER to winner.teamName))
            TitleHelper.broadcastTitle(Title.title(
                Configs.END_GAME_TITLE.get().format(TEAM_NAME_PLACEHOLDER to winner.teamName),
                Configs.END_GAME_SUBTITLE.get().format(TEAM_NAME_PLACEHOLDER to winner.teamName),
                0, 100, 20
            ))
        }
        val builder = SimpleTextBuilder()
        builder.append(Configs.TRIGGER_COUNT_HEAD.get())
        for (team in TeamManager.getInUseTeams().values) {
            for (player in team.members) {
                builder.append(Configs.TRIGGER_COUNT_BODY.get().format(
                    PLAYER_NAME_PLACEHOLDER to player.displayName(),
                    TRIGGER_COUNT_PLACEHOLDER to (CriteriaManager.triggerCountStat[player.uniqueId] ?: 0)
                ))
            }
        }
        val triggerCountMessage = builder.append(Configs.TRIGGER_COUNT_TAIL.get()).build()
        Bukkit.broadcast(triggerCountMessage)

        TeamManager.onGameEnd()
    }

    fun reset() {
        state = GameState.PREPARING
        Preparation.onEnterPreparation()
        TeamManager.onEnterPreparation()
        GameAreaGenerator.onEnterPreparation()

        Bukkit.broadcast(Configs.RESET_GAME_MESSAGE.get())
    }

    fun tick() {
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