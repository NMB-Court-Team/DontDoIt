package net.astrorbits.dontdoit.criteria.helper

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.lib.math.Duration
import net.astrorbits.lib.math.Rotation
import net.astrorbits.lib.math.vector.Vec3d
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

sealed class MoveType {
    /**
     * 每刻调用
     */
    abstract fun isMoving(player: Player): Boolean

    data object Move : MoveType() {
        val prevTickPos: MutableMap<UUID, Vec3d> = mutableMapOf()

        override fun isMoving(player: Player): Boolean {
            val prevPos = prevTickPos[player.uniqueId]
            val currentPos = Vec3d.fromLocation(player.location)
            prevTickPos[player.uniqueId] = currentPos
            if (prevPos == null) {
                return false
            }
            return !prevPos.fuzzyEqual(currentPos, EPSILON_DOUBLE)
        }
    }

    data object Sprint : MoveType() {
        override fun isMoving(player: Player): Boolean {
            return player.isSprinting
        }
    }

    data object Sneak : MoveType() {
        override fun isMoving(player: Player): Boolean {
            return player.isSneaking
        }
    }

    data object Swim : MoveType() {
        override fun isMoving(player: Player): Boolean {
            return player.isSwimming
        }
    }

    data object Glide : MoveType() {
        override fun isMoving(player: Player): Boolean {
            return player.isGliding
        }
    }

    data object Jump : MoveType(), Listener {
        val isJustJumped: MutableMap<UUID, Boolean> = mutableMapOf()

        override fun isMoving(player: Player): Boolean {
            if (isJustJumped.computeIfAbsent(player.uniqueId) { false }) {
                isJustJumped[player.uniqueId] = false
                return true
            }
            return false
        }

        @EventHandler
        fun onPlayerJump(event: PlayerJumpEvent) {
            val uuid = event.player.uniqueId
            isJustJumped[uuid] = true
            TaskBuilder(DontDoIt.instance, TaskType.Delayed(Duration.ticks(2.0)))
                .setTask { isJustJumped[uuid] = false }
                .runTask()
        }
    }

    data object Fall : MoveType() {
        override fun isMoving(player: Player): Boolean {
            return player.fallDistance > 0
        }
    }

    data object OnAir : MoveType() {
        override fun isMoving(player: Player): Boolean {
            return !player.isOnGround
        }
    }

    data object Rotate : MoveType() {
        val prevTickRot: MutableMap<UUID, Rotation> = mutableMapOf()

        override fun isMoving(player: Player): Boolean {
            val prevRot = prevTickRot[player.uniqueId]
            val currentRot = Rotation.fromLocation(player.location)
            prevTickRot[player.uniqueId] = currentRot
            if (prevRot == null) {
                return false
            }
            return !prevRot.fuzzyEqual(currentRot, EPSILON_FLOAT)
        }
    }

    private object GarbageCleaner : Listener {
        @EventHandler
        fun onPlayerQuit(event: PlayerQuitEvent) {
            val uuid = event.player.uniqueId
            Move.prevTickPos.remove(uuid)
            Jump.isJustJumped.remove(uuid)
            Rotate.prevTickRot.remove(uuid)
        }
    }

    companion object {
        private const val EPSILON_DOUBLE = 1e-3
        private const val EPSILON_FLOAT = 1e-2f

        fun registerListener(plugin: JavaPlugin) {
            val pluginManager = plugin.server.pluginManager
            pluginManager.registerEvents(Jump, plugin)
            pluginManager.registerEvents(GarbageCleaner, plugin)
        }

        fun valueOf(name: String): MoveType {
            return when (name.lowercase()) {
                "move" -> Move
                "sprint" -> Sprint
                "sneak" -> Sneak
                "swim" -> Swim
                "glide" -> Glide
                "jump" -> Jump
                "fall" -> Fall
                "on_air" -> OnAir
                "rotate" -> Rotate
                else -> throw IllegalArgumentException("Unknown move type: $name")
            }
        }
    }
}