package net.astrorbits.lib.config

class DoubleListConfigData(key: String, value: List<Double>, defaultValue: List<Double>) : ConfigData<List<Double>>(key, value, defaultValue) {
    constructor(key: String, defaultValue: List<Double>) : this(key, defaultValue, defaultValue)

    override fun getFromConfig(config: Config): List<Double>? {
        return config.getDoubleList(key)
    }
}