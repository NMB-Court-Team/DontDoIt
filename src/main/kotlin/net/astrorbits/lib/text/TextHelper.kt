package net.astrorbits.lib.text

import com.google.gson.JsonElement
import com.mojang.brigadier.Message
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.dialog.Dialog
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickCallback.Options
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import java.net.URL
import java.util.IllegalFormatException
import java.util.UUID

typealias ComponentHelper = TextHelper

/**
 * 提供了一系列实用的文本组件处理方法
 */
object TextHelper {
    /**
     * 将文本组件转化为命令使用的[Message]对象
     */
    fun Component.toMessage(): Message = MessageComponentSerializer.message().serialize(this)

    fun Location.formatToLegacyText(): String {
        return "§3[§b%s§3] §3(§a%.2f§f, §a%.2f§f, §a%.2f§3) §3(§d%.2f° §f/ §d%.2f°§3)".format(
            world.name, x, y, z, yaw, pitch
        )
    }

    private val PLACEHOLDER_PATTERN = Regex("\\{\\{|}}|\\{([^{}:]+)(:([^{}]+))?}")

    /**
     * 把文本组件像Python的`f-string`那样格式化，会将字符串中的`{placeholder_name}`替换为[formatArgs]中对应`placeholder_name`键的参数
     *
     * 占位符后也可以加上`:%xxx`，例如`{placeholder_name:%xxx}`，此时会先将参数按照`%xxx`给出的格式化占位符，调用[String.format]进行格式化，再替换进去
     *
     * 与Python的`f-string`的格式化规则相同，`{{`是`{`的转义，`}}`是`}`的转义
     *
     * 如果某个参数对应的格式化占位符是`%s`，且参数类型是[Component]，则会进行特殊处理，具体为：
     *
     * 会把[Component]内部的文本和样式填入，而不是调用`.toString`后填入字符串
     *
     * 示例：
     * ``` kotlin
     * val player: Player = Bukkit.getPlayer("Miccebe")
     * val score: Float = 15.63125f
     * val text = Component.text("Player {player} reached score {score:%.2f}!")
     *     .yellow().bold()
     *     .format(mapOf(
     *         "player" to player.name,
     *         "score" to score
     *     ))
     * // text内容：Player Miccebe reached score 15.63!
     * ```
     *
     * @param formatArgs 格式化参数
     * @throws IllegalFormatException 格式化占位符的格式不正确时抛出，规则与[String.format]相同
     * @see String.format
     */
    fun Component.format(formatArgs: Map<String, Any?>): Component {  // 实现原理跟另一个format一样，就不写注释了
        if (formatArgs.isEmpty()) return this

        return recursiveProcessComponent(this) process@{ component ->
            if (component is TextComponent) {
                val content = component.content()
                val builder = SimpleTextBuilder()

                val matchResults = PLACEHOLDER_PATTERN.findAll(content)
                var prevEndIndex = 0
                for (matchResult in matchResults) {
                    if (matchResult.range.first > prevEndIndex) {
                        builder.append(component.content(content.substring(prevEndIndex, matchResult.range.first)))
                    }

                    val matchContent = matchResult.value
                    if (matchContent == "{{") {
                        builder.append(component.content("{"))
                    } else if (matchContent == "}}") {
                        builder.append(component.content("}"))
                    } else {
                        val key = matchResult.groups[1]?.value
                        val format = matchResult.groups[3]?.value ?: "%s"
                        if (key != null && key in formatArgs) {
                            val replacement = formatArgs[key]
                            if (replacement is Component && format.matches(STRING_PATTERN)) {
                                builder.append(replacement)
                            } else if (replacement != null) {
                                builder.append(component.content(format.format(replacement)))
                            } else {
                                builder.append(component.content(matchResult.value))
                            }
                        } else {
                            builder.append(component.content(matchResult.value))
                        }
                    }

                    prevEndIndex = matchResult.range.last + 1
                }

                // 追加最后剩余的文本
                if (prevEndIndex < content.length) {
                    builder.append(component.content(content.substring(prevEndIndex)))
                }

                return@process builder.build()
            } else {
                return@process component
            }
        }
    }

