package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.criteria.inspect.InventoryItemInspectCandidate
import net.astrorbits.dontdoit.criteria.inspect.SourcedDamageInspector
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.range.DoubleRange
import org.bukkit.Material
import org.bukkit.damage.DamageType
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import kotlin.math.max

class HurtEntityCriteria : Criteria(), Listener, SourcedDamageInspector, InventoryItemInspectCandidate {
    override val type: CriteriaType = CriteriaType.HURT_ENTITY
    lateinit var entityTypes: Set<EntityType>
    var isEntityTypeWildcard: Boolean = false
    lateinit var damageTypes: Set<DamageType>
    var isDamageTypeWildcard: Boolean = false
    lateinit var mainhandItemTypes: Set<Material>
    var isMainhandItemTypeWildcard: Boolean = false
    var damageAmountRange: DoubleRange = DoubleRange.INFINITY
    var rangeReversed: Boolean = false

    override fun getCandidateEntityTypes(): Set<EntityType> {
        return entityTypes
    }

    override fun canMatchAnyEntity(): Boolean {
        return isEntityTypeWildcard
    }

    override fun getCandidateDamageTypes(): Set<DamageType> {
        return damageTypes
    }

    override fun canMatchAnyDamageType(): Boolean {
        return isDamageTypeWildcard
    }

    override fun getCandidateInventoryItemTypes(): Set<Material> {
        return mainhandItemTypes
    }

    override fun canMatchAnyInventoryItem(): Boolean {
        return isMainhandItemTypeWildcard
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
        data.setItemTypes(MAINHAND_KEY) { itemTypes, isWildcard ->
            this.mainhandItemTypes = itemTypes
            this.isMainhandItemTypeWildcard = isWildcard
        }
        data.setDoubleRangeField(DAMAGE_AMOUNT_RANGE_KEY, true) { damageAmountRange = it }
        data.setBoolField(RANGE_REVERSED_KEY, true) { rangeReversed = it }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        val player = event.damageSource.causingEntity as? Player ?: return
        val entity = event.entity
        val damageType = event.damageSource.damageType
        if ((isEntityTypeWildcard || entity.type in entityTypes) &&
            (isMainhandItemTypeWildcard || player.inventory.itemInMainHand.type in mainhandItemTypes) &&
            (isDamageTypeWildcard || damageType in damageTypes) &&
            ((event.finalDamage in damageAmountRange) xor rangeReversed)
        ) {
            trigger(player)
        }
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * max(getSourcedDamageMultiplier(context), getInventoryItemMultiplier(context))
    }

    companion object {
        const val ENTITY_TYPES_KEY = "entity"
        const val DAMAGE_TYPES_KEY = "damage_type"
        const val DAMAGE_AMOUNT_RANGE_KEY = "amount"
        const val MAINHAND_KEY = "mainhand"
        const val RANGE_REVERSED_KEY = "reversed"
    }
}