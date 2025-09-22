package net.astrorbits.dontdoit.criteria.builtin

import net.astrorbits.dontdoit.criteria.Criteria
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.helper.TriggerDifficulty
import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.dontdoit.team.TeamManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class UserDefinedCriteria(val teamColor: NamedTextColor) : Criteria() {
    override val type: CriteriaType = CriteriaType.USER_DEFINED
    val teamData: TeamData
        get() = TeamManager.getTeam(teamColor)

    init {
        triggerDifficulty = TriggerDifficulty.HARD
        displayName = Component.empty()
    }

    fun setName(name: String) {
        displayName = Component.text("[自定义]说脏话")
    }

    override fun readData(data: Map<String, String>) { }
}