    private val FORMAT_STRING_PATTERN = Regex("%(\\d+\\$)?([-#+ 0,(<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])")
    private val ORDINAL_PATTERN = Regex("(\\d+)\\$")
    private val STRING_PATTERN = Regex("^%(\\d+\\$)?s$")

    /**
     * 把文本组件像[String.format]那样格式化，可以使用诸如`%.2f`, `%2$s`这种稍微复杂的格式化占位符。
     *
     * 与[String.format]的格式化规则相同，`%%`是`%`的转义。
     *
     * 如果某个参数对应的格式化占位符是`%s`或者它的带序数的版本，且参数类型是[Component]，则会进行特殊处理，具体为：
     *
     * 会把[Component]内部的文本和样式填入，而不是调用`.toString`后填入字符串。
     *
     * @param args 格式化参数
     * @throws IllegalFormatException 格式化占位符的格式不正确时抛出，规则与[String.format]相同
     * @see String.format
     */
    fun Component.format(vararg args: Any?): Component {
        if (args.isEmpty()) return this
        var i = 0
        return recursiveProcessComponent(this) process@{ component ->
            if (component is TextComponent) {
                val content = component.content()
                val builder = SimpleTextBuilder()

                val matchResults = FORMAT_STRING_PATTERN.findAll(content)  // 匹配 %xxx 形式的格式化占位符
                var prevEndIndex = 0  // 上一个匹配项结尾的index
                for (matchResult in matchResults) {
                    // 把在这之前的子串用原样式添加进去
                    if (matchResult.range.first > prevEndIndex) {
                        builder.append(component.content(content.substring(prevEndIndex, matchResult.range.first)))
                    }

                    val matchContent = matchResult.value
                    val ordinalMatchResult = ORDINAL_PATTERN.find(matchContent)  // 寻找序数("%n$xxx"中的"n$")
                    if (matchContent == "%%") {  // 匹配项内容是转义的"%"
                        builder.append(component.content("%"))
                    } else if (ordinalMatchResult != null) {  // 处理带序数的参数
                        val ordinal = ordinalMatchResult.groupValues[1].toInt() - 1
                        if (ordinal in args.indices) {
                            builder.append(replaceArg(component, matchContent, args[ordinal]))
                        } else {
                            builder.append(component.content(matchContent))
                        }
                    } else if (i in args.indices) {  // 处理不带序数的参数
                        builder.append(replaceArg(component, matchContent, args[i]))
                        i += 1
                    } else {  // 所有情况都不符合，这个参数相当于没什么用
                        builder.append(component.content(matchContent))
                    }
                    prevEndIndex = matchResult.range.last + 1   // 更新上一个匹配项结尾的index
                }
                builder.append(component.content(content.substring(prevEndIndex)))
                return@process builder.build()
            } else {
                return@process component
            }
        }
    }

    private fun replaceArg(originalComponent: TextComponent, content: String, arg: Any?): TextComponent {
        return if (arg is Component && content.matches(STRING_PATTERN)) {
            originalComponent.content("").append(arg)
        } else {
            originalComponent.content(content.replace(ORDINAL_PATTERN, "").format(arg))
        }
    }

    /**
     * 递归处理文本组件
     * @param component 原文本组件
     * @param process 处理文本组件的函数，输入的文本组件的[Component.children]一定为空
     * @return 处理后的文本组件
     */
    fun recursiveProcessComponent(component: Component, process: (Component) -> (Component)): Component {
        fun recursiveProcess(builder: SimpleTextBuilder, component: Component, process: (Component) -> Component) {
            builder.append(process(component.children(emptyList())))
            for (child in component.children()) {
                recursiveProcess(builder, child, process)
            }
        }
        val builder = SimpleTextBuilder()
        recursiveProcess(builder, component, process)
        return builder.build()
    }

    fun recursiveCheckComponent(component: Component, check: (Component) -> Unit) {
        check(component)
        for (child in component.children()) {
            if (child.children().isNotEmpty()) {
                recursiveCheckComponent(child, check)
            }
        }
    }

    fun parseMiniMessage(message: String): Component {
        return MiniMessage.miniMessage().deserialize(message)
    }

