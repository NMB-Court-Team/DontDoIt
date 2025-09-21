package net.astrorbits.lib.range

import net.astrorbits.lib.codec.Codec
import net.astrorbits.lib.codec.ObjectDecoder
import net.astrorbits.lib.codec.ObjectEncoder
import net.astrorbits.lib.codec.SizedCodec
import java.util.Random
import kotlin.math.max
import kotlin.math.min

class FloatRange(override val min: Float, override val max: Float) : Range<Float> {
    init {
        require(min <= max) { "Min value should be smaller than max value" }
    }

    override fun random(): Float {
        if (min == max) {
            return min
        }
        return random.nextFloat(min, max)
    }

    override fun random(random: Random?): Float {
        if (min == max) {
            return min
        }
        if (random == null) return random()
        return random.nextFloat(min, max)
    }

    override fun getMid() = (min + max) / 2

    override operator fun contains(value: Float): Boolean = value in min..max

    override fun clamp(value: Float): Float = Math.clamp(value, min, max)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FloatRange

        if (min != other.min) return false
        if (max != other.max) return false

        return true
    }

    override fun hashCode(): Int {
        var result = min.hashCode()
        result = 31 * result + max.hashCode()
        return result
    }

    override fun toString(): String {
        return "$min..$max"
    }

    companion object {
        val CODEC: Codec<FloatRange> = object : SizedCodec<FloatRange>(8) {
            override fun encodeSized(obj: FloatRange): ByteArray = ObjectEncoder(size).putFloat(obj.min).putFloat(obj.max).getData()
            override fun decodeSized(data: ByteArray): FloatRange = ObjectDecoder(data).let { FloatRange(it.getFloat(), it.getFloat()) }
        }

        val INFINITY: FloatRange = FloatRange(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)

        fun of(value: Float) = FloatRange(value, value)

        fun of(v1: Float, v2: Float) = FloatRange(min(v1, v2), max(v1, v2))

        private val random = Random()

        fun parse(str: String): FloatRange {
            val range = str.split("..").map { it.trim() }
            if (range.size != 2) throw IllegalArgumentException("Invalid range format")
            val min = if (range[0].isEmpty()) Float.NEGATIVE_INFINITY else range[0].toFloat()
            val max = if (range[1].isEmpty()) Float.POSITIVE_INFINITY else range[1].toFloat()
            return FloatRange(min, max)
        }
    }
}