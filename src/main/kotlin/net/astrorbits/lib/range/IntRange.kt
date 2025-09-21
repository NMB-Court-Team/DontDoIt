package net.astrorbits.lib.range

import net.astrorbits.lib.codec.Codec
import net.astrorbits.lib.codec.ObjectDecoder
import net.astrorbits.lib.codec.ObjectEncoder
import net.astrorbits.lib.codec.SizedCodec
import java.util.Random
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class IntRange(override val min: Int, override val max: Int) : Range<Int> {
    init {
        require(min <= max) { "Min value should be smaller than max value" }
    }

    override fun random(): Int {
        if (min == max) {
            return min
        }
        return random.nextInt(min, max + 1)
    }

    override fun random(random: Random?): Int {
        if (min == max) {
            return min
        }
        if (random == null) return random()
        return random.nextInt(min, max + 1)
    }

    override fun getMid() = floor(((min + max) / 2).toDouble()).toInt()

    override operator fun contains(value: Int): Boolean = value in min..max

    override fun clamp(value: Int): Int = Math.clamp(value.toLong(), min, max)

    override fun toString(): String {
        return "$min..$max"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntRange

        if (min != other.min) return false
        if (max != other.max) return false

        return true
    }

    override fun hashCode(): Int {
        var result = min
        result = 31 * result + max
        return result
    }

    companion object {
        val CODEC: Codec<IntRange> = object : SizedCodec<IntRange>(8) {
            override fun encodeSized(obj: IntRange): ByteArray = ObjectEncoder(size).putInt(obj.min).putInt(obj.max).getData()
            override fun decodeSized(data: ByteArray): IntRange = ObjectDecoder(data).let { IntRange(it.getInt(), it.getInt()) }
        }

        val INFINITY: IntRange = IntRange(Int.MIN_VALUE, Int.MAX_VALUE)

        fun of(value: Int) = IntRange(value, value)

        fun of(v1: Int, v2: Int) = IntRange(min(v1, v2), max(v1, v2))

        private val random = Random()

        fun parse(str: String): IntRange {
            val range = str.split("..").map { it.trim() }
            if (range.size != 2) throw IllegalArgumentException("Invalid range format")
            val min = if (range[0].isEmpty()) Int.MIN_VALUE else range[0].toInt()
            val max = if (range[1].isEmpty()) Int.MAX_VALUE else range[1].toInt()
            return IntRange(min, max)
        }
    }
}