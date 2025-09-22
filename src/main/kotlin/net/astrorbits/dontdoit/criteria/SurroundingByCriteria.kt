package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.type.BlockCriteria
import net.astrorbits.dontdoit.team.TeamData
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class SurroundingByCriteria : Criteria(), Listener, BlockCriteria {
    override val type: CriteriaType = CriteriaType.SURROUNDING_BY
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
            if(getCheckBlocks(player).all { it.type in blockTypes } && !reversed){
                trigger(player)
                break
            }else if(!getCheckBlocks(player).any { it.type in blockTypes } && reversed){
                trigger(player)
                break
            }
        }
    }

    fun getCheckBlocks(player: Player): List<Block> {
        val toCheck = mutableListOf<Block>()
        val world = player.world
        val x = player.x.toInt()
        val y = player.y.toInt()
        val z = player.z.toInt()
        toCheck.add(world.getBlockAt(x + 1, y, z))
        toCheck.add(world.getBlockAt(x - 1, y, z))
        toCheck.add(world.getBlockAt(x, y, z + 1))
        toCheck.add(world.getBlockAt(x, y, z - 1))
        toCheck.add(world.getBlockAt(x, y - 1, z))
        if (player.isSneaking || player.isSwimming) {
            // --- 潜行 / 游泳姿态，1x1x1 ---
            toCheck.add(world.getBlockAt(x, y + 1, z))
        } else {
            // --- 站立姿态，1x2x1 ---\
            toCheck.add(world.getBlockAt(x + 1, y + 1, z))
            toCheck.add(world.getBlockAt(x - 1, y + 1, z))
            toCheck.add(world.getBlockAt(x, y + 1, z + 1))
            toCheck.add(world.getBlockAt(x, y + 1, z - 1))

            toCheck.add(world.getBlockAt(x, y + 2, z))
        }
        return toCheck
    }
    companion object {
        const val BLOCK_TYPES_KEY = "block"
        const val REVERSED_KEY = "reversed"
    }
}