package net.astrorbits.lib.math

import net.astrorbits.lib.codec.Codec

enum class NumberSign(val sInt: Int, val sLong: Long, val sFloat: Float, val sDouble: Double) {
    POSITIVE(1, 1L, 1f, 1.0),
    NEGATIVE(-1, -1L, -1f, -1.0);

    fun opposite(): NumberSign {
        return when (this) {
            POSITIVE -> NEGATIVE
            NEGATIVE -> POSITIVE
        }
    }

    fun isPositive(): Boolean = this == POSITIVE
    fun isNegative(): Boolean = this == NEGATIVE

    operator fun times(value: Int): Int = value * sInt
    operator fun times(value: Long): Long = value * sLong
    operator fun times(value: Float): Float = value * sFloat
    operator fun times(value: Double): Double = value * sDouble

    fun of(value: Int): Int = value * sInt
    fun of(value: Long): Long = value * sLong
    fun of(value: Float): Float = value * sFloat
    fun of(value: Double): Double = value * sDouble

    companion object {
        val CODEC: Codec<NumberSign> = Codec.BOOL.xmap({ if (it) POSITIVE else NEGATIVE }, { it == POSITIVE })
    }
}