package net.astrorbits.lib.math

import net.astrorbits.lib.codec.Codec
import net.astrorbits.lib.math.vector.Vec3d
import net.astrorbits.lib.math.vector.Vec3i

enum class Direction(val axis: Axis, val sign: NumberSign, val horizontalRotateCount: Int) {
    UP(Axis.Y, NumberSign.POSITIVE, -1),
    DOWN(Axis.Y, NumberSign.NEGATIVE, -1),
    EAST(Axis.X, NumberSign.POSITIVE, 1),
    WEST(Axis.X, NumberSign.NEGATIVE, 3),
    SOUTH(Axis.Z, NumberSign.POSITIVE, 2),
    NORTH(Axis.Z, NumberSign.NEGATIVE, 0);

    val unitVec: Vec3i = axis.unitVec(sign)

    fun vec(length: Double): Vec3d = axis.vec(sign, length)

    fun rotateYClockwise(): Direction {
        return when (this) {
            UP, DOWN -> this
            EAST -> SOUTH
            WEST -> NORTH
            SOUTH -> WEST
            NORTH -> EAST
        }
    }

    fun rotateYClockwise(count: Int): Direction {
        return when ((count % 4 + 4) % 4) {
            0 -> this
            1 -> rotateYClockwise()
            2 -> rotateY180()
            3 -> rotateYCounterclockwise()
            else -> throw AssertionError("Unexpected rotate count")
        }
    }

    fun rotateYCounterclockwise(): Direction {
        return when (this) {
            UP, DOWN -> this
            EAST -> NORTH
            WEST -> SOUTH
            SOUTH -> EAST
            NORTH -> WEST
        }
    }

    fun rotateY180(): Direction {
        return when (this) {
            UP, DOWN -> this
            else -> opposite()
        }
    }

    fun opposite(): Direction {
        return when (this) {
            UP -> DOWN
            DOWN -> UP
            EAST -> WEST
            WEST -> EAST
            SOUTH -> NORTH
            NORTH -> SOUTH
        }
    }

    fun isHorizontal(): Boolean {
        return this == EAST || this == WEST || this == SOUTH || this == NORTH
    }

    fun isVertical(): Boolean {
        return this == UP || this == DOWN
    }

    fun isPositive(): Boolean = this.sign.isPositive()
    fun isNegative(): Boolean = this.sign.isNegative()

    fun isOf(axis: Axis): Boolean = this.axis == axis

    companion object {
        val CODEC: Codec<Direction> = Codec.ofEnum(Direction::class.java)

        val HORIZONTAL: Set<Direction> = setOf(EAST, WEST, SOUTH, NORTH)
        val VERTICAL: Set<Direction> = setOf(UP, DOWN)
    }
}