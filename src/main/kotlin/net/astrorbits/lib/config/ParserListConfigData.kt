package net.astrorbits.lib.config

open class ParserListConfigData<T>(key: String, value: List<T>, defaultValue: List<T>, val parser: (String) -> T) : ConfigData<List<T>>(key, value, defaultValue) {
    constructor(key: String, defaultValue: List<T>, parser: (String) -> T) : this(key, defaultValue, defaultValue, parser)

    override fun getFromConfig(config: Config): List<T>? {
        return config.getStringList(key)?.map(parser)
    }
}