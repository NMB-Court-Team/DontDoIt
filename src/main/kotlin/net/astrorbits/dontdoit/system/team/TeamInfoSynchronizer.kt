package net.astrorbits.dontdoit.system.team

import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.lib.NMSWarning
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.world.scores.PlayerTeam
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team
import java.lang.reflect.Field

@NMSWarning
object TeamInfoSynchronizer {
    fun syncTeamInfos(teams: List<TeamData>, player: Player? = null) {
        val teamPackets: List<ClientboundSetPlayerTeamPacket> = teams.filter { it.hasMember }.map { teamData ->
            val team = teamData.team.getHandle()
            return@map ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true)
        }
        val players = if (player == null) Bukkit.getOnlinePlayers() else listOf(player)
        for (p in players) {
            val nmsPlayer = (p as? CraftPlayer)?.handle ?: continue
            teamPackets.forEach { nmsPlayer.connection.send(it) }
        }
    }

    private val craftTeamClass: Class<*> = Class.forName("org.bukkit.craftbukkit.scoreboard.CraftTeam")

    private val nmsTeamField: Field = craftTeamClass.getDeclaredField("team").apply { isAccessible = true }

    private fun Team.getHandle(): PlayerTeam {
        if (!craftTeamClass.isInstance(this)) {
            throw IllegalArgumentException("Team is not a CraftTeam: ${this::class.java.name}")
        }
        return nmsTeamField.get(this) as PlayerTeam
    }
}