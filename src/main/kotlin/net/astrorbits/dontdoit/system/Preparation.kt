package net.astrorbits.dontdoit.system

import net.astrorbits.dontdoit.DontDoIt
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team

object Preparation : Listener {

    const val TEAM_ITEM_SLOT = 0

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!GameStateManager.isWaiting()) return
        event.player.sendMessage("Hello, ${event.player.name}")
        event.player.inventory.setItem(TEAM_ITEM_SLOT, ItemStack(Material.LIGHT_GRAY_WOOL))
    }

    @EventHandler
    fun onInventory(event: InventoryClickEvent){
        if (!GameStateManager.isWaiting()) return
        event.isCancelled = true
    }

    @EventHandler
    fun onTeamSelect(event: PlayerDropItemEvent){
        if (!GameStateManager.isWaiting()) return
        val type = event.itemDrop.itemStack.type
//        val teamColor = TeamColor.entries.find { it.material == type } ?: return
//        if(TeamColor.contains(type)){
//            event.player.inventory.setItem(TEAM_ITEM_SLOT, ItemStack(teamColor.next().material))
//            assignTeamByWool(event.player, teamColor.next().color)
//        }
        //TODO: 循环切换
        event.itemDrop.remove()
    }

    fun assignTeamByWool(player: Player, color: NamedTextColor) {
        val scoreboard: Scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        val teamName = color.toString().lowercase()
        val team: Team = scoreboard.getTeam(teamName) ?: scoreboard.registerNewTeam(teamName)
        team.color(color)
        team.addEntry(player.name)
        val coloredName = Component.text(team.name).color(color)
        player.scoreboard = scoreboard
        player.sendMessage(
            Component.text("你加入了[")
                .append(coloredName)
                .append(Component.text("] 队"))
        )
    }

    fun register(plugin: JavaPlugin){
        Bukkit.getServer().pluginManager.registerEvents(Preparation, plugin)
    }
}