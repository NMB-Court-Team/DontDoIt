package net.astrorbits.util.config

class DoubleConfigData(key: String, value: Double, defaultValue: Double) : ConfigData<Double>(key, value, defaultValue) {
    constructor(key: String, defaultValue: Double) : this(key, defaultValue, defaultValue)

    override fun getFromConfig(config: Config): Double? {
        return config.getDouble(key)
    }
}