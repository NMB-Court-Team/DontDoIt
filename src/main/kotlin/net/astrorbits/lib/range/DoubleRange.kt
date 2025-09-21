package net.astrorbits.lib.range

import net.astrorbits.lib.codec.Codec
import net.astrorbits.lib.codec.ObjectDecoder
import net.astrorbits.lib.codec.ObjectEncoder
import net.astrorbits.lib.codec.SizedCodec
import java.util.*
import kotlin.math.max
import kotlin.math.min

class DoubleRange(override val min: Double, override val max: Double) : Range<Double> {
    init {
        require(min <= max) { "Min value should be smaller than max value" }
    }

    override fun random(): Double {
        if (min == max) {
            return min
        }
        return random.nextDouble(min, max)
    }

    override fun random(random: Random?): Double {
        if (min == max) {
            return min
        }
        if (random == null) return random()
        return random.nextDouble(min, max)
    }

    override fun getMid() = (min + max) / 2

    override operator fun contains(value: Double): Boolean = value in min..max

    override fun clamp(value: Double): Double = Math.clamp(value, min, max)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoubleRange) return false

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
        val CODEC: Codec<DoubleRange> = object : SizedCodec<DoubleRange>(16) {
            override fun encodeSized(obj: DoubleRange): ByteArray = ObjectEncoder(size).putDouble(obj.min).putDouble(obj.max).getData()
            override fun decodeSized(data: ByteArray): DoubleRange = ObjectDecoder(data).let { DoubleRange(it.getDouble(), it.getDouble()) }
        }

        val INFINITY: DoubleRange = DoubleRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)

        fun of(value: Double) = DoubleRange(value, value)

        fun of(v1: Double, v2: Double) = DoubleRange(min(v1, v2), max(v1, v2))

        private val random = Random()

        fun parse(str: String): DoubleRange {
            val range = str.split("..").map { it.trim() }
            if (range.size != 2) throw IllegalArgumentException("Invalid range format")
            val min = if (range[0].isEmpty()) Double.NEGATIVE_INFINITY else range[0].toDouble()
            val max = if (range[1].isEmpty()) Double.POSITIVE_INFINITY else range[1].toDouble()
            return DoubleRange(min, max)
        }
    }
}