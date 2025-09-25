package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.DamageTypeInspectCandidate
import net.astrorbits.dontdoit.criteria.inspect.EntityInspectCandidate
import org.bukkit.damage.DamageType
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class KillEntityCriteria : Criteria(), Listener, EntityInspectCandidate, DamageTypeInspectCandidate {
    override val type = CriteriaType.KILL_ENTITY
    lateinit var entityTypes: Set<EntityType>
    var isEntityTypeWildcard: Boolean = false
    lateinit var damageTypes: Set<DamageType>
    var isDamageTypeWildcard: Boolean = false

    override fun getCandidateEntityTypes(): Set<EntityType> {
        return entityTypes
    }

    override fun getCandidateDamageTypes(): Set<DamageType> {
        return damageTypes
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setEntityTypes(ENTITY_TYPES_KEY) { entityTypes, isWildcard ->
            this.entityTypes = entityTypes
            this.isEntityTypeWildcard = isWildcard
        }
        data.setDamageTypes(DAMAGE_TYPES_KEY) { damageTypes, isWildcard ->
            this.damageTypes = damageTypes
            this.isDamageTypeWildcard = isWildcard
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDeathEvent) {
        val player = event.damageSource.directEntity as? Player ?: return
        val entity = event.entity
        val damageType = event.damageSource.damageType
        if ((isEntityTypeWildcard || entity.type in entityTypes) &&
            (isDamageTypeWildcard || damageType in damageTypes)
        ) {
            trigger(player)
        }
    }

    companion object {
        const val ENTITY_TYPES_KEY = "entity"
        const val DAMAGE_TYPES_KEY = "damage_type"
    }
}