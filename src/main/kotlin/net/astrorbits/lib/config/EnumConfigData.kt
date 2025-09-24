package net.astrorbits.lib.config

open class EnumConfigData<E : Enum<E>>(key: String, value: E, defaultValue: E) : ConfigData<E>(key, value, defaultValue) {
    constructor(key: String, defaultValue: E) : this(key, defaultValue, defaultValue)

    override fun getFromConfig(config: Config): E? {
        return defaultValue.javaClass.enumConstants.firstOrNull { config.getString(key)?.uppercase() == it.name }
    }
}