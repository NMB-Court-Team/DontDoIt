package net.astrorbits.dontdoit.system.team

import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.DynamicSettings
import net.astrorbits.dontdoit.criteria.Criteria
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.system.CriteriaManager
import net.astrorbits.dontdoit.system.CriteriaChangeReason
import net.astrorbits.dontdoit.system.generate.GameAreaGenerator
import net.astrorbits.lib.StringHelper.isUuid
import net.astrorbits.lib.math.Duration
import net.astrorbits.lib.scoreboard.SidebarDisplay
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType
import net.astrorbits.lib.task.Timer
import net.astrorbits.lib.task.TimerType
import net.astrorbits.lib.text.TextHelper.append
import net.astrorbits.lib.text.TextHelper.clickRunCommand
import net.astrorbits.lib.text.TextHelper.format
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team
import kotlin.math.ceil

class TeamData(val color: NamedTextColor) {
    val team: Team
    val teamId: String
        get() = team.name
    val teamName: Component
        get() = team.displayName()
    val members: List<Player>
        get() = team.entries.mapNotNull { name -> if (name.isUuid()) null else Bukkit.getPlayer(name) }
    val memberCount: Int
        get() = team.entries.size
    val hasMember: Boolean
        get() = members.isNotEmpty()

    var isInUse: Boolean = false
    var lifeCount: Int = DynamicSettings.lifeCount
        private set
    val isEliminated: Boolean
        get() = lifeCount <= 0
    val sidebarDisplay: SidebarDisplay = SidebarDisplay(Configs.SIDEBAR_TITLE.get())
    var criteria: Criteria? = null
        set(value) {
            field = value
            TeamManager.updateSidebars()
        }
    val mainTimer: Timer = object : Timer(DontDoIt.instance, Duration.seconds(Configs.AUTO_CHANGE_CRITERIA_TIME.get().toDouble()), TimerType.COUNTDOWN) {
        override fun onStart() { }

        override fun onTick() {
            members.forEach { it.sendActionBar(Configs.INGAME_ACTIONBAR.get().format(
                CHANGE_CRITERIA_TIME_LEFT_PLACEHOLDER to ceil(currentTime.seconds).toInt(),
                LIFE_COUNT_PLACEHOLDER to lifeCount
            )) }
            criteria?.tick(this@TeamData)

            // 自定义词条消息
            if (criteria?.type != CriteriaType.USER_DEFINED) return
            if ((currentTimeTicks - CUSTOM_CRITERIA_ANNOUNCE_DELAY_SEC * 20) % (CUSTOM_CRITERIA_ANNOUNCE_COOLDOWN_SEC * 20) == 0) {
                broadcastOtherTeams(
                    Component.empty().append(
                        Configs.HOLDING_CUSTOM_CRITERIA_MESSAGE.get().format(
                            TEAM_NAME_PLACEHOLDER to teamName,
                            CRITERIA_DISPLAY_NAME_PLACEHOLDER to criteria?.displayText?.color(color)
                        )
                    ).appendNewline().append(
                        Configs.HOLDING_CUSTOM_CRITERIA_CLICK_MESSAGE.get()
                            .clickRunCommand("/criteria trigger $teamId")
                    )
                )
            }
        }

        override fun onStop() {
            val oldCriteria = criteria
            val unbindResult = criteria?.onUnbind(this@TeamData, CriteriaChangeReason.AUTO)
            if (unbindResult != false) {
                criteria = CriteriaManager.getRandomCriteria(this@TeamData, oldCriteria)
                criteria!!.onBind(this@TeamData, CriteriaChangeReason.AUTO)
            }

            if (oldCriteria != null) {
                Bukkit.broadcast(Configs.AUTO_CHANGE_CRITERIA_ANNOUNCE.get().format(
                    TEAM_NAME_PLACEHOLDER to teamName,
                    CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria.displayText.color(color)
                ))
                broadcastTitle(Title.title(
                    Configs.AUTO_CHANGE_CRITERIA_TITLE.get().color(color),
                    Configs.AUTO_CHANGE_CRITERIA_SUBTITLE.get().format(
                        CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria.displayText.color(color)
                    ),
                    5, 50, 10
                ))
            }

            requestStartAfterStop()
            LOGGER.info("Team $teamId auto changed criteria from ${oldCriteria?.displayName} to ${criteria!!.displayName}")
        }
    }

