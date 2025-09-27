package net.astrorbits.dontdoit.criteria.inspect

import net.astrorbits.dontdoit.criteria.helper.TriggerDifficulty
import kotlin.math.ln

/**
 * 实现该接口的准则可以参与仓检
 */
interface InventoryInspectable {
    fun modifyWeight(weight: Double, context: InventoryInspectContext): Double

    companion object {
        const val CRITERIA_DUPLICATED_WITH_SELF_MULTIPLIER = 0.5
        const val CRITERIA_DUPLICATED_WITH_OTHER_MULTIPLIER = 0.8

        // 这个函数里有一些莫名其妙的常数，这是我构造这个函数的过程中凑出来的数字
        fun calcLifePercentageMultiplier(lifePercentage: Double, difficulty: TriggerDifficulty): Double {
            val diff = difficulty.difficulty
            return -0.00565 * (diff - 4) * (diff - 5) * (diff - 4.5) * ln(lifePercentage + 0.5) + 1
            // 公式的参数化版本：（D为diff，L为lifePercentage)
            // f(D) = a*(D-4)(D-5)(D-b)*life(L)+1
            // a=-0.00565, b=4.5
            // life(L) = ln(L+0.5)
            // 这里a*(D-4)(D-5)(D-b)一部分是一个三次函数，4,5,b是用来凑零点的，a是用来控制三次函数的增长速度的
            // ln(L+0.5)是凑出来的，只是为了满足life(0.5)=0而已，使用自然对数函数是试出来的
        }
    }
}