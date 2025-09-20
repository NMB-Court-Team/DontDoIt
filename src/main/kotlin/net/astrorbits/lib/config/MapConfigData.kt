package net.astrorbits.lib.config

/**
 * [Map]对象的配置项
 *
 * @param flattened 是否是扁平的`Map`
 */
open class MapConfigData<V>(
    key: String,
    value: Map<String, V>,
    defaultValue: Map<String, V>,
    val flattened: Boolean,
    val parser: (Any?) -> V
) : ConfigData<Map<String, V>>(key, value, defaultValue) {
    constructor(key: String, defaultValue: Map<String, V>, flattened: Boolean, parser: (Any?) -> V) : this(key, defaultValue, defaultValue, flattened, parser)

    override fun getFromConfig(config: Config): Map<String, V>? {
        val map = if (flattened) config.getFlattenedMap(key) else config.getMap(key)
        return map?.mapValues { (_, value) -> parser(value) }
    }
}