package net.astrorbits.dontdoit.criteria

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.astrorbits.lib.Identifier
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class KillEntityCriteria : Criteria(), Listener {
    override val type = CriteriaType.KILL_ENTITY
    lateinit var entityTypes: List<EntityType>
    var isWildcard: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        val entityContent = data[ENTITY_TYPES_KEY] ?: throw InvalidCriteriaException(this, "Missing key '$ENTITY_TYPES_KEY'")
        val entities = entityContent.replace("\n", "").split(",").map { it.trim() }
        val result = ArrayList<EntityType>()
        for (entity in entities) {
            if (entity.startsWith("#")) {
                val tagKey = Identifier.of(entity.removePrefix("#")).toKey()
                val tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE).tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this, "Invalid entity tag: $entity")
                result.addAll(tag.values().map { EntityType.fromName(entity)!! })
            } else if (entity == "*") {
                isWildcard = true
            } else {
                val entityType = EntityType.fromName(entity) ?: throw InvalidCriteriaException(this, "Invalid entity: $entity")
                result.add(entityType)
            }
        }
        entityTypes = result
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