    init {
        val teamName = Configs.getTeamName(color)
        val team = sidebarDisplay.scoreboard.registerNewTeam(TeamManager.TEAM_COLORS.inverse()[color]!!)
        team.color(color)
        team.displayName(teamName)
        team.prefix(Component.text("[").color(color).append(teamName).append("]"))
        this.team = team
    }

    fun join(player: Player) {
        team.addPlayer(player)
        setPlayerDisplayName(player)
        sidebarDisplay.addPlayer(player)
    }

    fun setPlayerDisplayName(player: Player) {
        val displayName = Component.empty().append(team.prefix()).append(Component.text(player.name).color(color))
        player.displayName(displayName)
        player.playerListName(displayName)
    }

    fun leave(player: Player) {
        team.removePlayer(player)
        TeamManager.setSpectatorDisplayName(player)
        sidebarDisplay.removePlayer(player)
    }

    fun onGameStart() {
        isInUse = true
        lifeCount = DynamicSettings.lifeCount
        val unbindResult = criteria?.onUnbind(this, CriteriaChangeReason.GAME_STAGE_CHANGE)
        if (unbindResult != false) {
            criteria = CriteriaManager.getRandomCriteria(this)
            criteria!!.onBind(this, CriteriaChangeReason.GAME_STAGE_CHANGE)
        }
        sidebarDisplay.show()
        mainTimer.start()
    }

    fun broadcast(message: Component) {
        members.forEach { it.sendMessage(message) }
    }

    fun broadcastOtherTeams(message: Component) {
        for (team in TeamManager.getInUseTeams().values) {
            if (team === this) continue
            team.broadcast(message)
        }
    }

    fun broadcastTitle(title: Title) {
        members.forEach { it.showTitle(title) }
    }

    fun updateSidebar(otherTeamsData: List<TeamData>) {
        sidebarDisplay.content = otherTeamsData.map(TeamManager::formatTeamSidebarInfo)
        for (player in members) {
            sidebarDisplay.addPlayer(player)
        }
    }

    fun addLife(amount: Int = 1) {
        lifeCount += amount
        lifeCount.coerceAtMost(DynamicSettings.lifeCount)
        TeamManager.updateSidebars()
    }

    fun reduceLife(amount: Int = 1) {
        lifeCount -= amount
        if (lifeCount <= 0) {
            eliminate()
            lifeCount = 0
        }
        TeamManager.updateSidebars()
    }

    fun setLife(life: Int) {
        val prevEliminated = isEliminated
        lifeCount = life
        if (prevEliminated && !isEliminated) {
            for (player in members) {
                player.teleport(GameAreaGenerator.center ?: player.respawnLocation ?: player.world.spawnLocation)
                player.gameMode = GameMode.SURVIVAL
            }
            criteria = CriteriaManager.getRandomCriteria(this)
            criteria!!.onBind(this, CriteriaChangeReason.GAME_STAGE_CHANGE)
            mainTimer.start()
        } else if (!prevEliminated && isEliminated) {
            eliminate()
        }
        TeamManager.updateSidebars()
    }

