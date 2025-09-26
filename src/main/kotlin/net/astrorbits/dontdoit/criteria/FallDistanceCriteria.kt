package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID
import kotlin.properties.Delegates

class FallDistanceCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.FALL_DISTANCE
    var distance: Double by Delegates.notNull()
    private val lastTickFallDistances = mutableMapOf<UUID, Float>()
    private val lastTickYLevels = mutableMapOf<UUID, Double>()

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setDoubleField(DISTANCE_KEY) { distance = it }
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            val y = player.location.y
            val uuid = player.uniqueId
            val lastTickFallDistance = lastTickFallDistances[uuid] ?: 0f
            val lastTickYLevel = lastTickYLevels[uuid] ?: y
            val yLevelChange = lastTickYLevel - y
            lastTickFallDistances[uuid] = player.fallDistance
            lastTickYLevels[uuid] = y
            if (lastTickFallDistance + yLevelChange >= distance) {
                player.sendMessage("$lastTickFallDistance, $yLevelChange")
                trigger(player)
                break
            }
        }
    }

    @EventHandler
    fun onPlayerFallDamaged(event: EntityDamageEvent) {
        if (event.damageSource.damageType !in listOf(DamageType.FALL, DamageType.STALAGMITE)) return
        val player = event.entity as? Player ?: return
        if (player.fallDistance >= distance) {
            trigger(player)
            return
        }
    }

    @EventHandler
    fun onPlayerFallDamaged(event: PlayerMoveEvent) {
        if(!event.hasExplicitlyChangedPosition()) return

    }

    companion object {
        const val DISTANCE_KEY = "distance"
    }
}