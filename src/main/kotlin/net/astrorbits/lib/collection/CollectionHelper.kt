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

        // 仅保留有限且大于0的权重
        val positiveWeights = weightMap.filterValues { it.isFinite() && it > 0.0 }
        if (positiveWeights.isEmpty()) {
            throw NoSuchElementException("No positive finite weights available for selection: $weightMap")
        }

        val totalWeight = positiveWeights.values.sum()
        // 保险检查，确保传入 nextDouble 的 bound 合法
        if (!totalWeight.isFinite() || totalWeight <= 0.0) {
            throw IllegalStateException("Total weight must be finite and positive, got: $totalWeight")
        }

        var randomValue = random.nextDouble(totalWeight)

        for ((key, weight) in positiveWeights) {
            if (randomValue < weight) {
                return key
            }
            randomValue -= weight
        }
        // 理论上不会执行到这里，但不写throw就会报错，所以throw一下
        throw IllegalStateException("Unexpected error during selection")
    }
}