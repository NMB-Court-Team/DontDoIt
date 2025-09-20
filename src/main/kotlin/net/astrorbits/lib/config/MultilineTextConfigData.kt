package net.astrorbits.lib.config

import net.astrorbits.lib.text.TextHelper.join
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class MultilineTextConfigData(
    key: String,
    value: List<Component>,
    defaultValue: List<Component>
) : ParserListConfigData<Component>(key, value, defaultValue, { MiniMessage.miniMessage().deserialize(it) }) {
    constructor(key: String, defaultValue: List<Component>) : this(key, defaultValue, defaultValue)
    constructor(key: String, defaultValue: Collection<String>) : this(key, parseStringList(defaultValue), parseStringList(defaultValue))

    fun getFull(): Component {
        return get().join(Component.newline())
    }

    companion object {
        private fun parseStringList(list: Collection<String>): List<Component> {
            return list.map { MiniMessage.miniMessage().deserialize(it) }
        }
    }
}