package net.astrorbits.util.config

class ParserConfigData<T>(key: String, value: T, defaultValue: T, val parser: (String) -> T) : ConfigData<T>(key, value, defaultValue) {
    constructor(key: String, defaultValue: T, parser: (String) -> T) : this(key, defaultValue, defaultValue, parser)

    override fun getFromConfig(config: Config): T? {
        val data = config.getString(key) ?: return null
        return runCatching { parser(data) }.getOrNull()
    }

    companion object {
        fun <T> placeholderParser(): (String) -> T { throw IllegalStateException("Should not invoke placeholder parser") }
    }
}