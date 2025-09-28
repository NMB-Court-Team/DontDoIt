package net.astrorbits.dontdoit.system

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
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
import net.astrorbits.dontdoit.criteria.system.CriteriaManager
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.dontdoit.system.team.TeamData.Companion.CRITERIA_DISPLAY_NAME_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamData.Companion.LIFE_COUNT_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamData.Companion.TEAM_NAME_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamManager
import net.astrorbits.lib.command.CommandHelper
import net.astrorbits.lib.text.TextHelper.format
import net.astrorbits.lib.text.TextHelper.toMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

object CriteriaCommand {
    const val COMMAND_NAME = "criteria"

    fun register(registrar: Commands) {
        val argumentBuilder = Commands.literal(COMMAND_NAME)
        argumentBuilder.then(Commands.literal("trigger")
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
        ).then(Commands.literal("trigger-forced")
            .then(Commands.argument("team", TeamIdArgumentType())
                .requires { it.sender.isOp }
                .executes { ctx ->
                    if (!GameStateManager.isRunning()) throw GAME_NOT_START.create()
                    val team = ctx.getArgument("team", TeamData::class.java)
                    if (team == null || team.isEliminated) throw INVALID_TEAM_NAME.create(team.teamId)
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
                        val senderTeam = TeamManager.getTeam(ctx.source.sender as Player)
                        val team = TeamManager.getTeam(player)
                        if (team == null || team.isEliminated) throw INVALID_PLAYER.create(player.name)
                        if (senderTeam == null || player in senderTeam) throw GUESS_SELF_CRITERIA.create()
                        val guessed = BoolArgumentType.getBool(ctx, "guessed")
                        val cooldown = TeamManager.guess(player, team, guessed)
                        if (cooldown != null) {
                            throw GUESS_IN_COOLDOWN.create(player.name, cooldown)
                        }

                        return@executes 1
                    }
                )
            )
        ).then(Commands.literal("change")
            .then(Commands.argument("team", TeamIdArgumentType())
                .requires { it.sender.isOp }
                .executes { ctx ->
                    if (!GameStateManager.isRunning()) throw GAME_NOT_START.create()
                    val team = ctx.getArgument("team", TeamData::class.java)
                    if (team == null || team.isEliminated) throw INVALID_TEAM_NAME.create(team.teamId)

                    val oldCriteria = team.criteria
                    val unbindResult = team.criteria?.onUnbind(team, CriteriaChangeReason.MANUAL)
                    if (unbindResult != false) {
                        team.criteria = CriteriaManager.getRandomCriteria(team, oldCriteria)
                        team.criteria!!.onBind(team, CriteriaChangeReason.MANUAL)

                        if (oldCriteria != null) {
                            Bukkit.broadcast(Configs.AUTO_CHANGE_CRITERIA_ANNOUNCE.get().format(
                                TEAM_NAME_PLACEHOLDER to team.teamName,
                                CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria.displayText.color(team.color)
                            ))
                            team.broadcastTitle(Title.title(
                                Configs.AUTO_CHANGE_CRITERIA_TITLE.get().color(team.color),
                                Configs.AUTO_CHANGE_CRITERIA_SUBTITLE.get().format(
                                    CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria.displayText.color(team.color)
                                ),
                                5, 50, 10
                            ))
                        }
                    }
                    return@executes 1
                }
            )
        ).then(Commands.literal("grant")
            .then(Commands.argument("team", TeamIdArgumentType())
                .then(Commands.argument("criteria", StringArgumentType.greedyString())
                    .suggests { _, builder ->
                        // 自动补全所有 Criteria 的 displayName
                        CriteriaManager.allCriteria
                            .map { it.displayName }
                            .forEach { builder.suggest(it) }
                        builder.buildFuture()
                    }
                .requires { it.sender.isOp }
                .executes { ctx ->
                    if (!GameStateManager.isRunning()) throw GAME_NOT_START.create()
                    val team = ctx.getArgument("team", TeamData::class.java)
                    if (team == null || team.isEliminated) throw INVALID_TEAM_NAME.create(team.teamId)

                    val displayName = StringArgumentType.getString(ctx, "criteria")
                    val criteria = CriteriaManager.allCriteria.firstOrNull { it.displayName == displayName } ?: throw INVALID_CRITERIA_NAME.create(displayName)

                    val oldCriteria = team.criteria
                    // 替换前解绑
                    val unbindResult = team.criteria?.onUnbind(team, CriteriaChangeReason.MANUAL)
                    if (unbindResult != false) {
                        team.criteria = criteria
                        team.criteria!!.onBind(team, CriteriaChangeReason.MANUAL)

                        if (oldCriteria != null) {
                            Bukkit.broadcast(Configs.AUTO_CHANGE_CRITERIA_ANNOUNCE.get().format(
                                TEAM_NAME_PLACEHOLDER to team.teamName,
                                CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria.displayText.color(team.color)
                            ))
                            team.broadcastTitle(Title.title(
                                Configs.AUTO_CHANGE_CRITERIA_TITLE.get().color(team.color),
                                Configs.AUTO_CHANGE_CRITERIA_SUBTITLE.get().format(
                                    CRITERIA_DISPLAY_NAME_PLACEHOLDER to oldCriteria.displayText.color(team.color)
                                ),
                                5, 50, 10
                            ))
                        }
                    }
                    return@executes 1
                })
            )
        ).then(Commands.literal("reset")
            .requires { it.sender.isOp }
            .executes { _ ->
                GameStateManager.reset()
                Bukkit.broadcast(Configs.COMMAND_RESET_GAME.get())
                return@executes 1
            }
        ).then(Commands.literal("set-life")
            .then(Commands.argument("team", TeamIdArgumentType())
                .then(Commands.argument("life", IntegerArgumentType.integer(0))
                    .requires { it.sender.isOp }
                    .executes { ctx ->
                        if (!GameStateManager.isRunning()) throw GAME_NOT_START.create()
                        val team = ctx.getArgument("team", TeamData::class.java)
                        val life = IntegerArgumentType.getInteger(ctx, "life").coerceAtMost(DynamicSettings.lifeCount)
                        team.setLife(life)

                        Bukkit.broadcast(Configs.COMMAND_SET_LIFE.get().format(
                            TEAM_NAME_PLACEHOLDER to team.teamName,
                            LIFE_COUNT_PLACEHOLDER to life
                        ))

                        return@executes 1
                    }
                )
            )
        ).then(Commands.literal("get")
            .then(Commands.argument("team", TeamIdArgumentType())
                .requires { it.sender.isOp }
                .then(Commands.literal("criteria")
                    .executes { ctx ->
                        if (!GameStateManager.isRunning()) throw GAME_NOT_START.create()
                        val team = ctx.getArgument("team", TeamData::class.java)
                        if (!team.isInUse || team.isEliminated) throw INVALID_TEAM_NAME.create(team.teamId)

                        val criteria = team.criteria
                        ctx.source.sender.sendMessage(Configs.COMMAND_GET_TEAM_CRITERIA.get().format(
                            TEAM_NAME_PLACEHOLDER to team.teamName,
                            CRITERIA_DISPLAY_NAME_PLACEHOLDER to criteria?.displayText?.color(team.color)
                        ))

                        return@executes 1
                    }
                )
            )
        ).then(Commands.literal("settings")
            .requires { it.sender is Player && it.sender.isOp }
            .executes { ctx ->
                if (GameStateManager.isRunning()) throw CHANGE_SETTINGS_WHEN_NOT_WAITING.create()
                val player = ctx.source.sender as Player
                player.showDialog(DynamicSettings.createDynamicSettingsDialog())
                return@executes 1
            }
        )

        registrar.register(argumentBuilder.build())
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
    private val INVALID_CRITERIA_NAME = DynamicCommandExceptionType { criteriaName -> Component.text(Configs.COMMAND_INVALID_CRITERIA_NAME.get().format(criteriaName)).toMessage() }
    private val CHANGE_SETTINGS_WHEN_NOT_WAITING = SimpleCommandExceptionType(Component.text(Configs.COMMAND_CHANGE_SETTINGS_WHEN_NOT_WAITING.get()).toMessage())
}