package net.astrorbits.lib.config

abstract class ConfigData<T>(val key: String, value: T, val defaultValue: T) {
    var value: T = value
        private set
    var config: Config? = null

    fun get(): T = value

    fun update() {
        if (config == null) return
        value = getFromConfig(config!!) ?: defaultValue
    }

    protected abstract fun getFromConfig(config: Config): T?
}