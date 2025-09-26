package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import kotlin.properties.Delegates

class FallDistanceCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.FALL_DISTANCE
    var distance: Double by Delegates.notNull()

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setDoubleField(DISTANCE_KEY) { distance = it }
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (player.fallDistance >= distance) {
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

    companion object {
        const val DISTANCE_KEY = "distance"
    }
}