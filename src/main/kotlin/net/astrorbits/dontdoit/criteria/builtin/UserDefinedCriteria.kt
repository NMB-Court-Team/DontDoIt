package net.astrorbits.dontdoit.criteria.builtin

import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.criteria.Criteria
import net.astrorbits.dontdoit.criteria.helper.BuiltinCriteria
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.helper.TriggerDifficulty
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.dontdoit.system.team.TeamManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class UserDefinedCriteria(val teamColor: NamedTextColor) : Criteria(), BuiltinCriteria {
    override val type: CriteriaType = CriteriaType.USER_DEFINED
    val teamData: TeamData
        get() = TeamManager.getTeam(teamColor)

    init {
        triggerDifficulty = TriggerDifficulty.HARD
        displayName = Component.empty()
    }

    fun setName(name: String) {
        displayName = Component.text("${Configs.CUSTOM_CRITERIA_NAME_PREFIX.get()}$name")
    }

    override fun readData(data: Map<String, String>) { }

    override fun shouldUse(): Boolean {
        return super.shouldUse() && teamData.isInUse
    }
}