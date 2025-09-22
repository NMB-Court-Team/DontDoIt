package net.astrorbits.dontdoit.system

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.astrorbits.lib.text.LegacyText
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

object TitleManager {
    private const val COMMAND_NAME = "showbegintitle"
    val title = Title.title(LegacyText.toComponent("主标题"), LegacyText.toComponent("副标题"))
    fun init(plugin: JavaPlugin) {
        val node = Commands.literal(COMMAND_NAME).requires { it.sender is Player }
            .executes { ctx ->
                val player = ctx.source.sender as Player
                player.showTitle(title)
                return@executes 1
            }.build()

        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS.newHandler { event ->
            val registrar = event.registrar()
            registrar.register(node)
        })
    }
}