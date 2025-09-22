package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.type.EntityCriteria
import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.lib.range.DoubleRange
import org.bukkit.entity.EntityType
import org.bukkit.event.Listener

class DistanceToEntityCriteria : Criteria(), Listener, EntityCriteria {
    override val type: CriteriaType = CriteriaType.DISTANCE_TO_ENTITY
    lateinit var entityTypes: Set<EntityType>
    var isEntityTypeWildcard: Boolean = false
    var distanceRange: DoubleRange = DoubleRange.INFINITY
    lateinit var squaredRange: DoubleRange


    override fun getCandidateEntityTypes(): Set<EntityType> {
        return entityTypes
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setEntityTypes(ENTITY_TYPES_KEY) { entityTypes, isWildcard ->
            this.entityTypes = entityTypes
            this.isEntityTypeWildcard = isWildcard
        }
        data.setDoubleRangeField(DISTANCE_KEY, true) { distanceRange = it }
        squaredRange = DoubleRange.of(distanceRange.min * distanceRange.min, distanceRange.max * distanceRange.max)
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if(player.world.entities.any { it != player
            && (isEntityTypeWildcard || it.type in entityTypes)
            && player.location.distanceSquared(it.location) in squaredRange }){//TODO 有点抽象
                trigger(player)
                break
            }
        }
    }

    companion object {
        const val ENTITY_TYPES_KEY = "entity"
        const val DISTANCE_KEY = "distance"
    }
}