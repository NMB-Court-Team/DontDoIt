package net.astrorbits.dontdoit.system.team

import com.google.common.collect.BiMap
import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.DynamicSettings
import net.astrorbits.dontdoit.system.DiamondBehavior
import net.astrorbits.dontdoit.system.GameState
import net.astrorbits.dontdoit.system.GameStateManager
import net.astrorbits.dontdoit.system.team.TeamData.Companion.CRITERIA_DISPLAY_NAME_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamData.Companion.LIFE_COUNT_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamData.Companion.PLAYER_NAME_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamData.Companion.TEAM_NAME_PLACEHOLDER
import net.astrorbits.lib.collection.CollectionHelper.toBiMap
import net.astrorbits.lib.item.ItemHelper.getBoolPdc
import net.astrorbits.lib.math.Duration
import net.astrorbits.lib.scoreboard.SidebarDisplay
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType
import net.astrorbits.lib.task.Timer
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
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scoreboard.Team

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

    private val _teams: MutableList<TeamData> = mutableListOf()
    val spectatorSidebarDisplay: SidebarDisplay = SidebarDisplay()
    val spectatorTeam: Team = spectatorSidebarDisplay.scoreboard.registerNewTeam("spectator")

    val teams: List<TeamData>
        get() = _teams

    fun init(server: Server) {
        server.pluginManager.registerEvents(this, DontDoIt.instance)

        for ((_, color) in TEAM_COLORS) {
            _teams.add(TeamData(color))
        }
        spectatorSidebarDisplay.title = Configs.SIDEBAR_TITLE.get()
        spectatorTeam.color(NamedTextColor.GRAY)
    }

    fun getInUseTeams(): Map<String, TeamData> {
        return if (!GameStateManager.isWaiting()) {
            teams.filter { it.isInUse }.associateBy { it.teamId }
        } else {
            teams.associateBy { it.teamId }
        }
    }

    fun joinTeam(player: Player, color: NamedTextColor) {
        leaveTeam(player, false)
        getTeam(color).join(player)
        TeamInfoSynchronizer.syncTeamInfos(teams)
    }

    fun leaveTeam(player: Player, joinSpectator: Boolean = true) {
        getTeam(player)?.leave(player)
        spectatorTeam.removePlayer(player)
        if (joinSpectator) {
            spectatorTeam.addPlayer(player)
        }
        TeamInfoSynchronizer.syncTeamInfos(teams)
    }

    fun setSpectatorDisplayName(player: Player) {
        val displayName = Component.text(player.name).gray()
        player.displayName(displayName)
        player.playerListName(displayName)
    }

    /** 更新计分板显示 */
    fun updateSidebars() {
        val teams = getInUseTeams().values
        for (teamData in teams) {
            val otherTeams = teams.filter { it !== teamData }
            teamData.updateSidebar(otherTeams)
        }
        spectatorSidebarDisplay.content = getInUseTeams().values.map(::formatTeamSidebarInfo)
        for (player in Bukkit.getOnlinePlayers()) {
            if (getTeam(player) == null) {
                spectatorSidebarDisplay.addPlayer(player)
            }
        }
    }

    fun formatTeamSidebarInfo(teamData: TeamData): SidebarDisplay.ScoreEntry {
        val isWinner = GameStateManager.state == GameState.FINISHED && getWinner() === teamData

        val criteria = teamData.criteria
        val nameFormatConfig = if (teamData.isEliminated) Configs.SIDEBAR_ENTRY_DEAD_NAME else Configs.SIDEBAR_ENTRY_NAME
        val name = nameFormatConfig.get().format(mapOf(
            TEAM_NAME_PLACEHOLDER to teamData.teamName,
            LIFE_COUNT_PLACEHOLDER to teamData.lifeCount,
            CRITERIA_DISPLAY_NAME_PLACEHOLDER to criteria?.displayName
        ))
        val numberFormatConfig = if (teamData.isEliminated) {
            Configs.SIDEBAR_ENTRY_DEAD_NUMBER
        } else if (isWinner) {
            Configs.SIDEBAR_ENTRY_NUMBER_WINNER
        } else {
            Configs.SIDEBAR_ENTRY_NUMBER
        }
        val number = numberFormatConfig.get().format(mapOf(
            TEAM_NAME_PLACEHOLDER to teamData.teamName,
            LIFE_COUNT_PLACEHOLDER to teamData.lifeCount,
            CRITERIA_DISPLAY_NAME_PLACEHOLDER to criteria?.displayName
        ))
        return SidebarDisplay.ScoreEntry(name, number)
    }

    fun getTeam(player: Player): TeamData? {
        return _teams.firstOrNull { player in it }
    }

    fun getTeam(color: NamedTextColor): TeamData {
        return _teams.first { it.color == color }
    }

    fun getWinner(): TeamData? {
        val aliveTeams = getInUseTeams().values.filter { !it.isEliminated }
        return if (aliveTeams.size == 1) {
            aliveTeams[0]
        } else {
            null
        }
    }

    @EventHandler
    fun onJoinServer(event: PlayerJoinEvent) {
        if (GameStateManager.isWaiting()) return
        val player = event.player
        val team = getTeam(player)
        if (team == null) {
            player.gameMode = GameMode.SPECTATOR
            setSpectatorDisplayName(player)
        } else {
            team.setPlayerDisplayName(player)
        }
        TaskBuilder(DontDoIt.instance, TaskType.Delayed(Duration.ticks(2.0)))
            .setTask { TeamInfoSynchronizer.syncTeamInfos(teams) }
            .runTask()
        updateSidebars()
    }

    private val guessHintAnnounceTimer: Timer = object : Timer(DontDoIt.instance) {
        override fun onStart() { }
        override fun onTick() {
            if ((currentTimeTicks - GUESS_ANNOUNCE_DELAY_SEC * 20) % (GUESS_ANNOUNCE_COOLDOWN_SEC * 20) == 0) {
                getInUseTeams().values.forEach { it.broadcast(Configs.GUESS_HINT_MESSAGE.get()) }
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
            val team = getTeam(player)
            if (team == null) {
                player.gameMode = GameMode.SPECTATOR
                spectatorSidebarDisplay.addPlayer(player)
                spectatorTeam.addPlayer(player)
            } else {
                player.gameMode = GameMode.SURVIVAL
                spectatorSidebarDisplay.removePlayer(player)
                team.sidebarDisplay.addPlayer(player)
            }
        }
        if (DynamicSettings.allowGuessCriteria) {
            guessHintAnnounceTimer.start()
        }
        spectatorSidebarDisplay.show()
        updateSidebars()
    }

    fun tryEndGame() {
        if (getInUseTeams().values.count { !it.isEliminated } <= 1) {
            GameStateManager.endGame()
        }
    }

    fun onGameEnd() {
        guessHintAnnounceTimer.reset()
        teams.forEach { team -> team.onGameEnd() }
        updateSidebars()
    }

    fun onEnterPreparation() {
        for (teamData in teams) {
            teamData.onEnterPreparation()
        }
        spectatorSidebarDisplay.hide()
        spectatorSidebarDisplay.content = emptyList()
        guessHintAnnounceTimer.reset()
    }

    /**
     * 返回值代表是否允许猜词条
     */
    fun guess(player: Player, teamData: TeamData, guessed: Boolean): Int? {
        return teamData.guess(player, guessed)
    }

    val TRIGGERED_DIAMOND_PDC_KEY = DontDoIt.id("triggered_diamond")

    @EventHandler
    fun onPickUpItem(event: EntityPickupItemEvent) {
        if (!GameStateManager.isRunning() || !DynamicSettings.diamondBehaviorEnabled) return
        val player = event.entity as? Player ?: return
        val item = event.item.itemStack
        if (item.type != Material.DIAMOND || item.isTriggeredDiamond()) return
        item.editMeta { it.persistentDataContainer.set(TRIGGERED_DIAMOND_PDC_KEY, PersistentDataType.BOOLEAN, true) }

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
        Bukkit.getOnlinePlayers().forEach { p ->
            if (p.uniqueId != player.uniqueId && getTeam(p) != null) {
                p.playSound(
                    p.location,
                    "minecraft:block.note_block.bass",
                    1f, 0.5f
                )
            }
        }
        player.world.spawnParticle(
            Particle.WAX_OFF,
            player.location.x, player.location.y + 1, player.location.z,
            30,
            0.5, 0.5, 0.5,
            1.0, null, true
        )
    }

    private fun ItemStack.isTriggeredDiamond(): Boolean {
        return this.getBoolPdc(TRIGGERED_DIAMOND_PDC_KEY) ?: false
    }
}
