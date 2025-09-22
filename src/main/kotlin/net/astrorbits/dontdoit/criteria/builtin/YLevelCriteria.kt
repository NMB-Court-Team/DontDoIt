package net.astrorbits.dontdoit.criteria.builtin

import net.astrorbits.dontdoit.criteria.Criteria
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.helper.TriggerDifficulty
import net.astrorbits.dontdoit.team.TeamData
import net.kyori.adventure.text.Component

class YLevelCriteria : Criteria() {
    override val type: CriteriaType = CriteriaType.Y_LEVEL
    var border: Int = Int.MIN_VALUE
        private set
    var smallerThanBorder: Boolean = true
        private set

    init {
        triggerDifficulty = TriggerDifficulty.VERY_EASY
        displayName = Component.empty()
    }

    fun setBorder(border: Int, smallerThanBorder: Boolean) {
        this.border = border
        this.smallerThanBorder = smallerThanBorder
        displayName = if (smallerThanBorder) {
            Component.text("Y坐标小于$border")
        } else {
            Component.text("Y坐标大于$border")
        }
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            val y = player.location.y
            if (smallerThanBorder) {
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
}