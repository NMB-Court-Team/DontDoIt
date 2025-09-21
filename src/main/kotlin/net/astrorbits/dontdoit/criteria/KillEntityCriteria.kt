package net.astrorbits.dontdoit.criteria

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class KillEntityCriteria : Criteria(), Listener {
    override val type = CriteriaType.KILL_ENTITY
    lateinit var entityTypes: Set<EntityType>
    var isWildcard: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setEntityTypes(ENTITY_TYPES_KEY) { entityTypes, isWildcard ->
            this.entityTypes = entityTypes
            this.isWildcard = isWildcard
        }
    }

    @EventHandler
    fun onEntityBeingDamaged(event: EntityDamageEvent) {
        val player = event.damageSource.causingEntity ?: return
        if (player.type != EntityType.PLAYER || !event.entity.isDead) return
        val entity = event.entity
        if (isWildcard || entity.type in entityTypes) {
            trigger(player as Player)
        }
    }

    companion object {
        const val ENTITY_TYPES_KEY = "entity"
    }
}