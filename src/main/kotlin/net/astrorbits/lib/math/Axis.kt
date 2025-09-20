package net.astrorbits.lib.math

import net.astrorbits.lib.codec.Codec
import net.astrorbits.lib.math.vector.Vec3d
import net.astrorbits.lib.math.vector.Vec3i

enum class Axis(val positiveUnitVec: Vec3i, val negativeUnitVec: Vec3i) {
    X(Vec3i.x(1), Vec3i.x(-1)),
    Y(Vec3i.y(1), Vec3i.y(-1)),
    Z(Vec3i.z(1), Vec3i.z(-1));

    fun unitVec(sign: NumberSign): Vec3i {
        return when (sign) {
            NumberSign.POSITIVE -> positiveUnitVec
            NumberSign.NEGATIVE -> negativeUnitVec
        }
    }

    fun vec(sign: NumberSign, length: Double): Vec3d = unitVec(sign).toVec3d() * length

    companion object {
        val CODEC: Codec<Axis> = Codec.ofEnum(Axis::class.java)
    }
}