package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.BlockInspectCandidate
import net.astrorbits.dontdoit.system.CriteriaChangeReason
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import java.util.UUID

class PlaceBlockCriteria : Criteria(), Listener, BlockInspectCandidate {
    override val type = CriteriaType.PLACE_BLOCK
    lateinit var blockTypes: Set<Material>
    var count: Int = 1
    var isWildcard: Boolean = false
    var placeCount = mutableMapOf<UUID, Int>()

    override fun canMatchAnyBlock(): Boolean {
        return isWildcard
    }

    override fun getCandidateBlockTypes(): Set<Material> {
        return blockTypes
    }

    override fun onBind(teamData: TeamData, reason: CriteriaChangeReason) {
        super.onBind(teamData, reason)
        for (player in teamData.members) {
            placeCount[player.uniqueId] = 0
        }
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setBlockTypes(BLOCK_TYPES_KEY) { blockTypes, isWildcard ->
            this.blockTypes = blockTypes
            this.isWildcard = isWildcard
        }
        data.setIntField(COUNT_KEY, true) { count = it }
        if (count <= 0) throw InvalidCriteriaException(this, "Count should be at least 1")
    }

    override fun onUnbind(teamData: TeamData, reason: CriteriaChangeReason): Boolean {
        for (player in teamData.members) {
            placeCount.remove(player.uniqueId)
        }
        return super.onUnbind(teamData, reason)
    }

    @EventHandler
    fun onPlaceBlock(event: BlockPlaceEvent) {
        if (isWildcard || event.block.type in blockTypes) {
            val player = event.player
            val currentCount = placeCount.getOrPut(player.uniqueId) { 0 }
            val newCount = currentCount + 1
            placeCount[player.uniqueId] = newCount

            if (newCount >= count) {
                trigger(player)
            }
        }
    }

    companion object {
        const val BLOCK_TYPES_KEY = "block"
        const val COUNT_KEY = "count"
    }
}