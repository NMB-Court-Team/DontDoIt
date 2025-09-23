package net.astrorbits.dontdoit.system.team

import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.GlobalSettings
import net.astrorbits.dontdoit.criteria.Criteria
import net.astrorbits.lib.StringHelper.isUuid
import net.astrorbits.lib.scoreboard.SidebarDisplay
import net.astrorbits.lib.text.LegacyText
import net.astrorbits.lib.text.TextHelper.format
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team

class TeamData(val color: NamedTextColor, val team: Team, val teamItem: Material) {
    val teamId: String
        get() = team.name
    val teamName: Component
        get() = team.displayName()
    val members: List<Player>  //TODO 有待优化，最好能优化成缓存Player对象的形式
        get() = team.entries.mapNotNull { name -> if (name.isUuid()) null else Bukkit.getPlayer(name) }
    val hasMember: Boolean
        get() = members.isNotEmpty()

    var isInUse: Boolean = false
    var lifeCount: Int = GlobalSettings.lifeCount
        private set
    var isDead: Boolean = false
        private set
    var criteria: Criteria? = null
        set(value) {
            TeamManager.updateSidebars()
            field = value
        }

    val sidebarDisplay: SidebarDisplay = SidebarDisplay()

    init {
        sidebarDisplay.title = Configs.SIDEBAR_TITLE.get()
    }

    fun updateSidebar(otherTeamsData: List<TeamData>) {
        sidebarDisplay.content = otherTeamsData
            .filter { it.criteria != null }
            .map { teamData ->
                val criteria = teamData.criteria!!
                val nameFormatConfig = if (teamData.isDead) Configs.SIDEBAR_ENTRY_DEAD_NAME else Configs.SIDEBAR_ENTRY_NAME
                val name = nameFormatConfig.get().format(mapOf(
                    TEAM_NAME_PLACEHOLDER to teamData.teamName,
                    LIFE_COUNT_PLACEHOLDER to teamData.lifeCount,
                    CRITERIA_DISPLAY_NAME_PLACEHOLDER to criteria.displayName
                ))
                val numberFormatConfig = if (teamData.isDead) Configs.SIDEBAR_ENTRY_DEAD_NUMBER else Configs.SIDEBAR_ENTRY_NUMBER
                val number = numberFormatConfig.get().format(mapOf(
                    TEAM_NAME_PLACEHOLDER to teamData.teamName,
                    LIFE_COUNT_PLACEHOLDER to teamData.lifeCount,
                    CRITERIA_DISPLAY_NAME_PLACEHOLDER to criteria.displayName
                ))
                return@map SidebarDisplay.ScoreEntry(name, number)
            }
    }

    fun loseLife(amount: Int = 1) {
        lifeCount -= amount
        if (lifeCount <= 0) {
            death()
            lifeCount = 0
        }
        TeamManager.updateSidebars()
    }

    fun death() {
        isDead = true
        members.forEach { player ->
            player.isInvulnerable = true
            player.sendActionBar(LegacyText.toComponent("§c你的队伍已失败，进入旁观模式"))
            player.gameMode = GameMode.SPECTATOR
        }
    }

    operator fun contains(player: Player): Boolean {
        val name = player.name
        return name in team.entries
    }

    companion object {
        const val TEAM_NAME_PLACEHOLDER = "team_name"
        const val LIFE_COUNT_PLACEHOLDER = "life"
        const val CRITERIA_DISPLAY_NAME_PLACEHOLDER = "criteria"
    }
}
