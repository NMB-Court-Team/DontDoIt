package net.astrorbits.lib.math

import net.astrorbits.lib.codec.ObjectDecoder
import net.astrorbits.lib.codec.ObjectEncoder
import net.astrorbits.lib.codec.SizedCodec
import net.astrorbits.lib.math.vector.Vec3d
import org.bukkit.Location
import kotlin.math.*

class Rotation(yaw: Float, pitch: Float, epsilon: Float = EPSILON) {
    val yaw = normalizeYaw(yaw, epsilon)
    val pitch = normalizePitch(pitch, epsilon)

    val yawRad: Float
        get() = MathHelper.toRadian(yaw)
    val pitchRad: Float
        get() = MathHelper.toRadian(pitch)

    fun toUnitVec(): Vec3d {
        return Vec3d(
            (-sin(yawRad) * cos(pitchRad)).toDouble(),
            -sin(pitchRad).toDouble(),
            (cos(yawRad) * cos(pitchRad)).toDouble()
        )
    }

    fun toHorizontalUnitVec(): Vec3d {
        return Vec3d(
            -sin(yawRad).toDouble(),
            0.0,
            cos(yawRad).toDouble()
        )
    }

    fun toVec3d(length: Double): Vec3d = toUnitVec() * length

    operator fun component1(): Float = yaw
    operator fun component2(): Float = pitch

    fun setYaw(newYaw: Float): Rotation = Rotation(newYaw, pitch)
    fun setPitch(newPitch: Float): Rotation = Rotation(yaw, newPitch)

    fun modifyYaw(modifier: (Float) -> Float): Rotation = Rotation(modifier(yaw), pitch)
    fun modifyPitch(modifier: (Float) -> Float): Rotation = Rotation(yaw, modifier(pitch))

    fun fuzzyEqual(other: Rotation, epsilon: Float = EPSILON): Boolean {
        return abs(yaw - other.yaw) < epsilon && abs(pitch - other.pitch) < epsilon
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return when (other) {
            is Rotation -> yaw == other.yaw && pitch == other.pitch
            is io.papermc.paper.math.Rotation -> yaw == other.yaw() && pitch == other.pitch()
            is Location -> yaw == other.yaw && pitch == other.pitch
            else -> false
        }
    }

    override fun hashCode(): Int = yaw.hashCode() * 31 + pitch.hashCode()

    override fun toString(): String {
        return "Rotation($yaw, $pitch)"
    }

    fun toShortString(): String = "$yaw, $pitch"

    companion object {
        private const val EPSILON: Float = 1e-5f

        fun normalizeYaw(rawYaw: Float, epsilon: Float): Float {
            val temp = rawYaw % 360f
            val angle = if (abs(temp) < epsilon) 0f else temp
            return if (angle > 180f) {
                angle - 360f
            } else {
                angle
            }
        }

        fun normalizePitch(rawPitch: Float, epsilon: Float): Float {
            return normalizeYaw(rawPitch, epsilon).coerceIn(-90f, 90f)
        }

        val ZERO = Rotation(0f, 0f)

        val CODEC: SizedCodec<Rotation> = object : SizedCodec<Rotation>(8) {
            override fun encodeSized(obj: Rotation): ByteArray = ObjectEncoder(size).putFloat(obj.yaw).putFloat(obj.pitch).getData()
            override fun decodeSized(data: ByteArray): Rotation = ObjectDecoder(data).let { Rotation(it.getFloat(), it.getFloat()) }
        }

        fun yaw(yaw: Float) = Rotation(yaw, 0f)
        fun pitch(pitch: Float) = Rotation(0f, pitch)

        fun fromVec3d(v: Vec3d): Rotation {
            return Rotation(
                MathHelper.toDegree(atan2(v.y, v.x).toFloat()),
                MathHelper.toDegree(atan2(v.z, sqrt(v.x * v.x + v.y * v.y)).toFloat())
            )
        }

        fun fromPaperRotation(rot: io.papermc.paper.math.Rotation): Rotation = Rotation(rot.yaw(), rot.pitch())

        fun fromLocation(loc: Location): Rotation = Rotation(loc.yaw, loc.pitch)
    }
}