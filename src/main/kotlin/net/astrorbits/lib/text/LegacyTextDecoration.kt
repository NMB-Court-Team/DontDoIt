package net.astrorbits.lib.text

import com.google.common.collect.BiMap
import net.astrorbits.lib.collection.CollectionHelper.toBiMap
import net.kyori.adventure.text.format.TextDecoration

/**
 * 提供了一系列用于操作旧版样式格式（即用章节号`§`来控制文本样式，例如`§l`）的方法
 */
object LegacyTextDecoration {
    /**
     * 一个映射表，将旧版样式格式字符映射为[TextDecoration]对象
     */
    val DECORATIONS: BiMap<Char, TextDecoration> = mapOf(
        'n' to TextDecoration.UNDERLINED,
        'l' to TextDecoration.BOLD,
        'o' to TextDecoration.ITALIC,
        'm' to TextDecoration.STRIKETHROUGH,
        'k' to TextDecoration.OBFUSCATED
    ).toBiMap()

    /**
     * 获得与输入的[TextDecoration]对象对应的旧版样式格式代码
     *
     * @param deco 一个[TextDecoration]对象
     * @return 对应的旧版样式格式代码，例如`"§l"`
     */
    fun TextDecoration.getSymbol(): String = "§" + DECORATIONS.inverse()[this]

    /**
     * 获得与输入的旧版样式格式字符对应的[TextDecoration]对象
     *
     * @param legacyChar 旧版样式格式字符
     */
    fun getDecoration(legacyChar: Char): TextDecoration {
        return DECORATIONS[legacyChar] ?: throw IllegalArgumentException("Invalid legacy text decoration: $legacyChar")
    }

    fun getDecorationName(legacy: Char): String = getDecoration(legacy).toString()

    /**
     * 获得与输入的旧版样式格式代码对应的[TextDecoration]对象
     *
     * @param legacySymbol 旧版样式格式代码，例如`"§l"`
     */
    fun getDecoration(legacySymbol: String): TextDecoration {
        require(legacySymbol.startsWith("§") && legacySymbol.length == 2) { "Invalid legacy text decoration symbol: $legacySymbol" }
        return DECORATIONS[legacySymbol[1]] ?: throw IllegalArgumentException("Invalid legacy text decoration: $legacySymbol")
    }
}