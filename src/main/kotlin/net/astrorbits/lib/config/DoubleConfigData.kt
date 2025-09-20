package net.astrorbits.lib.config

import net.astrorbits.lib.range.DoubleRange

class DoubleConfigData(key: String, value: Double, defaultValue: Double, val range: DoubleRange = DoubleRange.INFINITY) : ConfigData<Double>(key, value, defaultValue) {
    constructor(key: String, defaultValue: Double, range: DoubleRange = DoubleRange.INFINITY) : this(key, defaultValue, defaultValue, range)

    override fun getFromConfig(config: Config): Double? {
        val value = config.getDouble(key) ?: return null
        if (value !in range) return null
        return value
    }
}