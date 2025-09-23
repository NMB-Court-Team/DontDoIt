package net.astrorbits.dontdoit.system

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.astrorbits.lib.text.TextHelper
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

object TitleManager {
    private const val COMMAND_NAME = "showtitle"

    fun init(plugin: JavaPlugin) {
        val node = Commands.literal(COMMAND_NAME)
            .requires { it.sender is Player }
            .then(
                Commands.argument("message", StringArgumentType.greedyString()) // 捕获整行文字
                    .executes { ctx ->
                        val player = ctx.source.sender as Player
                        val msg = TextHelper.parseMiniMessage(StringArgumentType.getString(ctx, "message"))

                        val title = Title.title(
                            msg,
                            Component.empty()
                        )
                        player.sendMessage(msg)

                        player.showTitle(title)
                        return@executes 1
                    }
            )
            .build()

        plugin.lifecycleManager.registerEventHandler(
            LifecycleEvents.COMMANDS.newHandler { event ->
                val registrar = event.registrar()
                registrar.register(node)
            }
        )
    }
}