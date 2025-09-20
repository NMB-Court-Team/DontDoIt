package net.astrorbits.lib.text

import net.astrorbits.lib.text.LegacyTextColor.COLORS
import net.astrorbits.lib.text.LegacyTextDecoration.DECORATIONS
import net.astrorbits.lib.text.TextHelper.format
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

/**
 * 提供了一系列用于操作使用旧版文本格式（即用章节号`§`来控制文本颜色与样式）的文本的方法
 *
 * 注：为避免定制文本时引起混淆，默认文本风格会被设定为白色、无粗体、无斜体、无下划线、无中划线、无乱码
 */
object LegacyText {
    val DEFAULT_COLOR: NamedTextColor = NamedTextColor.WHITE
    val DEFAULT_DECORATIONS: Map<TextDecoration, TextDecoration.State> = mapOf(
        TextDecoration.BOLD to TextDecoration.State.FALSE,
        TextDecoration.ITALIC to TextDecoration.State.FALSE,
        TextDecoration.UNDERLINED to TextDecoration.State.FALSE,
        TextDecoration.STRIKETHROUGH to TextDecoration.State.FALSE,
        TextDecoration.OBFUSCATED to TextDecoration.State.FALSE
    )

    /**
     * 将使用旧版文本格式的文本解析为[Component]对象
     *
     * @param legacyText 使用旧版文本格式的文本
     * @return 等价的[Component]对象
     */
    fun toComponent(legacyText: String): Component {
        val components = ArrayList<Component>()
        var currentTextColor: NamedTextColor? = null
        val currentTextDecorations = HashSet<TextDecoration>()
        val currentText = StringBuilder()
        var isPreviousCharSectionSymbol = false

        for (c in legacyText.toCharArray()) {
            if (isPreviousCharSectionSymbol) {
                when (c) {
                    in COLORS -> {
                        currentTextColor = COLORS[c]
                        currentTextDecorations.clear() // 遇到颜色代码会重置样式
                    }
                    in DECORATIONS -> currentTextDecorations.add(DECORATIONS[c]!!)
                    'r' -> {
                        currentTextColor = null
                        currentTextDecorations.clear() // 遇到重置代码会把颜色和样式都重置
                    }
                }
                isPreviousCharSectionSymbol = false
            } else if (c == '§') {
                if (currentText.isNotEmpty()) {
                    components.add(createComponent(currentText.toString(), currentTextColor, currentTextDecorations))
                    currentText.setLength(0)
                }
                isPreviousCharSectionSymbol = true
            } else {
                currentText.append(c)
            }
        }
        if (currentText.isNotEmpty()) {
            components.add(createComponent(currentText.toString(), currentTextColor, currentTextDecorations))
        }
        return Component.text().append(components).build()
    }

    fun formatToComponent(legacyText: String, vararg args: Any?): Component {
        return toComponent(legacyText).format(args)
    }

    private fun createComponent(text: String, color: NamedTextColor?, textDecorations: Set<TextDecoration>): Component {
        var style = Style.empty().color(color ?: DEFAULT_COLOR)
        val textDecorationsToApply: MutableMap<TextDecoration, TextDecoration.State> = HashMap(DEFAULT_DECORATIONS)
        for (textDecoration in textDecorations) {
            if (textDecorationsToApply.containsKey(textDecoration)) {
                textDecorationsToApply[textDecoration] = TextDecoration.State.TRUE
            }
        }
        for (textDecoration in textDecorationsToApply.keys) {
            style = style.decoration(textDecoration, textDecorationsToApply[textDecoration]!!)
        }
        return Component.text(text, style)
    }

    /**
     * 将使用旧版文本格式的文本解析为`MiniMessage`字符串
     *
     * @param legacyText 使用旧版文本格式的文本
     * @return 等价的`MiniMessage`
     */
    fun toMiniMessage(legacyText: String): String {
        val miniMessageBuilder = StringBuilder()
        var currentColorSymbol: Char? = null
        val currentTextDecorationSymbols = HashSet<Char>()
        val currentText = StringBuilder()
        var isPreviousCharSectionSymbol = false

        for (c in legacyText.toCharArray()) {
            if (isPreviousCharSectionSymbol) {
                when (c) {
                    in COLORS -> {
                        currentColorSymbol = c
                        currentTextDecorationSymbols.clear() // 遇到颜色代码会重置样式
                    }
                    in DECORATIONS -> currentTextDecorationSymbols.add(c)
                    'r' -> {
                        currentColorSymbol = null
                        currentTextDecorationSymbols.clear() // 遇到重置代码会把颜色和样式都重置
                    }
                }
                isPreviousCharSectionSymbol = false
            } else if (c == '§') {
                if (currentText.isNotEmpty()) {
                    miniMessageBuilder.append(createMiniMessage(currentText.toString(), currentColorSymbol, currentTextDecorationSymbols))
                    currentText.setLength(0)
                }
                isPreviousCharSectionSymbol = true
            } else {
                currentText.append(c)
            }
        }
        if (currentText.isNotEmpty()) {
            miniMessageBuilder.append(createMiniMessage(currentText.toString(), currentColorSymbol, currentTextDecorationSymbols))
        }
        return miniMessageBuilder.toString()
    }

    fun formatToMiniMessage(legacyText: String, vararg args: Any?): String {
        return toMiniMessage(legacyText.format(*args))
    }

    private fun createMiniMessage(text: String, colorSymbol: Char?, decoSymbols: Set<Char>): String {
        val colorSymbol1 = colorSymbol ?: 'f'
        val miniMessageBuilder = StringBuilder()
        miniMessageBuilder.append("<").append(LegacyTextColor.getColorName(colorSymbol1)).append(">")
        for (decoSymbol in DECORATIONS.keys) {
            if (decoSymbol in decoSymbols) {
                miniMessageBuilder.append("<").append(LegacyTextDecoration.getDecorationName(decoSymbol)).append(">")
            } else {
                miniMessageBuilder.append("<").append(LegacyTextDecoration.getDecorationName(decoSymbol)).append(":false>")
            }
        }
        miniMessageBuilder.append(text)
        return miniMessageBuilder.toString()
    }

    private val RAW_TEXT_REPLACE_REGEX = Regex("<[^>]+>|§.")

    /**
     * 将文本中的旧版格式代码和`MiniMessage`标签全部清除
     *
     * 注：方法实际表现为，将所有成对的尖括号及其包裹的内容，以及所有`§`和它的下一个字符，全部清除
     */
    fun toRawText(text: String): String {
        return text.replace(RAW_TEXT_REPLACE_REGEX, "")
    }
}