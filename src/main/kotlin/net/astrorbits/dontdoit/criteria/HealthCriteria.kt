package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.ImmediatelyTriggerInspector
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.range.DoubleRange
import org.bukkit.entity.Player

class HealthCriteria : Criteria(), ImmediatelyTriggerInspector {
    override val type: CriteriaType = CriteriaType.HEALTH
    var amountRange: DoubleRange = DoubleRange.INFINITY
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
        data.setDoubleRangeField(AMOUNT_RANGE_KEY, true) { amountRange = it }
        data.setBoolField(RANGE_REVERSED_KEY, true) { rangeReversed = it }
    }

    override fun shouldTrigger(player: Player): Boolean {
        return (player.health in amountRange) xor rangeReversed
    }

    companion object {
        const val AMOUNT_RANGE_KEY = "amount"
        const val RANGE_REVERSED_KEY = "reversed"
    }
}