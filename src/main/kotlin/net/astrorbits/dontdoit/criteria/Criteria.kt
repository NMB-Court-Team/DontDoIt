package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.dontdoit.team.TeamManager
import net.astrorbits.lib.text.TextHelper
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

abstract class Criteria {
    abstract val type: CriteriaType
    lateinit var displayName: Component
    val holders: MutableList<TeamData> = mutableListOf()

    /**
     * 当队伍绑定了该词条时调用
     *
     * 绑定操作包括：
     * 1. 游戏开始时获得此词条
     * 2. 词条自动更换或触发后，替换到此词条
     * @param teamData 绑定了此词条的队伍
     */
    open fun onBind(teamData: TeamData) {
        holders.add(teamData)
    }

    /**
     * 当队伍解除绑定该词条时调用
     *
     * 解除绑定操作包括：
     * 1. 此词条自动更换成了另一个词条
     * 2. 此词条被触发
     * 3. 游戏结束时取消所有玩家的词条，包括此词条
     * @param teamData 解除绑定了此词条的队伍
     */
    open fun onUnbind(teamData: TeamData) {
        holders.remove(teamData)
    }

    /**
     * 游戏进行期间每刻调用
     * @param teamData 持有此词条的队伍
     */
    open fun tick(teamData: TeamData) { }

    fun trigger(player: Player) {
        CriteriaManager.trigger(this, player)
    }

    fun trigger(playerUuid: UUID) {
        val player = Bukkit.getPlayer(playerUuid) ?: return
        trigger(player)
    }

    fun trigger(teamData: TeamData) {
        CriteriaManager.trigger(this, teamData)
    }

    /**
     * 读取数据，用于初始化词条
     */
    open fun readData(data: Map<String, String>) {
        val name = data["name"] ?: throw InvalidCriteriaException(this, "Missing key 'name'")
        displayName = TextHelper.parseMiniMessage(name)
    }
}
