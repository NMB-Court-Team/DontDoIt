package net.astrorbits.dontdoit.criteria.helper

import org.bukkit.entity.Entity

enum class DistanceToEntityMode(val check: (List<Entity>, (Entity) -> Boolean) -> Boolean) {
    ANY_OF({ list, predicate -> list.any(predicate) }),
    ALL_OF({ list, predicate -> list.all(predicate) })
}