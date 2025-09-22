package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.HurtEntityCriteria.Companion
import net.astrorbits.dontdoit.criteria.type.DamageTypeCriteria
import net.astrorbits.dontdoit.criteria.type.EntityCriteria
import net.astrorbits.lib.range.DoubleRange
import org.bukkit.damage.DamageType
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class HurtByEntityCriteria : Criteria(), Listener, EntityCriteria, DamageTypeCriteria {
    override val type: CriteriaType = CriteriaType.HURT_BY_ENTITY
    lateinit var entityTypes: Set<EntityType>
    var isEntityTypeWildcard: Boolean = false
    lateinit var damageTypes: Set<DamageType>
    var isDamageTypeWildcard: Boolean = false
    var damageAmountRange: DoubleRange = DoubleRange.INFINITY
    var rangeReversed: Boolean = false

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
        data.setDoubleRangeField(DAMAGE_AMOUNT_RANGE_KEY, true) { damageAmountRange = it }
        data.setBoolField(RANGE_REVERSED_KEY, true) { rangeReversed = it }
    }

    @EventHandler
    fun onDamageByEntity(event: EntityDamageByEntityEvent) {
        val player = event.entity as? Player ?: return
        val entity = event.damager
        val damageType = event.damageSource.damageType
        if ((isEntityTypeWildcard || entity.type in entityTypes) &&
            (isDamageTypeWildcard || damageType in damageTypes) &&
            ((event.damage in damageAmountRange) xor rangeReversed)
        ) {
            trigger(player)
        }
    }

    companion object {
        const val ENTITY_TYPES_KEY = "entity"
        const val DAMAGE_TYPES_KEY = "damage_type"
        const val DAMAGE_AMOUNT_RANGE_KEY = "amount"
        const val RANGE_REVERSED_KEY = "reversed"
    }
}