    fun Collection<Component>.join(delimiter: Component): Component {
        val builder = SimpleTextBuilder()
        for ((index, component) in this.withIndex()) {
            builder.append(component)
            if (index != size - 1) {
                builder.append(delimiter)
            }
        }
        return builder.build()
    }

    fun Collection<Component>.join(delimiter: String): Component {
        val componentDelimiter = Component.text(delimiter)
        return join(componentDelimiter)
    }

    fun Component.isEmpty(): Boolean {
        var isEmpty = true
        recursiveCheckComponent(this) {
            if (it is TextComponent && it.content().isNotEmpty()) {
                isEmpty = false
            }
        }
        return isEmpty
    }

    fun Component.toJson(): JsonElement {
        return GsonComponentSerializer.gson().serializeToTree(this)
    }

    fun Component.toJsonString(): String {
        return GsonComponentSerializer.gson().serialize(this)
    }

    fun toComponent(json: JsonElement): Component {
        return GsonComponentSerializer.gson().deserializeFromTree(json)
    }

    fun toComponent(jsonString: String): Component {
        return GsonComponentSerializer.gson().deserialize(jsonString)
    }

    fun Component.appendNewline(count: Int): Component {
        return this.append("\n".repeat(count))
    }

    fun Component.appendSpace(count: Int): Component {
        return this.append(" ".repeat(count))
    }

    fun Component.append(number: Number?): Component {
        return this.append(Component.text(number.toString()))
    }

    fun Component.append(bool: Boolean?): Component {
        return this.append(Component.text(bool.toString()))
    }

    fun Component.append(c: Char?): Component {
        return this.append(Component.text(c.toString()))
    }

    fun Component.append(array: Array<*>?): Component {
        return this.append(Component.text(array.contentToString()))
    }

    fun Component.append(text: String?): Component {
        return this.append(Component.text(text.toString()))
    }

    fun Component.appendMiniMessage(miniMessage: String): Component {
        return this.append(MiniMessage.miniMessage().deserialize(miniMessage))
    }

    fun Component.appendLegacy(legacyText: String): Component {
        return this.append(LegacyText.toComponent(legacyText))
    }

    fun Component.color(color: Int?): Component {
        if (color == null) return this.color(null)
        return this.color(TextColor.color(color))
    }

    fun Component.black(): Component {
        return this.color(NamedTextColor.BLACK)
    }

    fun Component.darkBlue(): Component {
        return this.color(NamedTextColor.DARK_BLUE)
    }

    fun Component.darkGreen(): Component {
        return this.color(NamedTextColor.DARK_GREEN)
    }

    fun Component.darkAqua(): Component {
        return this.color(NamedTextColor.DARK_AQUA)
    }

    fun Component.darkRed(): Component {
        return this.color(NamedTextColor.DARK_RED)
    }

    fun Component.darkPurple(): Component {
        return this.color(NamedTextColor.DARK_PURPLE)
    }

    fun Component.gold(): Component {
        return this.color(NamedTextColor.GOLD)
    }

    fun Component.gray(): Component {
        return this.color(NamedTextColor.GRAY)
    }

    fun Component.darkGray(): Component {
        return this.color(NamedTextColor.DARK_GRAY)
    }

    fun Component.blue(): Component {
        return this.color(NamedTextColor.BLUE)
    }

    fun Component.green(): Component {
        return this.color(NamedTextColor.GREEN)
    }

    fun Component.aqua(): Component {
        return this.color(NamedTextColor.AQUA)
    }

    fun Component.red(): Component {
        return this.color(NamedTextColor.RED)
    }

    fun Component.lightPurple(): Component {
        return this.color(NamedTextColor.LIGHT_PURPLE)
    }

    fun Component.yellow(): Component {
        return this.color(NamedTextColor.YELLOW)
    }

    fun Component.white(): Component {
        return this.color(NamedTextColor.WHITE)
    }

    fun Component.resetColor(): Component {
        return this.style { it.color(null) }
    }

    fun Component.bold(apply: Boolean = true): Component {
        return this.style { it.decoration(TextDecoration.BOLD, apply) }
    }

    fun Component.resetBold(): Component {
        return this.style { it.decoration(TextDecoration.BOLD, TextDecoration.State.NOT_SET) }
    }

