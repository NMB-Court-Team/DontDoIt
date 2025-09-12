package net.astrorbits.util.config

class StringConfigData(key: String, value: String, defaultValue: String) : ConfigData<String>(key, value, defaultValue) {
    constructor(key: String, defaultValue: String) : this(key, defaultValue, defaultValue)

    override fun getFromConfig(config: Config): String? {
        return config.getString(key)
    }
}