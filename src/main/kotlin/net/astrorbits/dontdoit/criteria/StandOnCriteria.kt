package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.lib.math.vector.Box
import net.astrorbits.lib.math.vector.Vec3d
import org.bukkit.event.Listener

class StandOnCriteria : BlockCriteria(), Listener {
    override val type: CriteriaType = CriteriaType.STAND_ON
    var reversed: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setBoolField(REVERSED_KEY, true) { reversed = it }
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (isWildcard && reversed && !player.isOnGround) {
                trigger(player)
                break
            }
            if (player.isOnGround) {
                val world = player.world
                val groundPos = player.location.clone().add(0.0, -0.01, 0.0)
                val block = world.getBlockAt(groundPos)
                val blockPos = Vec3d.fromLocation(groundPos).floor()
                if (block.isEmpty ||
                    !block.isCollidable ||
                    (!reversed && !isWildcard && block.type !in blockTypes) ||
                    (reversed && block.type in blockTypes)
                ) continue

                val isPosInCollisionShape = block.collisionShape.boundingBoxes.any { boundingBox ->
                    val box = Box.fromBoundingBox(boundingBox).offset(blockPos)
                    return@any groundPos in box
                }
                if (isPosInCollisionShape) {
                    trigger(player)
                    break
                }
            }
        }
    }

    companion object {
        const val REVERSED_KEY = "reversed"
    }
}