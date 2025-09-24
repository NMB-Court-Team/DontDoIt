package net.astrorbits.dontdoit.system.team

import com.google.common.collect.BiMap
import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.criteria.system.CriteriaManager
import net.astrorbits.dontdoit.system.GameState
import net.astrorbits.dontdoit.system.GameStateManager
import net.astrorbits.lib.collection.CollectionHelper.toBiMap
import net.astrorbits.lib.math.Duration
import net.astrorbits.lib.task.Timer
import net.astrorbits.lib.task.TimerType
import net.astrorbits.lib.text.TextHelper.append
import net.astrorbits.lib.text.TextHelper.gray
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard

object TeamManager {
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
    private val _teamTimers: MutableMap<NamedTextColor, TeamTimer> = mutableMapOf()

    val teams: List<TeamData>
        get() = _teams

    fun init(server: Server) {
        scoreboard = server.scoreboardManager.newScoreboard
        for ((name, color) in TEAM_COLORS) {
            val teamName = Configs.getTeamName(color)
            val teamItem = Configs.getJoinTeamItemMaterial(color)
            val team = scoreboard.registerNewTeam(name)
            team.color(color)
            team.displayName(teamName)
            team.prefix(Component.text("[").color(color).append(teamName).append("]"))
            val teamData = TeamData(color, team, teamItem)
            _teams.add(teamData)
            _teamTimers[color] = TeamTimer(teamData)
        }
    }

    fun getInUseTeams(): Map<String, TeamData> {
        return  if (GameStateManager.isRunning()) {
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
        val teams = teams
        for (teamData in teams) {
            val otherTeams = teams.filter { it !== teamData }
            teamData.updateSidebar(otherTeams)
        }
    }

    fun getTeam(player: Player): TeamData? {
        return _teams.firstOrNull { player in it }
    }

    fun getTeam(color: NamedTextColor): TeamData {
        return _teams.first { it.color == color }
    }

    fun getWinner(): TeamData? {
        return _teams.firstOrNull { !it.isDead }
    }

    fun onStartGame() {
        for (teamData in teams) {
            if (teamData.hasMember) {
                teamData.isInUse = true
                teamData.criteria = CriteriaManager.getRandomCriteria()
            }
        }
    }

    fun onEnterPreparation() {
        for (teamData in teams) {
            teamData.criteria = null
            teamData.isInUse = false
        }
    }

    fun tick(currentState: GameState) {
        if (currentState == GameState.RUNNING) {
            for (team in teams) {
                team.criteria?.tick(team)
            }
        }
    }

    fun guess(teamData: TeamData, guessed: Boolean) {

    }

    class TeamTimer(val teamData: TeamData) : Timer(DontDoIt.instance, Duration.seconds(Configs.AUTO_CHANGE_CRITERIA_TIME.get().toDouble()), TimerType.COUNTDOWN) {
        override fun onStart() {

        }

        override fun onTick() {
            TODO("Not yet implemented")
        }

        override fun onStop() {
            teamData.criteria = CriteriaManager.getRandomCriteria()
            //TODO 切换词条的显示
        }
    }
}
