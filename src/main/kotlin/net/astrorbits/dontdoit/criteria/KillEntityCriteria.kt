package net.astrorbits.dontdoit.criteria

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class KillEntityCriteria : EntityCriteria(), Listener {
    override val type = CriteriaType.KILL_ENTITY

    @EventHandler
    fun onEntityBeingDamaged(event: EntityDamageEvent) {
        val player = event.damageSource.causingEntity ?: return
        if (player.type != EntityType.PLAYER || !event.entity.isDead) return
        val entity = event.entity
        if (isWildcard || entity.type in entityTypes) {
            trigger(player as Player)
        }
    }
}