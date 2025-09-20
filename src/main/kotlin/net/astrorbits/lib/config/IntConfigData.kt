package net.astrorbits.lib.config

import net.astrorbits.lib.range.IntRange

class IntConfigData(key: String, value: Int, defaultValue: Int, val range: IntRange = IntRange.INFINITY) : ConfigData<Int>(key, value, defaultValue) {
    constructor(key: String, defaultValue: Int, range: IntRange = IntRange.INFINITY) : this(key, defaultValue, defaultValue, range)

    override fun getFromConfig(config: Config): Int? {
        val value = config.getInt(key) ?: return null
        if (value !in range) return null
        return value
    }
}