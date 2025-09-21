package net.astrorbits.dontdoit.criteria

import net.astrorbits.lib.text.TextHelper
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

abstract class Criteria {
    abstract val type: CriteriaType
    lateinit var displayName: Component

    fun trigger(player: Player) {
        CriteriaManager.trigger(this, player)
    }

    fun trigger(playerUuid: UUID) {
        val player = Bukkit.getPlayer(playerUuid) ?: return
        trigger(player)
    }

    open fun readData(data: Map<String, String>) {
        val name = data["name"] ?: throw InvalidCriteriaException(this, "Missing key 'name'")
        displayName = TextHelper.parseMiniMessage(name)
    }
}
