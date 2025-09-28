package net.astrorbits.dontdoit.criteria.inspect

import net.astrorbits.dontdoit.system.team.TeamData
import kotlin.math.max

interface SourcedDamageInspector : EntityInspectCandidate, DamageTypeInspectCandidate {
    fun getSourcedDamageMultiplier(context: InventoryInspectContext): Double {
        return max(getEntityMultiplier(context), getDamageTypeMultiplier(context))
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getSourcedDamageMultiplier(context)
    }
}