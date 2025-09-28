package net.astrorbits.dontdoit.criteria.builtin

import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.criteria.Criteria
import net.astrorbits.dontdoit.criteria.helper.BuiltinCriteria
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.helper.TriggerDifficulty
import net.astrorbits.dontdoit.criteria.helper.YLevelType
import net.astrorbits.dontdoit.system.team.TeamData

class YLevelCriteria : Criteria(), BuiltinCriteria {
    override val type: CriteriaType = CriteriaType.Y_LEVEL
    var border: Int = Int.MIN_VALUE
        private set
    var belowBorder: Boolean = true
        private set

    init {
        triggerDifficulty = TriggerDifficulty.VERY_EASY
        displayName = ""
    }

    fun setBorder(groundYLevel: Int, type: YLevelType) {
        this.border = groundYLevel + type.groundOffsetGetter()
        this.belowBorder = type.belowBorder
        displayName = if (belowBorder) {
            Configs.Y_LEVEL_CRITERIA_BELOW
        } else {
            Configs.Y_LEVEL_CRITERIA_ABOVE
        }.get().format(type.typeNameGetter(), border)
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            val y = player.location.y
            if (belowBorder) {
                if (y < border) {
                    trigger(player)
                }
            } else {
                if (y > border) {
                    trigger(player)
                }
            }
        }
    }

    override fun readData(data: Map<String, String>) { }

    override fun shouldUse(): Boolean {
        return super.shouldUse() && Configs.Y_LEVEL_CRITERIA_ENABLED.get() && !(border == Int.MIN_VALUE && belowBorder)
    }
}