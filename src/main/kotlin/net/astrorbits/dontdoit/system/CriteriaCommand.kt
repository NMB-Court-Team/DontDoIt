package net.astrorbits.dontdoit.system

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.dontdoit.team.TeamManager
import net.astrorbits.lib.command.CommandHelper
import net.astrorbits.lib.text.TextHelper.toMessage
import net.kyori.adventure.text.Component
import java.util.concurrent.CompletableFuture

object CriteriaCommand {
    const val COMMAND_NAME = "criteria"

    fun register(registrar: Commands) {
        val builder = Commands.literal(COMMAND_NAME)
        builder.then(Commands.literal("trigger")
            .then(Commands.argument("team", TeamIdArgumentType())
                .executes { ctx ->
                    val team = ctx.getArgument("team", TeamData::class.java)
                    if (!GameStateManager.isRunning() || team.isDead || !team.isInUse || team.criteria?.type != CriteriaType.USER_DEFINED) return@executes 0
                    team.criteria!!.trigger(team)
                    return@executes 1
                }
            )
        ).then(Commands.literal("define")
            .then(Commands.argument("team", TeamIdArgumentType())
                .then(Commands.argument("name", StringArgumentType.greedyString())
                    .executes { ctx ->
                        if (GameStateManager.isRunning()) throw DEFINE_CRITERIA_WHEN_RUNNING.create()
                        val team = ctx.getArgument("team", TeamData::class.java)
                        val name = StringArgumentType.getString(ctx, "name")
                        //TODO
                        return@executes 1
                    }
                )
            )
        )
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

    private val INVALID_TEAM_NAME = DynamicCommandExceptionType { name -> Component.text("无效的队伍名称：$name").toMessage() }
    private val DEFINE_CRITERIA_WHEN_RUNNING = SimpleCommandExceptionType(Component.text("不可在游戏进行时修改自定义词条").toMessage())
}