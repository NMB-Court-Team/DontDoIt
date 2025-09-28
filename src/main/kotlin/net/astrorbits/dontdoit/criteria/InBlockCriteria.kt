package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.BlockInspectCandidate
import net.astrorbits.dontdoit.criteria.inspect.ImmediatelyTriggerInspector
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class InBlockCriteria : Criteria(), Listener, BlockInspectCandidate, ImmediatelyTriggerInspector {
    override val type: CriteriaType = CriteriaType.IN_BLOCK
    lateinit var blockTypes: Set<Material>
    var reversed: Boolean = false

    override fun getCandidateBlockTypes(): Set<Material> {
        return blockTypes
    }

    override fun canMatchAnyBlock(): Boolean {
        return false
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setBlockTypes(BLOCK_TYPES_KEY) { blockTypes, _ ->
            this.blockTypes = blockTypes
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
        val world = player.world
        val checkBlocks = setOf(
            world.getBlockAt(player.location), // feet
            world.getBlockAt(player.location.clone().add(0.0, player.height / 2, 0.0)), // middle
            world.getBlockAt(player.eyeLocation) // eye
        )
        return checkBlocks.any { it.type in blockTypes } xor reversed
    }

    override fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double {
        return weight * getAnyTriggersMultiplier(bindTarget) { getBlockMultiplier(context) }
    }

    companion object {
        const val BLOCK_TYPES_KEY = "block"
        const val REVERSED_KEY = "reversed"
    }
}