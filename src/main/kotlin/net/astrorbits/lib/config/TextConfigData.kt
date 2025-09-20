package net.astrorbits.lib.config

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

typealias ComponentConfigData = TextConfigData

class TextConfigData(key: String, value: Component, defaultValue: Component) : ConfigData<Component>(key, value, defaultValue) {
    constructor(key: String, defaultValue: Component) : this(key, defaultValue, defaultValue)
    constructor(key: String, miniMessage: String) : this(key, MiniMessage.miniMessage().deserialize(miniMessage), MiniMessage.miniMessage().deserialize(miniMessage))

    override fun getFromConfig(config: Config): Component? {
        val miniMessage = config.getString(key) ?: return null
        return MiniMessage.miniMessage().deserialize(miniMessage)
    }
}