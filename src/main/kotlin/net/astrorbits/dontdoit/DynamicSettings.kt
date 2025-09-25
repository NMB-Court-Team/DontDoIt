package net.astrorbits.dontdoit

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.astrorbits.dontdoit.system.DiamondBehavior
import net.astrorbits.lib.text.TextHelper.toMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Difficulty
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.floor

object DynamicSettings {
    private const val COMMAND_NAME = "settings"
    private var isDialogOpen: Boolean = false
    private val DIALOG_IS_OPEN = SimpleCommandExceptionType(Component.text("配置界面被其他人占用，无法打开").toMessage())

    fun init(plugin: JavaPlugin) {
        val node = Commands.literal(COMMAND_NAME).requires { it.sender is Player && it.sender.isOp }
            .executes { ctx ->
                if (isDialogOpen) throw DIALOG_IS_OPEN.create()
                val player = ctx.source.sender as Player
                openLifeDialog(player)
                isDialogOpen = true
                return@executes 1
            }.build()
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS.newHandler { event ->
            val registrar = event.registrar()
            registrar.register(node)
        })

    }

    var gameAreaSize: Int = Configs.GAME_AREA_SIZE.get()
    var lifeCount: Int = Configs.LIFE_COUNT.get()
    var diamondBehavior: DiamondBehavior = Configs.DIAMOND_BEHAVIOR.get()
    var diamondBehaviorEnabled: Boolean = Configs.DIAMOND_BEHAVIOR_ENABLED.get()
    var diamondBehaviorDisabledThreshold: Int = Configs.DIAMOND_BEHAVIOR_DISABLED_THRESHOLD.get()
    var allowUnbalancedTeams: Boolean = Configs.ALLOW_UNBALANCED_TEAMS.get()
    var allowGuessCriteria: Boolean = Configs.ALLOW_GUESS_CRITERIA.get()
    var guessSuccessAddLife: Int = Configs.GUESS_SUCCESS_ADD_LIFE.get()
    var guessFailedReduceLife: Int = Configs.GUESS_FAILED_REDUCE_LIFE.get()
    var ingameDifficulty: Difficulty = Configs.INGAME_DIFFICULTY.get()

    @Suppress("UnstableApiUsage")
    fun openLifeDialog(player: Player) {
        val dialog = Dialog.create { builder ->
            builder.empty()
                .base(
                    DialogBase.builder(Component.text("设置队伍生命"))
                        .inputs(
                            listOf(
                                DialogInput.numberRange(
                                    "life_count", // 输入字段 ID
                                    Component.text("队伍生命", NamedTextColor.LIGHT_PURPLE),
                                    1f, 99f // min, max
                                )
                                    .step(1f) // 步长
                                    .initial(lifeCount.toFloat()) // 初始值
                                    .labelFormat("%s: %s") // 格式化文本
                                    .width(300)
                                    .build()
                            )
                        )
                        .build()
                )
                .type(
                    DialogType.confirmation(
                        ActionButton.create(
                            Component.text("确认✔", TextColor.color(0xAEFFC1)),
                            Component.text("保存设置"),
                            100,
                            DialogAction.customClick( { context, player ->
                                val value = context.getFloat("life_count")?: 0.0
                                lifeCount = floor(value.toDouble()).toInt()
                                player.sendMessage(Component.text("队伍生命已设置为 $lifeCount"))
                                isDialogOpen = false
                            }, ClickCallback.Options.builder().build())
                        ),
                        ActionButton.create(
                            Component.text("取消❌", TextColor.color(0xFFA0B1)),
                            Component.text("放弃更改"),
                            100,
                            DialogAction.customClick( { _, _ ->
                                isDialogOpen = false
                            }, ClickCallback.Options.builder().build())
                        )
                    )
                )
        }

        player.showDialog(dialog)
    }
}