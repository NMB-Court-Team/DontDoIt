package net.astrorbits.lib.config

class BoolConfigData(key: String, value: Boolean, defaultValue: Boolean) : ConfigData<Boolean>(key, value, defaultValue) {
    constructor(key: String, defaultValue: Boolean) : this(key, defaultValue, defaultValue)

    override fun getFromConfig(config: Config): Boolean? {
        return config.getBool(key)
    }
}