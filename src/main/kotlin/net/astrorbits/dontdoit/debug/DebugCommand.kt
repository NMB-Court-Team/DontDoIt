package net.astrorbits.dontdoit.debug

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.DynamicSettings
import net.astrorbits.dontdoit.criteria.system.CriteriaManager
import net.astrorbits.dontdoit.system.GameStateManager
import net.astrorbits.dontdoit.system.generate.GameAreaGenerator
import net.astrorbits.dontdoit.system.team.TeamManager
import net.astrorbits.lib.text.TextHelper

object DebugCommand {
    const val COMMAND_NAME = "debug"

    fun register(registrar: Commands) {
        val builder = Commands.literal(COMMAND_NAME).requires { it.sender.isOp }
            .then(Commands.literal("breakpoint")
                .executes { ctx ->
                    val teamManager = TeamManager
                    val gameAreaGenerator = GameAreaGenerator
                    val criteriaManager = CriteriaManager
                    val gameStateManager = GameStateManager
                    val configs = Configs
                    val dynamicSettings = DynamicSettings

                    println("breakpoint!")
                    return@executes 1
                }
            ).then(Commands.literal("message")
                .then(Commands.argument("miniMessage", StringArgumentType.greedyString())
                    .executes { ctx ->
                        val message = StringArgumentType.getString(ctx, "miniMessage")
                        ctx.source.sender.sendMessage(TextHelper.parseMiniMessage(message))
                        return@executes 1
                    }
                )
            )

        registrar.register(builder.build())
    }
}