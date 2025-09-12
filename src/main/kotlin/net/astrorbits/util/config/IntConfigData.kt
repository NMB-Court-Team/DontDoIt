package net.astrorbits.util.config

class IntConfigData(key: String, value: Int, defaultValue: Int) : ConfigData<Int>(key, value, defaultValue) {
    constructor(key: String, defaultValue: Int) : this(key, defaultValue, defaultValue)

    override fun getFromConfig(config: Config): Int? {
        return config.getInt(key)
    }
}