package net.astrorbits.dontdoit.system

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.DynamicSettings
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.dontdoit.system.team.TeamManager
import net.astrorbits.lib.command.CommandHelper
import net.astrorbits.lib.text.TextHelper.toMessage
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

object CriteriaCommand {
    const val COMMAND_NAME = "criteria"

    fun register(registrar: Commands) {
        val builder = Commands.literal(COMMAND_NAME)
        builder.then(Commands.literal("trigger")
            .then(Commands.argument("team", TeamIdArgumentType())
                .executes { ctx ->
                    if (!GameStateManager.isRunning()) throw GAME_NOT_START.create()
                    val team = ctx.getArgument("team", TeamData::class.java)
                    if (team == null || team.isEliminated) throw INVALID_TEAM_NAME.create(team.teamId)
                    if (team.criteria?.type != CriteriaType.USER_DEFINED) throw NOT_CUSTOM_CRITERIA.create(team.teamId)
                    team.criteria!!.trigger(team)
                    return@executes 1
                }
            )
        ).then(Commands.literal("guess")
            .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("guessed", BoolArgumentType.bool())
                    .requires { it.sender is Player }
                    .executes { ctx ->
                        if (!DynamicSettings.allowGuessCriteria) throw GUESS_NOT_ENABLED.create()
                        if (!GameStateManager.isRunning()) throw GAME_NOT_START.create()
                        val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)[0]
                        val team = TeamManager.getTeam(player)
                        if (team == null || team.isEliminated || team.criteria?.type != CriteriaType.USER_DEFINED) throw INVALID_PLAYER.create(player.name)
                        if (player in team) throw GUESS_SELF_CRITERIA.create()
                        val guessed = BoolArgumentType.getBool(ctx, "guessed")
                        val cooldown = TeamManager.guess(player, team, guessed)
                        if (cooldown != null) {
                            throw GUESS_IN_COOLDOWN.create(player.name, cooldown)
                        }
                        return@executes 1
                    }
                )
            )
        )

        registrar.register(builder.build())
    }

    class TeamIdArgumentType : CustomArgumentType<TeamData, String> {
        override fun parse(reader: StringReader): TeamData {
            val teamId = reader.readUnquotedString()
            val teamIds = TeamManager.getInUseTeams()
            return teamIds[teamId] ?: throw INVALID_TEAM_NAME.create(teamId)
        }

        override fun getNativeType(): ArgumentType<String> {
            return StringArgumentType.word()
        }

        override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            return CommandHelper.suggestMatching(TeamManager.getInUseTeams().keys, builder)
        }
    }

    private val GAME_NOT_START = SimpleCommandExceptionType(Component.text(Configs.COMMAND_GAME_NOT_START.get()).toMessage())
    private val GUESS_NOT_ENABLED = SimpleCommandExceptionType(Component.text(Configs.COMMAND_GUESS_NOT_ENABLED.get()).toMessage())
    private val INVALID_PLAYER = DynamicCommandExceptionType { player -> Component.text(Configs.COMMAND_INVALID_PLAYER.get().format(player)).toMessage() }
    private val NOT_CUSTOM_CRITERIA = DynamicCommandExceptionType { teamId -> Component.text(Configs.COMMAND_NOT_CUSTOM_CRITERIA.get().format(teamId)).toMessage() }
    private val INVALID_TEAM_NAME = DynamicCommandExceptionType { teamId -> Component.text(Configs.COMMAND_INVALID_TEAM_NAME.get().format(teamId)).toMessage() }
    private val GUESS_SELF_CRITERIA = SimpleCommandExceptionType(Component.text(Configs.COMMAND_GUESS_SELF_CRITERIA.get()).toMessage())
    private val GUESS_IN_COOLDOWN = Dynamic2CommandExceptionType { player, time -> Component.text(Configs.COMMAND_GUESS_IN_COOLDOWN.get().format(player, time)).toMessage() }
}