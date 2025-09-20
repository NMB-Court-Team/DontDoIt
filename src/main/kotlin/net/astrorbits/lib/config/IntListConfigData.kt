package net.astrorbits.lib.config

class IntListConfigData(key: String, value: List<Int>, defaultValue: List<Int>) : ConfigData<List<Int>>(key, value, defaultValue) {
    constructor(key: String, defaultValue: List<Int>) : this(key, defaultValue, defaultValue)

    override fun getFromConfig(config: Config): List<Int>? {
        return config.getIntList(key)
    }
}