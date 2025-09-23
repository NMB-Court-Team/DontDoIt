package net.astrorbits.dontdoit.system.generate

import net.astrorbits.lib.math.vector.Vec3i
import net.astrorbits.lib.range.IntRange
import net.astrorbits.lib.range.Range
import kotlin.math.floor

class GenerationEntry(val depth: IntRange, val ratio: Double) : Range<Int> by depth {
    fun selectAndRemove(totalCount: Int, positions: MutableSet<Vec3i>): Set<Vec3i> {
        if (positions.isEmpty()) return emptySet()
        if (positions.size == 1) {
            val result = setOf(positions.first())
            positions.clear()
            return result
        }
        val count = floor(totalCount * ratio).toInt().coerceIn(1, positions.size)
        val selectedPositions = positions.shuffled().take(count).toSet()
        positions.removeAll(selectedPositions)
        return selectedPositions
    }

    companion object {
        const val DEPTH_KEY = "depth"
        const val RATIO_KEY = "ratio"

        fun fromMap(data: Map<String, String>): GenerationEntry {
            val depthString = data[DEPTH_KEY] ?: throw IllegalArgumentException("Missing key: $DEPTH_KEY")
            val ratioString = data[RATIO_KEY] ?: throw IllegalArgumentException("Missing key: $RATIO_KEY")
            val depth = try {
                IntRange.parse(depthString)
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid depth value: $depthString")
            }
            val ratio = try {
                ratioString.toDouble()
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid ratio value: $ratioString")
            }
            return GenerationEntry(depth, ratio)
        }
    }
}