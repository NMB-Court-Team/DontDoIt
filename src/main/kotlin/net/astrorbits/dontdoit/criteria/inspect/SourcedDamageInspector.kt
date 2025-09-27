package net.astrorbits.dontdoit.criteria.inspect

interface SourcedDamageInspector : EntityInspectCandidate, DamageTypeInspectCandidate {
    override fun modifyWeight(weight: Double, context: InventoryInspectContext): Double {
        return weight * getEntityMultiplier(context) * getDamageTypeMultiplier(context)
    }
}