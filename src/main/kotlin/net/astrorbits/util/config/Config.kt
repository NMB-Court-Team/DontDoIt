package net.astrorbits.util.config

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream

class Config(
    val configName: String,
    val configPath: String,
    val defaultConfigResourcePath: String,
    private val logger: Logger = DEFAULT_LOGGER
) {
    private lateinit var configFile: File
    lateinit var yamlConfig: FileConfiguration

    private val configs: MutableList<ParserConfigData<*>> = mutableListOf()

    fun <T> addConfig(configData: ParserConfigData<T>) {
        configs.add(configData)
        configData.config = this
    }

    fun load() {
        loadFromFile()
        loadToLocal()
    }

    fun destroy() {
        for (data in configs) {
            data.config = null
        }
        configs.clear()
    }

    private fun loadFromFile() {
        configFile = File(configPath)

        if (!configFile.exists()) {
            logger.info("Config file of '$configName' not found, copying default config")
            configFile.parentFile.mkdirs()
            getDefaultConfigInputStream().use { inputStream ->
                configFile.outputStream().use { out ->
                    inputStream.copyTo(out)
                }
            }
        }

        logger.info("Loading config of '$configName'")
        yamlConfig = YamlConfiguration.loadConfiguration(configFile)
    }

    private fun loadToLocal() {
        for (config in configs) {
            config.update()
        }
    }

    private fun getDefaultConfigInputStream(): InputStream = javaClass.getResourceAsStream(defaultConfigResourcePath)!!

    operator fun contains(key: String): Boolean = yamlConfig.contains(key)

    fun getString(key: String): String? {
        return yamlConfig.getString(key)
    }

    fun getStringList(key: String): List<String>? {
        return if (contains(key)) yamlConfig.getStringList(key) else null
    }

    fun getInt(key: String): Int? {
        return if (contains(key)) yamlConfig.getInt(key) else null
    }

    fun getIntList(key: String): List<Int>? {
        return if (contains(key)) yamlConfig.getIntegerList(key) else null
    }

    fun getDouble(key: String): Double? {
        return if (contains(key)) yamlConfig.getDouble(key) else null
    }

    fun getDoubleList(key: String): List<Double>? {
        return if (contains(key)) yamlConfig.getDoubleList(key) else null
    }

    fun getList(key: String): List<Any?>? {
        return if (contains(key)) yamlConfig.getList(key) else null
    }

    fun save() {
        yamlConfig.save(configFile)
    }

    companion object {
        private val DEFAULT_LOGGER: Logger = LoggerFactory.getLogger("config")
    }
}