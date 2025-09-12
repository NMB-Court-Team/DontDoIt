package net.astrorbits.util.math

object MathUtil {
    const val RADIANS_PER_DEGREE: Double = Math.PI / 180.0
    const val DEGREES_PER_RADIAN: Double = 180.0 / Math.PI

    fun toRadian(degree: Double): Double = degree * RADIANS_PER_DEGREE
    fun toRadian(degree: Float): Float = degree * RADIANS_PER_DEGREE.toFloat()

    fun toDegree(radian: Double): Double = radian * DEGREES_PER_RADIAN
    fun toDegree(radian: Float): Float = radian * DEGREES_PER_RADIAN.toFloat()
}