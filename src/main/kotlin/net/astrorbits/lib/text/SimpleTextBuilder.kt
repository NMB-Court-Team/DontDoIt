package net.astrorbits.lib.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.MiniMessage

typealias SimpleComponentBuilder = SimpleTextBuilder

/**
 * 一个搭建[Component]的工具类
 *
 * 搭建全部完成后调用[build]或[toComponents]来获取搭建结果
 */
class SimpleTextBuilder(
    val root: Component = Component.empty(),
    val components: MutableList<ComponentLike> = mutableListOf()
) : MutableList<ComponentLike> by components {
    private var _cursor: Int? = null
    val cursor: Int
        get() = _cursor ?: (components.size - 1)

    fun moveCursor(offset: Int) {
        _cursor = (cursor + offset).coerceIn(0, components.size - 1)
    }

    fun getAtCursor(): ComponentLike = components[cursor]

    fun moveForward() {
        moveCursor(1)
    }

    fun moveBackward() {
        moveCursor(-1)
    }

    fun moveToHead() {
        _cursor = 0
    }

    fun moveToTail() {
        _cursor = null
    }

    fun append(number: Number?): SimpleTextBuilder = apply {
        components.add(cursor, Component.text(number.toString()))
    }

    fun append(bool: Boolean?): SimpleTextBuilder = apply {
        components.add(cursor, Component.text(bool.toString()))
    }

    fun append(c: Char?): SimpleTextBuilder = apply {
        components.add(cursor, Component.text(c.toString()))
    }

    fun append(array: Array<*>?): SimpleTextBuilder = apply {
        components.add(cursor, Component.text(array.contentToString()))
    }

    fun appendNewline(count: Int = 1): SimpleTextBuilder = apply {
        components.add(cursor, Component.text("\n".repeat(count)))
    }

    fun appendSpace(count: Int = 1): SimpleTextBuilder = apply {
        components.add(cursor, Component.text(" ".repeat(count)))
    }

    fun append(vararg components: ComponentLike): SimpleTextBuilder = apply {
        this.components.addAll(cursor, components.toList())
    }

    fun append(like: ComponentLike): SimpleTextBuilder = apply {
        components.add(cursor, like)
    }

    fun append(component: Component): SimpleTextBuilder = apply {
        components.add(cursor, component)
    }

    fun append(constructor: () -> Component): SimpleTextBuilder = apply {
        components.add(cursor, constructor())
    }

    fun append(builder: ComponentBuilder<*, *>): SimpleTextBuilder = apply {
        components.add(cursor, builder)
    }

    fun append(components: List<Component>): SimpleTextBuilder = apply {
        this.components.addAll(cursor, components)
    }

    fun append(text: String?): SimpleTextBuilder = apply {
        components.add(cursor, Component.text(text.toString()))
    }

    fun appendMiniMessage(miniMessage: String): SimpleTextBuilder = apply {
        components.add(cursor, MiniMessage.miniMessage().deserialize(miniMessage))
    }

    fun appendLegacy(legacyText: String): SimpleTextBuilder = apply {
        components.add(cursor, LegacyText.toComponent(legacyText))
    }

    fun build(): Component {
        var result = root
        for (component in components) {
            result = result.append(component)
        }
        return result
    }

    fun toComponent(): Component = build()
}