package net.astrorbits.dontdoit.criteria.inspect

import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.entity.Player

interface ImmediatelyTriggerInspector : InventoryInspectable {
    fun shouldTrigger(player: Player): Boolean

    fun anyTriggers(bindTarget: TeamData): Boolean {
        var anyTriggers = false
        for (player in bindTarget.members) {
            if (shouldTrigger(player)) {
                anyTriggers = true
                break
            }
        }
        return anyTriggers
    }

    fun getAnyTriggersMultiplier(bindTarget: TeamData, fallback: () -> Double): Double {
        return if (anyTriggers(bindTarget)) DIRECTLY_TRIGGER_MULTIPLIER else fallback()
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getAnyTriggersMultiplier(bindTarget) { 1.0 }
    }

    companion object {
        const val DIRECTLY_TRIGGER_MULTIPLIER = 0.9
    }
}