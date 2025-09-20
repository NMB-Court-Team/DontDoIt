package net.astrorbits.doNotDoIt.criteria

import net.astrorbits.doNotDoIt.team.TeamManager
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BreakBlockCriteria : CriteriaListener(), Listener {
    override val type = CriteriaType.BREAK_BLOCK
    override val placeholder: String = "{block}"
    @EventHandler
    fun onBreakBlock(event: BlockBreakEvent) {

        val team = TeamManager.getTeamOf(event.player.uniqueId)?:return
        val criteriaData = team.criteriaData?:return
        val criteriaType = criteriaData.first
        if(this.type != criteriaType) return
        val parms = criteriaData.second


        if (event.block.type == Material.getMaterial(parms[0])) {
            trigger(team, this.type)
        }
    }
}