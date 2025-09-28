package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.CriteriaChangeReason
import net.astrorbits.dontdoit.system.team.TeamData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent

class SelectedHotbarIndexCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.SELECTED_HOTBAR_INDEX
    var index: Int = 0

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setIntField(INDEX_KEY, false) { index = it }
    }

    override fun onBind(teamData: TeamData, reason: CriteriaChangeReason) {
        super.onBind(teamData, reason)
        for (player in teamData.members){
            if (player.inventory.heldItemSlot == index){
                trigger(player)
            }
        }
    }

    @EventHandler
    fun onSelectedHotbarChange(event: PlayerItemHeldEvent){
        if (event.newSlot == index){
            trigger(event.player)
        }
    }

    companion object {
        const val INDEX_KEY = "index"
    }
}