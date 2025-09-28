package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.BlockInspectCandidate
import net.astrorbits.dontdoit.criteria.inspect.ImmediatelyTriggerInspector
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.math.vector.Box
import net.astrorbits.lib.math.vector.Vec3d
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class StandingOnBlockCriteria : Criteria(), Listener, BlockInspectCandidate, ImmediatelyTriggerInspector {
    override val type: CriteriaType = CriteriaType.STANDING_ON_BLOCK
    lateinit var blockTypes: Set<Material>
    var isWildcard: Boolean = false
    var reversed: Boolean = false

    override fun getCandidateBlockTypes(): Set<Material> {
        return blockTypes
    }

    override fun canMatchAnyBlock(): Boolean {
        return isWildcard
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setBlockTypes(BLOCK_TYPES_KEY) { blockTypes, isWildcard ->
            this.blockTypes = blockTypes
            this.isWildcard = isWildcard
        }
        data.setBoolField(REVERSED_KEY, true) { reversed = it }
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (shouldTrigger(player)) {
                trigger(player)
                break
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun shouldTrigger(player: Player): Boolean {
        if (isWildcard && reversed) {
            return !player.isOnGround
        }
        if (isWildcard) {
            return player.isOnGround
        }
        if (player.isOnGround) {
            val world = player.world
            val groundPos = player.location.clone().add(0.0, -0.1, 0.0)
            val block = world.getBlockAt(groundPos)
            val blockPos = Vec3d.fromLocation(groundPos).floor()
            if (block.isEmpty ||
                !block.isCollidable ||
                (!reversed && block.type !in blockTypes) ||
                (reversed && block.type in blockTypes)
            ) return false

            val isPosInCollisionShape = block.collisionShape.boundingBoxes.any { boundingBox ->
                val box = Box.fromBoundingBox(boundingBox).offset(blockPos)
                return@any groundPos in box
            }
            if (isPosInCollisionShape) {
                return true
            }
        }
        return false
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getAnyTriggersMultiplier(bindTarget) { getBlockMultiplier(context) }
    }

    companion object {
        const val BLOCK_TYPES_KEY = "block"
        const val REVERSED_KEY = "reversed"
    }
}