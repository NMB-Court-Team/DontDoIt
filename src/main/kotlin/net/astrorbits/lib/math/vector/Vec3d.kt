package net.astrorbits.lib.math.vector

import io.papermc.paper.math.FinePosition
import io.papermc.paper.math.Position
import net.astrorbits.lib.codec.ObjectDecoder
import net.astrorbits.lib.codec.ObjectEncoder
import net.astrorbits.lib.codec.SizedCodec
import net.astrorbits.lib.math.Axis
import net.astrorbits.lib.math.NumberSign
import net.astrorbits.lib.math.Rotation
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import org.joml.Vector3L
import org.joml.Vector3d
import org.joml.Vector3f
import org.joml.Vector3i
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.sqrt

/**
 * 这个向量类与[org.joml]库里的向量类不同，这个类是不可变的
 *
 * 方法太多不想写文档了，功能自己看，一目了然
 */
class Vec3d(x: Double, y: Double, z: Double, epsilon: Double = EPSILON) {
    val x: Double = if (abs(x) < epsilon) 0.0 else x
    val y: Double = if (abs(y) < epsilon) 0.0 else y
    val z: Double = if (abs(z) < epsilon) 0.0 else z

    fun add(other: Vec3d): Vec3d = Vec3d(x + other.x, y + other.y, z + other.z)
    fun add(x: Double, y: Double, z: Double): Vec3d = Vec3d(this.x + x, this.y + y, this.z + z)
    operator fun plus(other: Vec3d): Vec3d = add(other)

    fun subtract(other: Vec3d): Vec3d = Vec3d(x - other.x, y - other.y, z - other.z)
    fun subtract(x: Double, y: Double, z: Double): Vec3d = Vec3d(this.x - x, this.y - y, this.z - z)
    operator fun minus(other: Vec3d): Vec3d = subtract(other)

    fun multiply(scale: Double): Vec3d = if (scale == 0.0) ZERO else Vec3d(x * scale, y * scale, z * scale)
    operator fun times(scale: Double): Vec3d = multiply(scale)
    operator fun div(scale: Double): Vec3d = multiply(1 / scale)

    fun invert(): Vec3d = Vec3d(-x, -y, -z)
    operator fun unaryPlus(): Vec3d = this
    operator fun unaryMinus(): Vec3d = invert()

    operator fun get(axis: Axis): Double = when (axis) { Axis.X -> x; Axis.Y -> y; Axis.Z -> z }

    operator fun component1(): Double = x
    operator fun component2(): Double = y
    operator fun component3(): Double = z

    operator fun rangeTo(other: Vec3d): Box = Box(this, other)

    infix fun dot(other: Vec3d): Double = x * other.x + y * other.y + z * other.z

    infix fun cross(other: Vec3d): Vec3d = Vec3d(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun offset(offset: Vec3d) = add(offset)
    fun offset(x: Double, y: Double, z: Double): Vec3d = Vec3d(this.x + x, this.y + y, this.z + z)

    fun offsetLocal(rot: Rotation, offset: Vec3d): Vec3d {
        return offset(calcLocalOffset(rot, offset))
    }

    fun length(): Double = sqrt(x * x + y * y + z * z)
    fun lengthSquared(): Double = x * x + y * y + z * z

    fun normalize(): Vec3d {
        val len = length()
        return if (len != 0.0) Vec3d(x / len, y / len, z / len) else ZERO
    }

    fun distanceTo(other: Vec3d): Double = (this - other).length()
    fun squaredDistanceTo(other: Vec3d): Double = (this - other).lengthSquared()
    fun chebyshevDistanceTo(other: Vec3d): Double = maxOf(abs(x - other.x), abs(y - other.y), abs(z - other.z))
    fun manhattanDistanceTo(other: Vec3d): Double = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)

    fun lerp(other: Vec3d, t: Double): Vec3d = Vec3d(x + (other.x - x) * t, y + (other.y - y) * t, z + (other.z - z) * t)