    fun trigger(player: Player? = null) {
        val oldCriteria = criteria
        val whoTriggered: Component = player?.displayName() ?: teamName

        criteria?.onUnbind(this, CriteriaChangeReason.TRIGGERED)
        if (lifeCount - 1 <= 0) {
            criteria = null
        } else {
            criteria = CriteriaManager.getRandomCriteria(this, oldCriteria)
            criteria!!.onBind(this, CriteriaChangeReason.TRIGGERED)
            broadcastTitle(
                Title.title(
                    Configs.CRITERIA_TRIGGERED_TITLE.get().format(
                        WHO_TRIGGERED_PLACEHOLDER to whoTriggered,
                        CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria?.displayText?.color(color)
                    ),
                    Configs.CRITERIA_TRIGGERED_SUBTITLE.get().format(
                        WHO_TRIGGERED_PLACEHOLDER to whoTriggered,
                        CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria?.displayText?.color(color)
                    ),
                    0, 50, 10
                )
            )
        }
        Bukkit.broadcast(
            Configs.CRITERIA_TRIGGERED_ANNOUNCE.get().format(
                WHO_TRIGGERED_PLACEHOLDER to whoTriggered,
                CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria?.displayText?.color(color)
            )
        )
        TaskBuilder(DontDoIt.instance, TaskType.Delayed(Duration.ticks(1.0)))
            .setTask {
                for (p in members) {
                    p.world.playSound(
                        p.location,
                        "minecraft:entity.ender_eye.death",
                        SoundCategory.BLOCKS,
                        1f, 1f
                    )
                    p.world.spawnParticle(
                        Particle.ASH,
                        p.location.x, p.location.y + 1, p.location.z,
                        2000,
                        0.5, 0.5, 0.5,
                        0.0, null, true
                    )
                }
            }.runTask()

        reduceLife(1)
        if (!isEliminated) {
            mainTimer.resetAndStart()
        }
        LOGGER.info("Team $teamId triggered criteria ${oldCriteria?.displayName}, new criteria is ${criteria?.displayName}, who triggered: {}", player?.name ?: teamId)
    }

    private val allowGuess: Boolean
        get() = !guessCooldownTimer.isTicking()
    private val guessCooldownTimer: Timer = object : Timer(DontDoIt.instance, GUESS_COOLDOWN, TimerType.COUNTDOWN) {
        override fun onStart() { }
        override fun onTick() { }
        override fun onStop() {
            broadcastOtherTeams(Configs.GUESS_COOLDOWN_FINISHED_MESSAGE.get().format(
                TEAM_NAME_PLACEHOLDER to teamName
            ))
        }
    }

    /**
     * 返回值代表是否允许猜词条
     */
    fun guess(player: Player, guessed: Boolean): Int? {
        if (!allowGuess) return guessCooldownTimer.currentTime.seconds.toInt()

        val oldCriteria = criteria
        if (guessed) {
            addLife(DynamicSettings.guessSuccessAddLife)

            members.forEach { p ->
                p.playSound(
                    p.location,
                    "minecraft:block.note_block.bell",
                    SoundCategory.BLOCKS,
                    1f, 1f
                )
            }
            Bukkit.broadcast(Configs.GUESS_SUCCESS_ANNOUNCE.get().format(
                PLAYER_NAME_PLACEHOLDER to player.displayName(),
                LIFE_COUNT_PLACEHOLDER to DynamicSettings.guessSuccessAddLife,
                CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria?.displayText?.color(color)
            ))
            broadcastTitle(Title.title(
                Configs.GUESS_SUCCESS_TITLE.get().color(color),
                Configs.GUESS_SUCCESS_SUBTITLE.get().format(
                    LIFE_COUNT_PLACEHOLDER to DynamicSettings.guessSuccessAddLife,
                    CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria?.displayText?.color(color)
                ),
                5, 50, 10
            ))

            val unbindResult = criteria?.onUnbind(this, CriteriaChangeReason.GUESS_SUCCESS)
            if (unbindResult != false) {
                criteria = CriteriaManager.getRandomCriteria(this, oldCriteria)
                criteria!!.onBind(this, CriteriaChangeReason.GUESS_SUCCESS)
            }
        } else {
            reduceLife(DynamicSettings.guessFailedReduceLife)

            members.forEach { p ->
                p.playSound(
                    p.location,
                    "minecraft:block.anvil.place",
                    SoundCategory.BLOCKS,
                    0.5f, 0.8f
                )
            }
            Bukkit.broadcast(Configs.GUESS_FAILED_ANNOUNCE.get().format(
                PLAYER_NAME_PLACEHOLDER to player.displayName(),
                LIFE_COUNT_PLACEHOLDER to DynamicSettings.guessFailedReduceLife,
                CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria?.displayText?.color(color)
            ))
            broadcastTitle(Title.title(
                Configs.GUESS_FAILED_TITLE.get().color(color),
                Configs.GUESS_FAILED_SUBTITLE.get().format(
                    LIFE_COUNT_PLACEHOLDER to DynamicSettings.guessFailedReduceLife,
                    CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria?.displayText?.color(color)
                ),
                5, 50, 10
            ))

            val unbindResult = criteria?.onUnbind(this, CriteriaChangeReason.GUESS_FAILED)
            if (unbindResult != false) {
                criteria = CriteriaManager.getRandomCriteria(this, oldCriteria)
                criteria!!.onBind(this, CriteriaChangeReason.GUESS_FAILED)
            }
        }

        guessCooldownTimer.start()
        mainTimer.resetAndStart()
        LOGGER.info("Team $teamId guessed criteria ${oldCriteria?.displayName}, new criteria is ${criteria!!.displayName}, guess result: {}", if (guessed) "success" else "failed")
        return null
    }

