package net.astrorbits.lib.config

class StringParserMapConfigData<V>(
    key: String,
    value: Map<String, V>,
    defaultValue: Map<String, V>,
    parser: (String) -> V
) : MapConfigData<V>(
    key,
    value,
    defaultValue,
    false,
    { if (it is String) parser(it) else throw IllegalArgumentException("Value is not a string") }
) {
    constructor(key: String, defaultValue: Map<String, V>, parser: (String) -> V) : this(key, defaultValue, defaultValue, parser)
}