    fun angle(other: Vec3d): Double {
        val dot = this dot other
        val lengths = this.length() * other.length()
        return if (lengths != 0.0) acos((dot / lengths).coerceIn(-1.0, 1.0)) else 0.0
    }

    fun project(onto: Vec3d): Vec3d {
        val ontoLengthSq = onto.lengthSquared()
        return if (ontoLengthSq != 0.0) onto * (this.dot(onto) / ontoLengthSq) else ZERO
    }

    fun isZero(epsilon: Double = EPSILON): Boolean = abs(x) < epsilon && abs(y) < epsilon && abs(z) < epsilon

    fun fuzzyEqual(other: Vec3d, epsilon: Double = EPSILON): Boolean = abs(x - other.x) < epsilon && abs(y - other.y) < epsilon && abs(z - other.z) < epsilon

    fun isUnit(epsilon: Double = EPSILON): Boolean = abs(length() - 1.0) < epsilon

    fun maxComponent(): Double = maxOf(x, y, z)
    fun minComponent(): Double = minOf(x, y, z)
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

    fun floor(): Vec3i = Vec3i(floor(x).toInt(), floor(y).toInt(), floor(z).toInt())
    fun ceil(): Vec3i = Vec3i(ceil(x).toInt(), ceil(y).toInt(), ceil(z).toInt())
    fun round(): Vec3i = Vec3i(round(x).toInt(), round(y).toInt(), round(z).toInt())

    fun center(): Vec3d = Vec3d(floor(x) + 0.5, floor(y) + 0.5, floor(z) + 0.5)
    fun floorCenter(): Vec3d = Vec3d(floor(x) + 0.5, floor(y), floor(z) + 0.5)

    fun clamp(maxLength: Double): Vec3d {
        val len = length()
        return if (len > maxLength) this * (maxLength / len) else this
    }

    fun setX(newX: Double): Vec3d = Vec3d(newX, y, z)
    fun setY(newY: Double): Vec3d = Vec3d(x, newY, z)
    fun setZ(newZ: Double): Vec3d = Vec3d(x, y, newZ)

    fun modifyX(modifier: (Double) -> Double): Vec3d = Vec3d(modifier(x), y, z)
    fun modifyY(modifier: (Double) -> Double): Vec3d = Vec3d(x, modifier(y), z)
    fun modifyZ(modifier: (Double) -> Double): Vec3d = Vec3d(x, y, modifier(z))

    fun toArray(): DoubleArray = doubleArrayOf(x, y, z)
    fun toList(): List<Double> = listOf(x, y, z)

    fun toLocation(world: World? = null, yaw: Float = 0f, pitch: Float = 0f): Location = Location(world, x, y, z, yaw, pitch)
    fun toLocation(world: World? = null, rot: Rotation): Location = Location(world, x, y, z, rot.yaw, rot.pitch)
    fun toLocation(yaw: Float, pitch: Float): Location = Location(null, x, y, z, yaw, pitch)
    fun toLocation(rot: Rotation): Location = Location(null, x, y, z, rot.yaw, rot.pitch)

