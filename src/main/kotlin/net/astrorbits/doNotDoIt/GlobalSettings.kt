package net.astrorbits.doNotDoIt

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.astrorbits.doNotDoIt.system.DiamondBehavior
import net.astrorbits.lib.text.TextHelper.toMessage
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

object GlobalSettings {
    private const val COMMAND_NAME = "settings"
    private var isDialogOpen: Boolean = false
    private val DIALOG_IS_OPEN = SimpleCommandExceptionType(Component.text("配置界面被其他人占用，无法打开").toMessage())

    fun init(plugin: JavaPlugin) {
        val node = Commands.literal(COMMAND_NAME).requires { it.sender is Player && it.sender.isOp }
            .executes { ctx ->
                if (isDialogOpen) throw DIALOG_IS_OPEN.create()
                val player = ctx.source.sender as Player
                player.showDialog(createDialog())
                isDialogOpen = true
                return@executes 1
            }.build()
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS.newHandler { event ->
            val registrar = event.registrar()
            registrar.register(node)
        })
    }

    var lifeCount: Int = Configs.DEFAULT_LIFE_COUNT.get()
    var diamondBehavior: DiamondBehavior = DiamondBehavior.REDUCE_OTHERS_LIFE

    fun createDialog(): Dialog {
        TODO()
    }
}