package net.astrorbits.lib.range

import java.util.*

/**
 * 代表一个范围，有最大值和最小值
 */
interface Range<T : Comparable<T>> {
    val min: T
    val max: T

    /**
     * 在范围中随机选取一个值
     */
    fun random(): T

    /**
     * 在范围中随机选取一个值
     */
    fun random(random: Random?): T

    /**
     * 获取范围的中心
     */
    fun getMid(): T

    /**
     * 检查一个值是否在范围内
     */
    operator fun contains(value: T): Boolean

    /**
     * 将一个值钳制在范围内
     */
    fun clamp(value: T): T

    /**
     * 获取由最小值和最大值构成的列表
     */
    fun toList(): List<T> = listOf(min, max)
}