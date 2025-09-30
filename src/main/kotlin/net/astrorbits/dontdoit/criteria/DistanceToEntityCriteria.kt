package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.helper.DistanceToEntityMode
import net.astrorbits.dontdoit.criteria.inspect.ImmediatelyTriggerInspector
import net.astrorbits.dontdoit.criteria.inspect.EntityInspectCandidate
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.dontdoit.system.team.TeamManager
import net.astrorbits.lib.range.DoubleRange
import org.bukkit.GameMode
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class DistanceToEntityCriteria : Criteria(), Listener, EntityInspectCandidate, ImmediatelyTriggerInspector {
    override val type: CriteriaType = CriteriaType.DISTANCE_TO_ENTITY
    lateinit var entityTypes: Set<EntityType>
    var isWildcard: Boolean = false
    var distanceRangeSquared: DoubleRange = DoubleRange.INFINITY
    var rangeReversed: Boolean = false
    lateinit var mode: DistanceToEntityMode

    override fun getCandidateEntityTypes(): Set<EntityType> {
        return entityTypes
    }

    override fun canMatchAnyEntity(): Boolean {
        return isWildcard
    }

    override fun getSurroundingEntityMatchingWeightMultiplier(context: InventoryInspectContext): Double {
        return getEntityMatchingWeightMultiplier(context)
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setEntityTypes(ENTITY_TYPES_KEY) { entityTypes, isWildcard ->
            this.entityTypes = entityTypes
            this.isWildcard = isWildcard
        }
        data.setDoubleRangeField(DISTANCE_RANGE_KEY, true) {
            distanceRangeSquared = DoubleRange(
                if (it.min == Double.NEGATIVE_INFINITY) Double.NEGATIVE_INFINITY else it.min * it.min,
                it.max * it.max
            )
        }
        data.setBoolField(RANGE_REVERSED_KEY, true) { rangeReversed = it }
        data.setField(MODE_KEY) { mode = DistanceToEntityMode.valueOf(it.uppercase()) }
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (shouldTrigger(player)) {
                trigger(player)
                break
            }
        }
    }

    override fun shouldTrigger(player: Player): Boolean {
        val world = player.world
        val entities = world.entities.filter { it.uniqueId != player.uniqueId && (it !is Player || TeamManager.getTeam(it) != null) && (isWildcard || it.type in entityTypes) }
        return mode.check(entities) { (player.location.distanceSquared(it.location) in distanceRangeSquared) xor rangeReversed }
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getAnyTriggersMultiplier(bindTarget) { getEntityMultiplier(context) }
    }

    companion object {
        const val ENTITY_TYPES_KEY = "entity"
        const val DISTANCE_RANGE_KEY = "distance"
        const val RANGE_REVERSED_KEY = "reversed"
        const val MODE_KEY = "mode"
    }
}