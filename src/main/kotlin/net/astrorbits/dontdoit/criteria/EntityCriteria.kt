package net.astrorbits.dontdoit.criteria

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.astrorbits.lib.Identifier
import org.bukkit.entity.EntityType

abstract class EntityCriteria : Criteria() {
    lateinit var entityTypes: Set<EntityType>
    var isWildcard: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        val entries = data.getCsvEntries(ENTITY_TYPES_KEY, AbsentBehavior.WILDCARD)

        val result = HashSet<EntityType>()
        for ((entity, isTag, isReversed) in entries) {
            if (isReversed && result.isEmpty()) {
                result.addAll(EntityType.entries)
            }
            val entityId = Identifier.of(entity)
            if (!entityId.isVanilla()) throw InvalidCriteriaException(this, "Non-vanilla entities are not supported")
            val entities = HashSet<EntityType>()
            if (isTag) {
                val tagKey = entityId.toKey()
                val tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE).tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this, "Invalid entity tag: $entity")
                entities.addAll(tag.values().map { EntityType.fromName(entityId.path)!! })
            } else {
                val entityType = EntityType.fromName(entityId.path) ?: throw InvalidCriteriaException(this, "Invalid entity: $entity")
                entities.add(entityType)
            }
            if (isReversed) {
                result.removeAll(entities)
            } else {
                result.addAll(entities)
            }
        }
        entityTypes = result

        isWildcard = entries.isWildcard
    }

    companion object {
        const val ENTITY_TYPES_KEY = "entity"
    }
}