    fun eliminate() {
        criteria?.onUnbind(this, CriteriaChangeReason.GAME_STAGE_CHANGE)
        criteria = null
        members.forEach { player ->
            player.gameMode = GameMode.SPECTATOR
        }
        mainTimer.reset()

        Bukkit.broadcast(Configs.ELIMINATED_ANNOUNCE.get().format(TEAM_NAME_PLACEHOLDER to teamName))
        broadcastTitle(Title.title(
            Configs.ELIMINATED_TITLE.get().format(TEAM_NAME_PLACEHOLDER to teamName),
            Configs.ELIMINATED_SUBTITLE.get().format(TEAM_NAME_PLACEHOLDER to teamName),
            5, 100, 10
        ))
        Bukkit.getOnlinePlayers().forEach { player ->
            player.playSound(
                player.location,
                "minecraft:entity.ender_dragon.ambient",
                SoundCategory.BLOCKS,
                1f, 1f
            )
        }
        LOGGER.info("Team $teamId is eliminated")

        TeamManager.updateSidebars()
        TeamManager.tryEndGame()
    }

    fun onGameEnd() {
        members.forEach { it.gameMode = GameMode.SPECTATOR }
        criteria?.onUnbind(this, CriteriaChangeReason.GAME_STAGE_CHANGE)
        criteria = null
        guessCooldownTimer.reset()
        mainTimer.reset()
    }

    operator fun contains(player: Player): Boolean {
        val name = player.name
        return name in team.entries
    }

    companion object {
        const val TEAM_NAME_PLACEHOLDER = "team_name"
        const val PLAYER_NAME_PLACEHOLDER = "player"
        const val WHO_TRIGGERED_PLACEHOLDER = "who_triggered"
        const val CHANGE_CRITERIA_TIME_LEFT_PLACEHOLDER = "time"
        const val LIFE_COUNT_PLACEHOLDER = "life_count"
        const val CRITERIA_DISPLAY_NAME_PLACEHOLDER = "criteria"

        val GUESS_COOLDOWN = Duration.seconds(30.0)

        const val CUSTOM_CRITERIA_ANNOUNCE_COOLDOWN_SEC = 30
        const val CUSTOM_CRITERIA_ANNOUNCE_DELAY_SEC = 10

        private val LOGGER = DontDoIt.LOGGER
    }
}
