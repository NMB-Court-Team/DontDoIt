package net.astrorbits.lib.collection

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import java.util.*
import kotlin.NoSuchElementException

object CollectionHelper {
    fun <K, V> Map<K, V>.toBiMap(): BiMap<K, V> {
        return HashBiMap.create(this)
    }

    /**
     * 根据权重随机选取一个元素
     */
    fun <K> selectByDoubleWeight(weightMap: Map<K, Double>, random: Random): K {
        if (weightMap.isEmpty()) {
            throw NoSuchElementException("Weight map is empty")
        }

        val totalWeight = weightMap.values.sum()
        var randomValue = random.nextDouble(totalWeight)

        for ((key, weight) in weightMap) {
            if (randomValue < weight) {
                return key
            }
            randomValue -= weight
        }
        // 理论上不会执行到这里，但不写throw就会报错，所以throw一下
        throw IllegalStateException("Unexpected error during selection")
    }
}