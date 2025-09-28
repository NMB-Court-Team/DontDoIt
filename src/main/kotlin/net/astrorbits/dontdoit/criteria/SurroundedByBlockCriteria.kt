package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.BlockInspectCandidate
import net.astrorbits.dontdoit.criteria.inspect.ImmediatelyTriggerInspector
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.math.vector.Vec3d
import net.astrorbits.lib.math.vector.Vec3i.Companion.getBlock
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class SurroundedByBlockCriteria : Criteria(), Listener, BlockInspectCandidate, ImmediatelyTriggerInspector {
    override val type: CriteriaType = CriteriaType.SURROUNDED_BY_BLOCK
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

    override fun shouldTrigger(player: Player): Boolean {
        val surroundingBlocks = getSurroundingBlocks(player)
        return ((surroundingBlocks.all { it.type in blockTypes } || isWildcard) && !reversed)
            || ((surroundingBlocks.none { it.type in blockTypes } && !isWildcard) && reversed)
    }

    fun getSurroundingBlocks(player: Player): List<Block> {
        val world = player.world
        val feetPos = Vec3d.fromLocation(player.location).floor()
        val eyePos = Vec3d.fromLocation(player.eyeLocation).floor()
        val surroundingPos = setOf(
            feetPos.modifyX { it - 1 },
            feetPos.modifyX { it + 1 },
            feetPos.modifyY { it - 1 },
            feetPos.modifyZ { it - 1 },
            feetPos.modifyZ { it + 1 },
            eyePos.modifyX { it - 1 },
            eyePos.modifyX { it + 1 },
            eyePos.modifyY { it + 1 },
            eyePos.modifyZ { it - 1 },
            eyePos.modifyZ { it + 1 },
        )
        return surroundingPos.map { world.getBlock(it) }
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getAnyTriggersMultiplier(bindTarget) { getBlockMultiplier(context) }
    }

    companion object {
        const val BLOCK_TYPES_KEY = "block"
        const val REVERSED_KEY = "reversed"
    }
}