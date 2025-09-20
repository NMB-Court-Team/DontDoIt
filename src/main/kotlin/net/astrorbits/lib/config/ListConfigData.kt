package net.astrorbits.lib.config

open class ListConfigData<T>(key: String, value: List<T>, defaultValue: List<T>, val parser: (Any?) -> T) : ConfigData<List<T>>(key, value, defaultValue) {
    constructor(key: String, defaultValue: List<T>, parser: (Any?) -> T) : this(key, defaultValue, defaultValue, parser)

    override fun getFromConfig(config: Config): List<T>? {
        return config.getList(key)?.map(parser)
    }
}