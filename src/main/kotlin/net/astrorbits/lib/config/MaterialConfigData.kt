package net.astrorbits.lib.config

import org.bukkit.Material

class MaterialConfigData(key: String, value: Material, defaultValue: Material) : EnumConfigData<Material>(key, value, defaultValue) {
    constructor(key: String, defaultValue: Material) : this(key, defaultValue, defaultValue)

    override fun getFromConfig(config: Config): Material? {
        val name = config.getString(key) ?: return null
        return Material.matchMaterial(name)
    }
}