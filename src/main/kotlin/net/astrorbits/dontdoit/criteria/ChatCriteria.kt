package net.astrorbits.dontdoit.criteria

import io.papermc.paper.event.player.AsyncChatEvent
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.lib.task.TaskHelper
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatCriteria() : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.CHAT
    var messageContains: Set<String> = emptySet()

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setField(MESSAGE_CONTAINS_KEY) { messageContains = it.split(',', '，').map{ it -> it.trim() }.toSet() }
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val message = PlainTextComponentSerializer.plainText().serialize(event.message())
        TaskHelper.runForceSync(DontDoIt.instance) {
            player.sendMessage(message)
            if (messageContains.any { message.contains(it, ignoreCase = true) }) {
                trigger(player)
            }
        }
    }

    companion object {
        const val MESSAGE_CONTAINS_KEY = "messages"
    }
}