package net.astrorbits.lib.math.vector

import io.papermc.paper.math.Position
import net.astrorbits.lib.codec.ObjectDecoder
import net.astrorbits.lib.codec.ObjectEncoder
import net.astrorbits.lib.codec.SizedCodec
import net.astrorbits.lib.math.Axis
import net.astrorbits.lib.math.Rotation
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.util.Vector
import org.joml.Vector3L
import org.joml.Vector3d
import org.joml.Vector3f
import org.joml.Vector3i
import kotlin.math.sqrt
import kotlin.math.abs

class Vec3i(val x: Int, val y: Int, val z: Int) {
    fun add(other: Vec3i): Vec3i = Vec3i(x + other.x, y + other.y, z + other.z)
    fun add(x: Int, y: Int, z: Int): Vec3i = Vec3i(this.x + x, this.y + y, this.z + z)
    operator fun plus(other: Vec3i): Vec3i = add(other)

    fun subtract(other: Vec3i): Vec3i = Vec3i(x - other.x, y - other.y, z - other.z)
    fun subtract(x: Int, y: Int, z: Int): Vec3i = Vec3i(this.x - x, this.y - y, this.z - z)
    operator fun minus(other: Vec3i): Vec3i = subtract(other)

    fun multiply(scale: Int): Vec3i = if (scale == 0) ZERO else Vec3i(x * scale, y * scale, z * scale)
    operator fun times(scale: Int): Vec3i = multiply(scale)
    operator fun div(scale: Int): Vec3i = multiply(1 / scale)

    fun invert(): Vec3i = Vec3i(-x, -y, -z)
    operator fun unaryPlus(): Vec3i = this
    operator fun unaryMinus(): Vec3i = invert()

    operator fun get(axis: Axis): Int = when (axis) { Axis.X -> x; Axis.Y -> y; Axis.Z -> z }

    operator fun component1(): Int = x
    operator fun component2(): Int = y
    operator fun component3(): Int = z

    operator fun rangeTo(other: Vec3i): BlockBox = BlockBox(this, other)
    operator fun rangeUntil(other: Vec3i): BlockBox = BlockBox(this, other.subtract(xyz(1)))

    infix fun dot(other: Vec3i): Int = x * other.x + y * other.y + z * other.z

    infix fun cross(other: Vec3i): Vec3i = Vec3i(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun offset(offset: Vec3i) = add(offset)
    fun offset(x: Int, y: Int, z: Int): Vec3i = Vec3i(this.x + x, this.y + y, this.z + z)

    fun length(): Double = sqrt((x * x + y * y + z * z).toDouble())
    fun lengthSquared(): Int = x * x + y * y + z * z

    fun distanceTo(other: Vec3i): Double = (this - other).length()
    fun squaredDistanceTo(other: Vec3i): Int = (this - other).lengthSquared()
    fun chebyshevDistanceTo(other: Vec3i): Int = maxOf(abs(x - other.x), abs(y - other.y), abs(z - other.z))
    fun manhattanDistanceTo(other: Vec3i): Int = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)

    fun isZero(): Boolean = x == 0 && y == 0 && z == 0

    fun isUnit(): Boolean = x == 1 || y == 1 || z == 1

    fun maxComponent(): Int = maxOf(x, y, z)
    fun minComponent(): Int = minOf(x, y, z)
    fun maxComponentAxis(): Axis = when {
        x >= y && x >= z -> Axis.X
        y >= z -> Axis.Y
        else -> Axis.Z
    }
    fun minComponentAxis(): Axis = when {
        x <= y && x <= z -> Axis.X
        y <= z -> Axis.Y
        else -> Axis.Z
    }

    fun toVec3d(): Vec3d = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
    fun center(): Vec3d = Vec3d(x + 0.5, y + 0.5, z + 0.5)
    fun floorCenter(): Vec3d = Vec3d(x + 0.5, y.toDouble(), z + 0.5)

    fun setX(newX: Int): Vec3i = Vec3i(newX, y, z)
    fun setY(newY: Int): Vec3i = Vec3i(x, newY, z)
    fun setZ(newZ: Int): Vec3i = Vec3i(x, y, newZ)

    fun modifyX(modifier: (Int) -> Int): Vec3i = Vec3i(modifier(x), y, z)
    fun modifyY(modifier: (Int) -> Int): Vec3i = Vec3i(x, modifier(y), z)
    fun modifyZ(modifier: (Int) -> Int): Vec3i = Vec3i(x, y, modifier(z))

