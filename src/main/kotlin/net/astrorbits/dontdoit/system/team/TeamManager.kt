package net.astrorbits.dontdoit.system.team

import com.google.common.collect.BiMap
import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.DynamicSettings
import net.astrorbits.dontdoit.system.DiamondBehavior
import net.astrorbits.dontdoit.system.GameStateManager
import net.astrorbits.dontdoit.system.team.TeamData.Companion.CRITERIA_DISPLAY_NAME_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamData.Companion.LIFE_COUNT_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamData.Companion.PLAYER_NAME_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamData.Companion.TEAM_NAME_PLACEHOLDER
import net.astrorbits.lib.collection.CollectionHelper.toBiMap
import net.astrorbits.lib.scoreboard.SidebarDisplay
import net.astrorbits.lib.task.Timer
import net.astrorbits.lib.text.TextHelper.append
import net.astrorbits.lib.text.TextHelper.format
import net.astrorbits.lib.text.TextHelper.gray
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scoreboard.Scoreboard
import java.util.UUID

object TeamManager : Listener {
    val TEAM_COLORS: BiMap<String, NamedTextColor> = setOf(
        NamedTextColor.RED,
        NamedTextColor.GOLD,
        NamedTextColor.YELLOW,
        NamedTextColor.GREEN,
        NamedTextColor.AQUA,
        NamedTextColor.DARK_AQUA,
        NamedTextColor.LIGHT_PURPLE,
        NamedTextColor.DARK_PURPLE
    ).associateBy { NamedTextColor.NAMES.valueToKey()[it]!! }.toBiMap()

    lateinit var scoreboard: Scoreboard
    private val _teams: MutableList<TeamData> = mutableListOf()
    val spectatorSidebarDisplay: SidebarDisplay = SidebarDisplay()

    val teams: List<TeamData>
        get() = _teams

    fun init(server: Server) {
        server.pluginManager.registerEvents(this, DontDoIt.instance)

        scoreboard = server.scoreboardManager.newScoreboard
        for ((name, color) in TEAM_COLORS) {
            val teamName = Configs.getTeamName(color)
            val team = scoreboard.registerNewTeam(name)
            team.color(color)
            team.displayName(teamName)
            team.prefix(Component.text("[").color(color).append(teamName).append("]"))
            val teamData = TeamData(color, team)
            _teams.add(teamData)
        }
        spectatorSidebarDisplay.hide()
        spectatorSidebarDisplay.title = Configs.SIDEBAR_TITLE.get()
    }

    fun getInUseTeams(): Map<String, TeamData> {
        return if (!GameStateManager.isWaiting()) {
            teams.filter { it.isInUse }.associateBy { it.teamId }
        } else {
            teams.associateBy { it.teamId }
        }
    }

    fun joinTeam(player: Player, color: NamedTextColor) {
        val team = getTeam(color).team
        team.addPlayer(player)
        player.displayName(Component.empty().append(team.prefix()).append(player.name))
    }

    fun leaveTeam(player: Player) {
        getTeam(player)?.team?.removePlayer(player)
        player.displayName(Component.text(player.name).gray())
    }

    /** 更新计分板显示 */
    fun updateSidebars() {
        val teams = getInUseTeams().values
        for (teamData in teams) {
            val otherTeams = teams.filter { it !== teamData }
            teamData.updateSidebar(otherTeams)
        }
        spectatorSidebarDisplay.content = teams.filter { it.criteria != null }
            .map { teamData ->
                val criteria = teamData.criteria!!
                val nameFormatConfig = if (teamData.isEliminated) Configs.SIDEBAR_ENTRY_DEAD_NAME else Configs.SIDEBAR_ENTRY_NAME
                val name = nameFormatConfig.get().format(mapOf(
                    TEAM_NAME_PLACEHOLDER to teamData.teamName,
                    LIFE_COUNT_PLACEHOLDER to teamData.lifeCount,
                    CRITERIA_DISPLAY_NAME_PLACEHOLDER to criteria.displayName
                ))
                val numberFormatConfig = if (teamData.isEliminated) Configs.SIDEBAR_ENTRY_DEAD_NUMBER else Configs.SIDEBAR_ENTRY_NUMBER
                val number = numberFormatConfig.get().format(mapOf(
                    TEAM_NAME_PLACEHOLDER to teamData.teamName,
                    LIFE_COUNT_PLACEHOLDER to teamData.lifeCount,
                    CRITERIA_DISPLAY_NAME_PLACEHOLDER to criteria.displayName
                ))
                return@map SidebarDisplay.ScoreEntry(name, number)
            }
    }

