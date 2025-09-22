package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.lib.range.IntRange

class FoodLevelCriteria : Criteria() {
    override val type: CriteriaType = CriteriaType.FOOD_LEVEL
    var amountRange: IntRange = IntRange.INFINITY
    var reversed: Boolean = false

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if ((player.foodLevel in amountRange) xor reversed) {
                trigger(player)
                break
            }
        }
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setIntRangeField(AMOUNT_RANGE_KEY, true) { amountRange = it }
        data.setBoolField(REVERSED_KEY, true) { reversed = it }
    }

    companion object {
        const val AMOUNT_RANGE_KEY = "amount"
        const val REVERSED_KEY = "reversed"
    }
}