    fun toArray(): IntArray = intArrayOf(x, y, z)
    fun toList(): List<Int> = listOf(x, y, z)

    fun toLocation(world: World? = null, yaw: Float = 0f, pitch: Float = 0f): Location = Location(world, x.toDouble(), y.toDouble(), z.toDouble(), yaw, pitch)
    fun toLocation(world: World? = null, rot: Rotation): Location = Location(world, x.toDouble(), y.toDouble(), z.toDouble(), rot.yaw, rot.pitch)
    fun toLocation(yaw: Float, pitch: Float): Location = Location(null, x.toDouble(), y.toDouble(), z.toDouble(), yaw, pitch)
    fun toLocation(rot: Rotation): Location = Location(null, x.toDouble(), y.toDouble(), z.toDouble(), rot.yaw, rot.pitch)

    fun toVector(): Vector = Vector(x, y, z)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return when (other) {
            is Vec3i -> x == other.x && y == other.y && z == other.z
            is Vec3d -> x.toDouble() == other.x && y.toDouble() == other.y && z.toDouble() == other.z
            is Vector -> x.toDouble() == other.x && y.toDouble() == other.y && z.toDouble() == other.z
            is Position -> x.toDouble() == other.x() && y.toDouble() == other.y() && z.toDouble() == other.z()
            is Location -> x.toDouble() == other.x && y.toDouble() == other.y && z.toDouble() == other.z
            is Vector3f -> x.toFloat() == other.x && y.toFloat() == other.y && z.toFloat() == other.z
            is Vector3d -> x.toDouble() == other.x && y.toDouble() == other.y && z.toDouble() == other.z
            is Vector3i -> x == other.x && y == other.y && z == other.z
            is Vector3L -> x.toLong() == other.x && y.toLong() == other.y && z.toLong() == other.z
            else -> false
        }
    }

    override fun hashCode(): Int = (x.hashCode() * 31 + y.hashCode()) * 31 + z.hashCode()

    override fun toString(): String = "Vec3i($x, $y, $z)"

    fun toShortString(): String = "$x, $y, $z"

    companion object {
        val CODEC: SizedCodec<Vec3i> = object : SizedCodec<Vec3i>(12) {
            override fun encodeSized(obj: Vec3i): ByteArray = ObjectEncoder(size).putInt(obj.x).putInt(obj.y).putInt(obj.z).getData()

            override fun decodeSized(data: ByteArray): Vec3i = ObjectDecoder(data).let { Vec3i(it.getInt(), it.getInt(), it.getInt()) }
        }

        fun fromString(str: String): Vec3i {
            val coords = str.split(",").map { it.trim().toInt() }
            return Vec3i(coords[0], coords[1], coords[2])
        }

        val ZERO: Vec3i = Vec3i(0, 0, 0)
        val ONE = Vec3i(1, 1, 1)
        val UNIT_X = Vec3i(1, 0, 0)
        val UNIT_Y = Vec3i(0, 1, 0)
        val UNIT_Z = Vec3i(0, 0, 1)
        val NEGATIVE_UNIT_X = Vec3i(-1, 0, 0)
        val NEGATIVE_UNIT_Y = Vec3i(0, -1, 0)
        val NEGATIVE_UNIT_Z = Vec3i(0, 0, -1)

        fun fromArray(array: IntArray): Vec3i = Vec3i(
            array.getOrElse(0) { 0 },
            array.getOrElse(1) { 0 },
            array.getOrElse(2) { 0 }
        )

        fun x(x: Int): Vec3i = Vec3i(x, 0, 0)
        fun y(y: Int): Vec3i = Vec3i(0, y, 0)
        fun z(z: Int): Vec3i = Vec3i(0, 0, z)
        fun ofPositive(axis: Axis, length: Int): Vec3i = axis.positiveUnitVec * length
        fun ofNegative(axis: Axis, length: Int): Vec3i = axis.negativeUnitVec * length
        fun of(axis: Axis, length: Int): Vec3i = ofPositive(axis, length)
        fun xy(value: Int): Vec3i = Vec3i(value, value, 0)
        fun xz(value: Int): Vec3i = Vec3i(value, 0, value)
        fun yz(value: Int): Vec3i = Vec3i(0, value, value)
        fun xyz(value: Int): Vec3i = Vec3i(value, value, value)

        fun World.getBlock(pos: Vec3i): Block {
            return this.getBlockAt(pos.x, pos.y, pos.z)
        }
    }
}