    fun Component.italic(apply: Boolean = true): Component {
        return this.style { it.decoration(TextDecoration.ITALIC, apply) }
    }

    fun Component.resetItalic(): Component {
        return this.style { it.decoration(TextDecoration.ITALIC, TextDecoration.State.NOT_SET) }
    }

    fun Component.underlined(apply: Boolean = true): Component {
        return this.style { it.decoration(TextDecoration.UNDERLINED, apply) }
    }

    fun Component.resetUnderlined(): Component {
        return this.style { it.decoration(TextDecoration.UNDERLINED, TextDecoration.State.NOT_SET) }
    }

    fun Component.strikethrough(apply: Boolean = true): Component {
        return this.style { it.decoration(TextDecoration.STRIKETHROUGH, apply) }
    }

    fun Component.resetStrikethrough(): Component {
        return this.style { it.decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.NOT_SET) }
    }

    fun Component.obfuscated(apply: Boolean = true): Component {
        return this.style { it.decoration(TextDecoration.OBFUSCATED, apply) }
    }

    fun Component.resetObfuscated(): Component {
        return this.style { it.decoration(TextDecoration.OBFUSCATED, TextDecoration.State.NOT_SET) }
    }

    fun Component.resetDecoration(): Component {
        return this.style {
            it.decoration(TextDecoration.BOLD, TextDecoration.State.NOT_SET)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.NOT_SET)
                .decoration(TextDecoration.UNDERLINED, TextDecoration.State.NOT_SET)
                .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.NOT_SET)
                .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.NOT_SET)
        }
    }

    fun Component.shadowColor(color: TextColor?): Component {
        if (color == null) return this.shadowColor(null)
        return this.shadowColor(ShadowColor.shadowColor(color, 0))
    }

    fun Component.shadowColor(color: Int?): Component {
        if (color == null) return this.shadowColor(null)
        return this.shadowColor(ShadowColor.shadowColor(color))
    }

    fun Component.resetShadowColor(): Component {
        return shadowColor(null)
    }

    fun Component.clickRunCommand(command: String): Component {
        return this.clickEvent(ClickEvent.runCommand(command))
    }

    fun Component.clickSuggestCommand(command: String): Component {
        return this.clickEvent(ClickEvent.suggestCommand(command))
    }

    fun Component.clickOpenUrl(url: String): Component {
        return this.clickEvent(ClickEvent.openUrl(url))
    }

    fun Component.clickOpenUrl(url: URL): Component {
        return this.clickEvent(ClickEvent.openUrl(url))
    }

    fun Component.clickShowDialog(dialog: Dialog): Component {
        return this.clickEvent(ClickEvent.showDialog(dialog))
    }

    fun Component.clickCopyToClipboard(content: String): Component {
        return this.clickEvent(ClickEvent.copyToClipboard(content))
    }

    fun Component.clickChangePage(page: Int): Component {
        return this.clickEvent(ClickEvent.changePage(page))
    }

    fun Component.clickCustomEvent(event: (Audience) -> Unit, options: Options): Component {
        return this.clickEvent(ClickEvent.callback(event, options))
    }

    fun Component.clickCustomEvent(event: (Audience) -> Unit): Component {
        return this.clickEvent(ClickEvent.callback(event))
    }

    fun Component.removeClick(): Component {
        return this.clickEvent(null)
    }

    fun Component.hoverText(text: String): Component {
        return this.hoverEvent(Component.text(text))
    }

    fun Component.hoverText(text: Component): Component {
        return this.hoverEvent(text)
    }

    fun Component.hoverItem(stack: ItemStack): Component {
        return this.hoverEvent(stack)
    }

    fun Component.hoverItem(stackSupplier: () -> ItemStack): Component {
        return this.hoverEvent(stackSupplier())
    }

    fun Component.hoverEntity(uuid: UUID): Component {
        return this.hoverEvent(Bukkit.getEntity(uuid))
    }

    fun Component.hoverEntity(entity: Entity): Component {
        return this.hoverEvent(entity)
    }

    fun Component.hoverEntity(entitySupplier: () -> Entity): Component {
        return this.hoverEvent(entitySupplier())
    }

    fun Component.removeHover(): Component {
        return this.hoverEvent(null)
    }
}