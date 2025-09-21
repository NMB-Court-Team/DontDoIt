package net.astrorbits.dontdoit.team

import com.google.common.collect.BiMap
import net.astrorbits.dontdoit.Configs
import net.astrorbits.lib.collection.CollectionHelper.toBiMap
import net.astrorbits.lib.text.TextHelper.append
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard

object TeamManager {
    val TEAM_COLORS: BiMap<String, NamedTextColor> = NamedTextColor.NAMES.keyToValue().filterValues { it != NamedTextColor.GRAY }.toBiMap()

    lateinit var scoreboard: Scoreboard
    private val _teams: MutableList<TeamData> = mutableListOf()

    val teams: List<TeamData>
        get() = _teams

    fun init(server: Server) {
        scoreboard = server.scoreboardManager.newScoreboard
        for ((name, color) in TEAM_COLORS) {
            val teamName = Configs.getTeamName(color) ?: continue
            val team = scoreboard.registerNewTeam(name)
            team.color(color)
            team.displayName(teamName)
            team.prefix(Component.text("[").color(color).append(teamName).append("]"))
            val teamData = TeamData(color, team)
            _teams.add(teamData)
        }
    }

    /** 更新计分板显示 */
    fun updateSidebars() {
        val teams = this.teams
        for (teamData in teams) {
            val otherTeams = teams.filter { it != teamData }
            teamData.updateSidebar(otherTeams)
        }
    }

    fun getTeamOf(player: Player): TeamData? {
        return _teams.firstOrNull { player in it }
    }
}
