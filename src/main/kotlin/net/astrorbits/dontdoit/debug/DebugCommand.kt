package net.astrorbits.dontdoit.debug

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.DynamicSettings
import net.astrorbits.dontdoit.criteria.system.CriteriaManager
import net.astrorbits.dontdoit.system.GameStateManager
import net.astrorbits.dontdoit.system.generate.GameAreaGenerator
import net.astrorbits.dontdoit.system.team.TeamManager
import net.astrorbits.lib.scoreboard.SidebarDisplay
import net.astrorbits.lib.text.SimpleTextBuilder
import net.astrorbits.lib.text.TextHelper
import net.astrorbits.lib.text.TextHelper.toMessage
import net.kyori.adventure.text.Component

object DebugCommand {
    const val COMMAND_NAME = "debug"

    private var sidebarDisplay: SidebarDisplay? = null

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
            ).then(Commands.literal("sidebar")
                .then(Commands.literal("init")
                    .executes { ctx ->
                        checkSidebarUninitialized()
                        sidebarDisplay = SidebarDisplay()
                        ctx.source.sender.sendMessage("Created sidebar display")
                        return@executes 1
                    }
                ).then(Commands.literal("destroy")
                    .executes { ctx ->
                        checkSidebarInitialized()
                        sidebarDisplay!!.unregisterDisplay()
                        sidebarDisplay = null
                        ctx.source.sender.sendMessage("Destroyed current sidebar display")
                        return@executes 1
                    }
                ).then(Commands.literal("addPlayer")
                    .then(Commands.argument("player", ArgumentTypes.player())
                        .executes { ctx ->
                            checkSidebarInitialized()
                            val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)[0]
                            sidebarDisplay!!.addPlayer(player)
                            ctx.source.sender.sendMessage("Added player ${player.name} to sidebar display")
                            return@executes 1
                        }
                    )
                ).then(Commands.literal("removePlayer")
                    .then(Commands.argument("player", ArgumentTypes.player())
                        .executes { ctx ->
                            checkSidebarInitialized()
                            val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)[0]
                            sidebarDisplay!!.removePlayer(player)
                            ctx.source.sender.sendMessage("Removed player ${player.name} to sidebar display")
                            return@executes 1
                        }
                    )
                ).then(Commands.literal("setTitle")
                    .then(Commands.argument("title", StringArgumentType.greedyString())
                        .executes { ctx ->
                            checkSidebarInitialized()
                            val title = TextHelper.parseMiniMessage(StringArgumentType.getString(ctx, "title"))
                            sidebarDisplay!!.title = title
                            ctx.source.sender.sendMessage(Component.text("Set sidebar display title to ").append(title))
                            return@executes 1
                        }
                    )
                ).then(Commands.literal("setContent")
                    .then(Commands.argument("content", StringArgumentType.greedyString())
                        .executes { ctx ->
                            checkSidebarInitialized()
                            val contents = StringArgumentType.getString(ctx, "content")
                                .split("\\n")
                                .map { line ->
                                    val entry = line.split("|")
                                    if (entry.isEmpty()) {
                                        SidebarDisplay.ScoreEntry(Component.empty(), Component.empty())
                                    } else if (entry.size == 1) {
                                        SidebarDisplay.ScoreEntry(TextHelper.parseMiniMessage(entry[0]), Component.empty())
                                    } else {
                                        SidebarDisplay.ScoreEntry(TextHelper.parseMiniMessage(entry[0]), TextHelper.parseMiniMessage(entry[1]))
                                    }
                                }
                            sidebarDisplay!!.content = contents
                            val builder = SimpleTextBuilder()
                            builder.append("Set sidebar display content to: ")
                            for ((name, number) in contents) {
                                builder.appendNewline().append(name).appendSpace(5).append(number)
                            }
                            ctx.source.sender.sendMessage(builder.build())
                            return@executes 1
                        }
                    )
                ).then(Commands.literal("hide")
                    .executes { ctx ->
                        checkSidebarInitialized()
                        sidebarDisplay!!.hide()
                        ctx.source.sender.sendMessage("Hided sidebar display")
                        return@executes 1
                    }
                ).then(Commands.literal("show")
                    .executes { ctx ->
                        checkSidebarInitialized()
                        sidebarDisplay!!.show()
                        ctx.source.sender.sendMessage("Showed sidebar display")
                        return@executes 1
                    }
                ).then(Commands.literal("getInfo")
                    .executes { ctx ->
                        checkSidebarInitialized()
                        val sidebar = sidebarDisplay!!
                        val builder = SimpleTextBuilder()
                        builder.append("Sidebar display info: ")
                        builder.append("\n|   Line count: ${sidebar.lineCount}")
                        builder.append("\n|   Title: ").append(sidebar.title)
                        builder.append("\n|   Content: ")
                        for ((name, number) in sidebar.content) {
                            builder.appendNewline().append("|   |   ").append(name).appendSpace(5).append(number)
                        }
                        ctx.source.sender.sendMessage(builder.build())
                        return@executes 1
                    }
                )
            )

        registrar.register(builder.build())
    }

    fun checkSidebarInitialized() {
        if (sidebarDisplay == null) {
            throw SIDEBAR_IS_NULL.create()
        }
    }

    fun checkSidebarUninitialized() {
        if (sidebarDisplay != null) {
            throw SIDEBAR_IS_NOTNULL.create()
        }
    }

    val SIDEBAR_IS_NULL = SimpleCommandExceptionType(Component.text("Sidebar display has not been initialized yet").toMessage())
    val SIDEBAR_IS_NOTNULL = SimpleCommandExceptionType(Component.text("Sidebar display has already been initialized").toMessage())
}