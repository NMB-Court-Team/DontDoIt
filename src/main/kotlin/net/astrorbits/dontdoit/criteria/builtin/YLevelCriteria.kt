package net.astrorbits.dontdoit.criteria.builtin

import net.astrorbits.dontdoit.criteria.Criteria
import net.astrorbits.dontdoit.criteria.helper.BuiltinCriteria
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.helper.TriggerDifficulty
import net.astrorbits.dontdoit.system.team.TeamData
import net.kyori.adventure.text.Component

class YLevelCriteria : Criteria(), BuiltinCriteria {
    override val type: CriteriaType = CriteriaType.Y_LEVEL
    var border: Int = Int.MIN_VALUE
        private set
    var belowBorder: Boolean = true
        private set

    init {
        triggerDifficulty = TriggerDifficulty.VERY_EASY
        displayName = Component.empty()
    }

    fun setBorder(border: Int, belowBorder: Boolean) {
        this.border = border
        this.belowBorder = belowBorder
        displayName = if (belowBorder) {
            Component.text("Y坐标小于$border")
        } else {
            Component.text("Y坐标大于$border")
        }
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
        return super.shouldUse() && !(border == Int.MIN_VALUE && belowBorder)
    }
}