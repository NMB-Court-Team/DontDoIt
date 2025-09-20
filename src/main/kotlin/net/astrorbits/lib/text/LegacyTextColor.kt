package net.astrorbits.lib.text

import com.google.common.collect.BiMap
import net.astrorbits.lib.collection.CollectionHelper.toBiMap
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import java.awt.Color

/**
 * 提供了一系列用于操作旧版颜色格式（即用章节号`§`来控制文本颜色，例如`§a`）的方法
 */
object LegacyTextColor {
    /**
     * 一个映射表，将旧版颜色格式字符映射为[NamedTextColor]对象
     */
    val COLORS: BiMap<Char, NamedTextColor> = mapOf(
        '0' to NamedTextColor.BLACK,
        '1' to NamedTextColor.DARK_BLUE,
        '2' to NamedTextColor.DARK_GREEN,
        '3' to NamedTextColor.DARK_AQUA,
        '4' to NamedTextColor.DARK_RED,
        '5' to NamedTextColor.DARK_PURPLE,
        '6' to NamedTextColor.GOLD,
        '7' to NamedTextColor.GRAY,
        '8' to NamedTextColor.DARK_GRAY,
        '9' to NamedTextColor.BLUE,
        'a' to NamedTextColor.GREEN,
        'b' to NamedTextColor.AQUA,
        'c' to NamedTextColor.RED,
        'd' to NamedTextColor.LIGHT_PURPLE,
        'e' to NamedTextColor.YELLOW,
        'f' to NamedTextColor.WHITE
    ).toBiMap()

    /**
     * 获得与输入的[TextColor]对象的颜色最接近的对应的[NamedTextColor]对象
     *
     * @param textColor 一个文本颜色
     * @return 颜色最接近的对应的[NamedTextColor]对象
     */
    fun getClosestNamedColor(textColor: TextColor): NamedTextColor {
        val color = Color(textColor.value())
        var closedColor = NamedTextColor.WHITE // 如果没找出来距离最近的颜色，那就使用默认值白色
        var closestDist = Int.MAX_VALUE

        for (namedTextColor in NamedTextColor.NAMES.values()) {
            val namedColor = Color(namedTextColor.value())
            val dist = calcSquaredDistance(namedColor, color)
            if (dist < closestDist) {
                closedColor = namedTextColor
                closestDist = dist
            }
        }
        return closedColor
    }

    /**
     * 计算两个颜色的平方距离
     */
    fun calcSquaredDistance(color1: Color, color2: Color): Int {
        val dAlpha = color1.alpha - color2.alpha
        val dRed = color1.red - color2.red
        val dGreen = color1.green - color2.green
        val dBlue = color1.blue - color2.blue
        return dAlpha * dAlpha + dRed * dRed + dGreen * dGreen + dBlue * dBlue
    }

    /**
     * 获得与输入的[NamedTextColor]对象对应的旧版颜色格式代码
     *
     * @return 对应的旧版颜色格式代码，例如`"§a"`
     */
    fun NamedTextColor.getSymbol(): String = "§" + COLORS.inverse()[this]

    /**
     * 获得与输入的[TextColor]对象颜色最接近的旧版颜色格式代码
     *
     * @return 颜色最接近的旧版颜色格式代码，例如`"§a"`
     */
    fun TextColor.getSymbol(): String = getClosestNamedColor(this).getSymbol()

    /**
     * 获得与输入的旧版颜色格式字符对应的[NamedTextColor]对象
     *
     * @param legacyChar 旧版颜色格式字符
     */
    fun getColor(legacyChar: Char): NamedTextColor {
        return COLORS[legacyChar] ?: throw IllegalArgumentException("Invalid legacy text color: $legacyChar")
    }

    fun getColorName(legacyChar: Char): String = getColor(legacyChar).toString()

    /**
     * 获得与输入的旧版颜色格式代码对应的[NamedTextColor]对象
     *
     * @param legacySymbol 旧版颜色格式字符，例如`"§a"`
     */
    fun getColor(legacySymbol: String): NamedTextColor {
        require(legacySymbol.startsWith("§") && legacySymbol.length == 2) { "Invalid legacy text color symbol: $legacySymbol" }
        return COLORS[legacySymbol[1]] ?: throw IllegalArgumentException("Invalid legacy text color: $legacySymbol")
    }
}