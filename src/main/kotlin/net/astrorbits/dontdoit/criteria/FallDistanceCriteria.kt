package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.math.vector.Vec3d
import org.bukkit.Material
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import kotlin.properties.Delegates

class FallDistanceCriteria : Criteria() {
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
    fun onPlayerDamaged(event: EntityDamageEvent) {  //TODO 还是有问题
        if (event.damageSource.damageType != DamageType.FALL) return
        val player = event.entity as? Player ?: return
        if (player.fallDistance >= distance) {
            trigger(player)
            return
        }

        val fallDamage = event.damage
        val world = player.world
        val groundPos = player.location.clone().add(0.0, -0.1, 0.0)
        val block = world.getBlockAt(groundPos)
        val fallDistance = if (block.type == Material.POINTED_DRIPSTONE) {  // damage = (h-1)*2, h = damage/2+1
            fallDamage / 2 + 1
        } else fallDamage + 3

        if (fallDistance >= distance) {
            trigger(player)
        }
    }

    companion object {
        const val DISTANCE_KEY = "distance"
    }
}