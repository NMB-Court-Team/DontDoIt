package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.ImmediatelyTriggerInspector
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.range.IntRange
import org.bukkit.entity.Player

class FoodLevelCriteria : Criteria(), ImmediatelyTriggerInspector {
    override val type: CriteriaType = CriteriaType.FOOD_LEVEL
    var amountRange: IntRange = IntRange.INFINITY
    var rangeReversed: Boolean = false

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (shouldTrigger(player)) {
                trigger(player)
                break
            }
        }
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setIntRangeField(AMOUNT_RANGE_KEY, true) { amountRange = it }
        data.setBoolField(RANGE_REVERSED_KEY, true) { rangeReversed = it }
    }

    override fun shouldTrigger(player: Player): Boolean {
        return (player.foodLevel in amountRange) xor rangeReversed
    }

    companion object {
        const val AMOUNT_RANGE_KEY = "amount"
        const val RANGE_REVERSED_KEY = "reversed"
    }
}

// foodLevel in amountRange, reversed -> result
// true, false -> true
// true, true -> false
// false, false -> false
// false, true -> true
