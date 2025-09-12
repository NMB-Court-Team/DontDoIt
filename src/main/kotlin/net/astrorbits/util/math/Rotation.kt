package net.astrorbits.util.math

import net.astrorbits.util.codec.ObjectDecoder
import net.astrorbits.util.codec.ObjectEncoder
import net.astrorbits.util.codec.SizedCodec
import org.bukkit.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Rotation(yaw: Float, pitch: Float) {
    val yaw = yaw % 360f - 180f
    val pitch = (pitch % 360f - 180f).coerceIn(-90f, 90f)

    val yawRad: Float
        get() = MathUtil.toRadian(yaw)
    val pitchRad: Float
        get() = MathUtil.toRadian(pitch)

    fun toUnitVec3d(): Vec3d {
        return Vec3d(
            (cos(pitchRad) * cos(yawRad)).toDouble(),
            (cos(pitchRad) * sin(yawRad)).toDouble(),
            sin(pitchRad).toDouble()
        )
    }

    fun toVec3d(length: Double): Vec3d = toUnitVec3d() * length

    operator fun component1(): Float = yaw
    operator fun component2(): Float = pitch

    fun setYaw(newYaw: Float): Rotation = Rotation(newYaw, pitch)
    fun setPitch(newPitch: Float): Rotation = Rotation(yaw, newPitch)

    fun modifyYaw(modifier: (Float) -> Float): Rotation = Rotation(modifier(yaw), pitch)
    fun modifyPitch(modifier: (Float) -> Float): Rotation = Rotation(yaw, modifier(pitch))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rotation) return false
        return yaw == other.yaw && pitch == other.pitch
    }

    override fun hashCode(): Int = yaw.hashCode() * 31 + pitch.hashCode()

    override fun toString(): String {
        return "Rotation($yaw, $pitch)"
    }

    fun toShortString(): String = "$yaw, $pitch"

    companion object {
        val CODEC: SizedCodec<Rotation> = object : SizedCodec<Rotation>(8) {
            override fun encodeSized(obj: Rotation): ByteArray = ObjectEncoder(size).putFloat(obj.yaw).putFloat(obj.pitch).getData()
            override fun decodeSized(data: ByteArray): Rotation = ObjectDecoder(data).let { Rotation(it.getFloat(), it.getFloat()) }
        }

        fun fromVec3d(v: Vec3d): Rotation {
            return Rotation(
                MathUtil.toDegree(atan2(v.y, v.x).toFloat()),
                MathUtil.toDegree(atan2(v.z, sqrt(v.x * v.x + v.y * v.y)).toFloat())
            )
        }

        fun fromPaperRotation(rot: io.papermc.paper.math.Rotation): Rotation = Rotation(rot.yaw(), rot.pitch())

        fun fromLocation(loc: Location): Rotation = Rotation(loc.yaw, loc.pitch)
    }
}