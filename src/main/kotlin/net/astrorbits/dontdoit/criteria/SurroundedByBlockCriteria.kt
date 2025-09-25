package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.BlockInspectCandidate
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.math.vector.Vec3d
import net.astrorbits.lib.math.vector.Vec3i.Companion.getBlock
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class SurroundedByBlockCriteria : Criteria(), Listener, BlockInspectCandidate {
    override val type: CriteriaType = CriteriaType.SURROUNDED_BY_BLOCK
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

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            val surroundingBlocks = getSurroundingBlocks(player)
            if (((surroundingBlocks.all { it.type in blockTypes } || isWildcard) && !reversed) ||
                ((surroundingBlocks.none { it.type in blockTypes } && !isWildcard) && reversed)
            ) {
                trigger(player)
                break
            }
        }
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

    companion object {
        const val BLOCK_TYPES_KEY = "block"
        const val REVERSED_KEY = "reversed"
    }
}