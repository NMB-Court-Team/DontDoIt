package net.astrorbits.lib.config

open class StringListConfigData(key: String, value: List<String>, defaultValue: List<String>) : ConfigData<List<String>>(key, value, defaultValue) {
    constructor(key: String, defaultValue: List<String>) : this(key, defaultValue, defaultValue)

    override fun getFromConfig(config: Config): List<String>? {
        return config.getStringList(key)
    }
}