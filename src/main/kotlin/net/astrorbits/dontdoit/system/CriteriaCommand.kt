package net.astrorbits.dontdoit.system

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.dontdoit.system.team.TeamManager
import net.astrorbits.lib.text.TextHelper.toMessage
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object CriteriaCommand {
    const val COMMAND_NAME = "criteria"

    fun register(registrar: Commands) {
        val builder = Commands.literal(COMMAND_NAME)
        builder.then(Commands.literal("trigger")
            .then(Commands.argument("player", ArgumentTypes.player())
                .executes { ctx ->
                    if (!GameStateManager.isRunning()) throw GAME_NOT_START.create()
                    val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)[0]
                    val team = TeamManager.getTeam(player)
                    if (team == null || team.isDead || team.criteria?.type != CriteriaType.USER_DEFINED) throw INVALID_PLAYER.create(player.name)
                    team.criteria!!.trigger(team)
                    return@executes 1
                }
            )
        ).then(Commands.literal("guess")
            .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("guessed", BoolArgumentType.bool())
                    .requires { it.sender is Player }
                    .executes { ctx ->
                        if (!GameStateManager.isRunning()) throw GAME_NOT_START.create()
                        val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)[0]
                        val team = TeamManager.getTeam(player)
                        if (team == null || team.isDead || team.criteria?.type != CriteriaType.USER_DEFINED) throw INVALID_PLAYER.create(player.name)
                        if (player in team) throw GUESS_SELF_CRITERIA.create()
                        val guessed = BoolArgumentType.getBool(ctx, "guessed")
                        TeamManager.guess(team, guessed)
                        return@executes 1
                    }
                )
            )
        )

        registrar.register(builder.build())
    }

    private val GAME_NOT_START = SimpleCommandExceptionType(Component.text(Configs.COMMAND_GAME_NOT_START.get()).toMessage())
    private val INVALID_PLAYER = DynamicCommandExceptionType { player -> Component.text(Configs.COMMAND_INVALID_PLAYER.get().format(player)).toMessage() }
    private val GUESS_SELF_CRITERIA = SimpleCommandExceptionType(Component.text(Configs.COMMAND_GUESS_SELF_CRITERIA.get()).toMessage())
}