    fun toVector(): Vector = Vector(x, y, z)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return when (other) {
            is Vec3d -> x == other.x && y == other.y && z == other.z
            is Vec3i -> x == other.x.toDouble() && y == other.y.toDouble() && z == other.z.toDouble()
            is Vector -> x == other.x && y == other.y && z == other.z
            is Position -> x == other.x() && y == other.y() && z == other.z()
            is Location -> x == other.x && y == other.y && z == other.z
            is Vector3f -> x == other.x.toDouble() && y == other.y.toDouble() && z == other.z.toDouble()
            is Vector3d -> x == other.x && y == other.y && z == other.z
            is Vector3i -> x == other.x.toDouble() && y == other.y.toDouble() && z == other.z.toDouble()
            is Vector3L -> x == other.x.toDouble() && y == other.y.toDouble() && z == other.z.toDouble()
            else -> false
        }
    }

    override fun hashCode(): Int = (x.hashCode() * 31 + y.hashCode()) * 31 + z.hashCode()

    override fun toString(): String = "Vec3d($x, $y, $z)"

    fun toShortString(): String = "$x, $y, $z"

    companion object {
        const val EPSILON = 1e-7

        val CODEC: SizedCodec<Vec3d> = object : SizedCodec<Vec3d>(24) {
            override fun encodeSized(obj: Vec3d): ByteArray = ObjectEncoder(size).putDouble(obj.x).putDouble(obj.y).putDouble(obj.z).getData()

            override fun decodeSized(data: ByteArray): Vec3d = ObjectDecoder(data).let { Vec3d(it.getDouble(), it.getDouble(), it.getDouble()) }
        }

        fun fromString(str: String): Vec3d {
            val coords = str.split(",").map { it.trim().toDouble() }
            return Vec3d(coords[0], coords[1], coords[2])
        }

        val ZERO: Vec3d = Vec3d(0.0, 0.0, 0.0)
        val ONE = Vec3d(1.0, 1.0, 1.0)
        val UNIT_X = Vec3d(1.0, 0.0, 0.0)
        val UNIT_Y = Vec3d(0.0, 1.0, 0.0)
        val UNIT_Z = Vec3d(0.0, 0.0, 1.0)
        val NEGATIVE_UNIT_X = Vec3d(-1.0, 0.0, 0.0)
        val NEGATIVE_UNIT_Y = Vec3d(0.0, -1.0, 0.0)
        val NEGATIVE_UNIT_Z = Vec3d(0.0, 0.0, -1.0)

        fun fromArray(array: DoubleArray): Vec3d = Vec3d(
            array.getOrElse(0) { 0.0 },
            array.getOrElse(1) { 0.0 },
            array.getOrElse(2) { 0.0 }
        )

        fun x(x: Double): Vec3d = Vec3d(x, 0.0, 0.0)
        fun y(y: Double): Vec3d = Vec3d(0.0, y, 0.0)
        fun z(z: Double): Vec3d = Vec3d(0.0, 0.0, z)
        fun ofPositive(axis: Axis, length: Double): Vec3d = axis.vec(NumberSign.POSITIVE, length)
        fun ofNegative(axis: Axis, length: Double): Vec3d = axis.vec(NumberSign.NEGATIVE, length)
        fun of(axis: Axis, length: Double): Vec3d = ofPositive(axis, length)
        fun xy(value: Double): Vec3d = Vec3d(value, value, 0.0)
        fun xz(value: Double): Vec3d = Vec3d(value, 0.0, value)
        fun yz(value: Double): Vec3d = Vec3d(0.0, value, value)
        fun xyz(value: Double): Vec3d = Vec3d(value, value, value)

        fun fromVector(v: Vector): Vec3d = Vec3d(v.x, v.y, v.z)

        fun fromLocation(loc: Location): Vec3d = Vec3d(loc.x, loc.y, loc.z)
        fun fromLocationAndOffsetLocal(loc: Location, offset: Vec3d): Vec3d = fromLocation(loc).offsetLocal(Rotation.fromLocation(loc), offset)
        fun fromFinePosition(pos: FinePosition): Vec3d = Vec3d(pos.x(), pos.y(), pos.z())

        fun calcLocalOffset(rot: Rotation, offset: Vec3d): Vec3d {
            val forward = rot.toUnitVec()

            val worldUp = Vec3d(0.0, 1.0, 0.0)
            val left = worldUp.cross(rot.toHorizontalUnitVec())

            val up = forward.cross(left)

            val globalOffset = left.multiply(offset.x)
                .add(up.multiply(offset.y))
                .add(forward.multiply(offset.z))

            return globalOffset
        }
    }
}