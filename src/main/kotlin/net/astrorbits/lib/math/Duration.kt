package net.astrorbits.lib.math

import net.astrorbits.lib.codec.Codec
import kotlin.math.floor

/**
 * 持续时间
 *
 * 类内进行了运算符重载，可以直接与另一个[Duration]进行加减或大小比较，也可以乘以/除以一个倍率
 *
 * 要创建[Duration]对象，请使用[Companion]中的[Companion.millis], [Companion.ticks]等方法
 * @param millis 毫秒形式的时间
 */
class Duration private constructor(val millis: Int): Comparable<Duration> {
    init {
        if (millis < 0) throw IllegalArgumentException("Invalid duration: $this")
    }

    /**
     * tick形式的时间
     */
    val ticks: Double
        get() = millis / 50.0
    /**
     * 秒形式的时间
     */
    val seconds: Double
        get() = millis / 1000.0
    /**
     * 分钟形式的时间
     */
    val minutes: Double
        get() = seconds / 60.0
    /**
     * 小时形式的时间
     */
    val hours: Double
        get() = minutes / 60.0
    /**
     * Minecraft游戏日形式的时间
     */
    val mcDays: Double
        get() = minutes / 20.0
    /**
     * 现实世界的一天形式的时间
     */
    val realDays: Double
        get() = hours / 24.0

    operator fun plus(other: Duration) = Duration(millis + other.millis)
    operator fun minus(other: Duration) = Duration(millis - other.millis)
    operator fun times(scale: Double) = Duration(floor(millis * scale).toInt())
    operator fun div(scale: Double) = Duration(floor(millis / scale).toInt())

    override fun compareTo(other: Duration): Int = millis.compareTo(other.millis)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Duration) return false
        if (millis != other.millis) return false
        return true
    }

    override fun hashCode(): Int {
        return millis
    }

    override fun toString(): String {
        return "Duration($millis)"
    }

    companion object {
        val CODEC: Codec<Duration> = Codec.INT.xmap(::Duration, Duration::millis)

        /**
         * 持续时长为0的时间
         */
        val ZERO = Duration(0)
        /**
         * 持续时长为1 tick的时间
         */
        val ONE_TICK = ticks(1.0)
        /**
         * 持续时长为1秒的时间
         */
        val ONE_SECOND = seconds(1.0)

        /**
         * 以毫秒为单位创建[Duration]
         */
        fun millis(millis: Int) = Duration(millis)
        /**
         * 以tick为单位创建[Duration]
         */
        fun ticks(ticks: Double) = Duration(floor(ticks * 50.0).toInt())
        /**
         * 以秒为单位创建[Duration]
         */
        fun seconds(seconds: Double) = Duration(floor(seconds * 1000.0).toInt())
        /**
         * 以分钟为单位创建[Duration]
         */
        fun minutes(minutes: Double) = seconds(minutes * 60.0)
        /**
         * 以小时为单位创建[Duration]
         */
        fun hours(hours: Double) = minutes(hours * 60.0)
        /**
         * 以Minecraft游戏日为单位创建[Duration]
         */
        fun mcDays(days: Double) = minutes(days * 20.0)
        /**
         * 以现实世界的一天为单位创建[Duration]
         */
        fun realDays(days: Double) = hours(days * 24.0)

        /**
         * 从过去的某个时间点到现在总共持续的时间
         */
        fun tillNow(pastTime: Long) = Duration((System.currentTimeMillis() - pastTime).toInt())
        /**
         * 从现在到未来的某个时间点总共持续的时间
         */
        fun nowTo(futureTime: Long) = Duration((futureTime - System.currentTimeMillis()).toInt())
    }
}