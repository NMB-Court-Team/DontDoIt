package net.astrorbits.dontdoit.system.team

import net.astrorbits.lib.text.LegacyTextColor.getClosestNamedColor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team

object TeamInfoSynchronizer {
    fun syncTeamInfos(teams: List<TeamData>) {
        val snapshots = (teams.map(TeamData::team) + TeamManager.spectatorTeam).map(TeamSnapshot::from)
        val scoreboards = (teams.map { it.sidebarDisplay.scoreboard } + TeamManager.spectatorSidebarDisplay.scoreboard).distinct()

        for (scoreboard in scoreboards) {
            snapshots.forEach { it.applyTo(scoreboard) }
        }
    }

    private data class TeamSnapshot(
        val name: String,
        val displayName: Component,
        val prefix: Component,
        val suffix: Component,
        val color: NamedTextColor,
        val allowFriendlyFire: Boolean,
        val canSeeFriendlyInvisibles: Boolean,
        val options: Map<Team.Option, Team.OptionStatus>,
        val entries: Set<String>
    ) {
        fun applyTo(scoreboard: Scoreboard) {
            val target = scoreboard.getTeam(name) ?: scoreboard.registerNewTeam(name)
            target.displayName(displayName)
            target.prefix(prefix)
            target.suffix(suffix)
            target.color(color)
            target.setAllowFriendlyFire(allowFriendlyFire)
            target.setCanSeeFriendlyInvisibles(canSeeFriendlyInvisibles)
            options.forEach(target::setOption)

            target.entries.toList().forEach(target::removeEntry)
            entries.forEach(target::addEntry)
        }

        companion object {
            fun from(team: Team): TeamSnapshot = TeamSnapshot(
                name = team.name,
                displayName = team.displayName(),
                prefix = team.prefix(),
                suffix = team.suffix(),
                color = getClosestNamedColor(team.color()),
                allowFriendlyFire = team.allowFriendlyFire(),
                canSeeFriendlyInvisibles = team.canSeeFriendlyInvisibles(),
                options = Team.Option.entries.associateWith(team::getOption),
                entries = team.entries.toSet()
            )
        }
    }
}
