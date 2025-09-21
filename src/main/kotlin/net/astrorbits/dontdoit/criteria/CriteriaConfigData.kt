package net.astrorbits.dontdoit.criteria

import net.astrorbits.lib.config.Config
import net.astrorbits.lib.config.ConfigData

class CriteriaConfigData(
    key: String,
    value: Map<String, List<Map<String, String>>>,
    defaultValue: Map<String, List<Map<String, String>>>
) : ConfigData<Map<String, List<Map<String, String>>>>(key, value, defaultValue) {
    constructor(key: String, defaultValue: Map<String, List<Map<String, String>>>) : this(key, defaultValue, defaultValue)

    override fun getFromConfig(config: Config): Map<String, List<Map<String, String>>>? {
        val yamlConfig = config.yamlConfig
        val allCriteria = yamlConfig.getConfigurationSection(key) ?: return null
        val result = HashMap<String, List<Map<String, String>>>()
        for (type in CriteriaManager.allCriteriaTypes.keys) {
            val criteria = allCriteria.getMapList(type)
            result[type] = criteria.map { e -> e.mapKeys { it.toString() }.mapValues { it.toString() } }
        }
        return result
    }
}