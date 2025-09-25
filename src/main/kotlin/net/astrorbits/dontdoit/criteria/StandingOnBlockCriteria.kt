package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.BlockInspectCandidate
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.math.vector.Box
import net.astrorbits.lib.math.vector.Vec3d
import org.bukkit.Material
import org.bukkit.event.Listener

class StandingOnBlockCriteria : Criteria(), Listener, BlockInspectCandidate {
    override val type: CriteriaType = CriteriaType.STANDING_ON_BLOCK
    lateinit var blockTypes: Set<Material>
    var isWildcard: Boolean = false
    var reversed: Boolean = false

    override fun getCandidateBlockTypes(): Set<Material> {
        return blockTypes
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setBlockTypes(BLOCK_TYPES_KEY) { blockTypes, isWildcard ->
            this.blockTypes = blockTypes
            this.isWildcard = isWildcard
        }
        data.setBoolField(REVERSED_KEY, true) { reversed = it }
    }

    @Suppress("DEPRECATION")
    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (isWildcard && reversed && !player.isOnGround) {
                trigger(player)
                break
            }
            if (isWildcard && !reversed && player.isOnGround) {
                trigger(player)
                break
            }
            if (player.isOnGround) {  //TODO 滞空(block = *, reversed = true)的判定有问题，会无视条件立刻触发
                val world = player.world
                val groundPos = player.location.clone().add(0.0, -0.1, 0.0)
                val block = world.getBlockAt(groundPos)
                val blockPos = Vec3d.fromLocation(groundPos).floor()
                if (block.isEmpty ||
                    !block.isCollidable ||
                    (!reversed && block.type !in blockTypes) ||
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
        const val BLOCK_TYPES_KEY = "block"
        const val REVERSED_KEY = "reversed"
    }
}