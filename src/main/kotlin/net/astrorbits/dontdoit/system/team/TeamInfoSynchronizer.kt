package net.astrorbits.dontdoit.system.team

import net.astrorbits.lib.NMSWarning
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.world.scores.PlayerTeam
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.scoreboard.Team
import java.lang.reflect.Field

@NMSWarning
object TeamInfoSynchronizer {
    fun syncTeamInfos(teams: List<TeamData>) {
        val teamPackets: List<ClientboundSetPlayerTeamPacket> = teams.map { teamData ->
            val team = teamData.team.getHandle()
            return@map ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true)
        } + ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(TeamManager.spectatorTeam.getHandle(), true)
        for (player in Bukkit.getOnlinePlayers()) {
            val nmsPlayer = (player as? CraftPlayer)?.handle ?: continue
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