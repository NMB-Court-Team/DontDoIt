package net.astrorbits.lib.config

class ParserMapConfigData<K, V>(
    key: String,
    value: Map<K, V>,
    defaultValue: Map<K, V>,
    val keyParser: (String) -> K,
    val valueParser: (String) -> V
) : ConfigData<Map<K, V>>(key, value, defaultValue) {
    constructor(key: String, defaultValue: Map<K, V>, keyParser: (String) -> K, valueParser: (String) -> V) : this(key, defaultValue, defaultValue, keyParser, valueParser)

    override fun getFromConfig(config: Config): Map<K, V>? {
        val map = config.getMap(key)?.mapValues { it.value.toString() } ?: return null
        return map.mapKeys { keyParser(it.key) }.mapValues { valueParser(it.value) }
    }
}