package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.EntityInspectCandidate
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.range.DoubleRange
import org.bukkit.entity.EntityType
import org.bukkit.event.Listener

class DistanceToEntityCriteria : Criteria(), Listener, EntityInspectCandidate {
    override val type: CriteriaType = CriteriaType.DISTANCE_TO_ENTITY
    lateinit var entityTypes: Set<EntityType>
    var isWildcard: Boolean = false
    var distanceRangeSquared: DoubleRange = DoubleRange.INFINITY
    var rangeReversed: Boolean = false

    override fun getCandidateEntityTypes(): Set<EntityType> {
        return entityTypes
    }

    override fun canMatchAnyEntity(): Boolean {
        return isWildcard
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
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            val world = player.world
            val entities = world.entities.filter { it.uniqueId != player.uniqueId && (isWildcard || it.type in entityTypes) }
            if (entities.any { (player.location.distanceSquared(it.location) in distanceRangeSquared) xor rangeReversed }) {
                trigger(player)
                break
            }
        }
    }

    companion object {
        const val ENTITY_TYPES_KEY = "entity"
        const val DISTANCE_RANGE_KEY = "distance"
        const val RANGE_REVERSED_KEY = "reversed"
    }
}