    fun getTeam(player: Player): TeamData? {
        return _teams.firstOrNull { player in it }
    }

    fun getTeam(color: NamedTextColor): TeamData {
        return _teams.first { it.color == color }
    }

    fun getWinner(): TeamData? {
        return getInUseTeams().values.firstOrNull { !it.isEliminated }
    }

    @EventHandler
    fun onJoinServer(event: PlayerJoinEvent) {
        val player = event.player
        if (getTeam(player) == null) {
            spectatorSidebarDisplay.addPlayer(player)
            player.gameMode = GameMode.SPECTATOR
        }
    }

    private val announceGuessTimer: Timer = object : Timer(DontDoIt.instance) {
        override fun onStart() { }
        override fun onTick() {
            if ((currentTime.seconds.toInt() - GUESS_ANNOUNCE_DELAY_SEC) % GUESS_ANNOUNCE_COOLDOWN_SEC == 0) {
                getInUseTeams().values.forEach{ it.broadcast(Configs.GUESS_HINT_MESSAGE.get()) }
            }
        }
        override fun onStop() { }
    }

    const val GUESS_ANNOUNCE_COOLDOWN_SEC = 60
    const val GUESS_ANNOUNCE_DELAY_SEC = 25

    fun onGameStart() {
        for (teamData in teams) {
            if (teamData.hasMember) {
                teamData.onGameStart()
            }
        }
        for (player in Bukkit.getOnlinePlayers()) {
            if (getTeam(player) == null) {
                player.gameMode = GameMode.SPECTATOR
                spectatorSidebarDisplay.addPlayer(player)
            } else {
                player.gameMode = GameMode.SURVIVAL
                spectatorSidebarDisplay.removePlayer(player)
            }
        }
        if (DynamicSettings.allowGuessCriteria) {
            announceGuessTimer.start()
        }
    }

    fun tryEndGame() {
        if (getInUseTeams().values.count { !it.isEliminated } <= 1) {
            GameStateManager.endGame()
        }
    }

    fun onGameEnd() {
        announceGuessTimer.reset()
        teams.forEach { team -> team.onGameEnd() }
    }

    fun onEnterPreparation() {
        for (teamData in teams) {
            teamData.criteria = null
            teamData.isInUse = false
            teamData.sidebarDisplay.hide()
        }
        spectatorSidebarDisplay.hide()
    }

    /**
     * 返回值代表是否允许猜词条
     */
    fun guess(player: Player, teamData: TeamData, guessed: Boolean): Int? {
        return teamData.guess(player, guessed)
    }

    @EventHandler
    fun onPickUpItem(event: EntityPickupItemEvent) {
        if (!GameStateManager.isRunning() || !DynamicSettings.diamondBehaviorEnabled) return
        val player = event.entity as? Player ?: return
        val item = event.item.itemStack
        if (item.type != Material.DIAMOND) return

        val team = getTeam(player) ?: return
        if (!team.isInUse || team.isEliminated) return
        when (DynamicSettings.diamondBehavior) {
            DiamondBehavior.REDUCE_OTHERS_LIFE -> {
                val otherTeams = getInUseTeams().values.filter { it !== team }
                if (otherTeams.any { it.lifeCount <= DynamicSettings.diamondBehaviorDisabledThreshold }) {
                    return
                }
                otherTeams.forEach { it.reduceLife(1) }
            }
            DiamondBehavior.ADD_SELF_LIFE -> {
                team.addLife(1)
            }
        }
        Bukkit.broadcast(Configs.GET_DIAMOND_MESSAGE.get().format(PLAYER_NAME_PLACEHOLDER to player.displayName()))
        player.world.playSound(
            player.location,
            "minecraft:block.note_block.bell",
            SoundCategory.BLOCKS,
            1f, 1f
        )
        player.world.spawnParticle(
            Particle.WAX_OFF,
            player.location.x, player.location.y + 1, player.location.z,
            30,
            0.5, 0.5, 0.5,
            1.0, null, true
        )
    }
}
