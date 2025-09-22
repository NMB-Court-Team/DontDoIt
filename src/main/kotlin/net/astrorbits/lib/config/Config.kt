package net.astrorbits.lib.config

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.nio.file.Path

/**
 * 通用的YAML配置类
 * @param configName 配置名称
 * @param configPath 配置文件的路径，需要带有.yml后缀
 * @param defaultConfigResourcePath 内置在resources文件夹的默认配置文件路径，当[configPath]指定的配置文件不存在时，会将该路径的文件复制到[configPath]
 * @param logger 日志记录器
 */
class Config(
    val configName: String,
    val configPath: Path,
    val defaultConfigResourcePath: String,
    private val logger: Logger = DEFAULT_LOGGER
): Closeable {
    private lateinit var configFile: File
    lateinit var yamlConfig: FileConfiguration

    private val configs: MutableList<ConfigData<*>> = mutableListOf()

    /**
     * 定义配置项
     */
    fun <C : ConfigData<T>, T> defineConfig(configData: C): C {
        configs.add(configData)
        configData.config = this
        return configData
    }

    /**
     * 加载配置文件，需在插件的`onEnable`内调用
     */
    fun load() {
        loadFromFile()
        loadToLocal()
    }

    /**
     * 关闭配置文件，需在插件的`onDisable`内调用
     */
    override fun close() {
        for (data in configs) {
            data.config = null
        }
        configs.clear()
    }

    private fun loadFromFile() {
        configFile = configPath.toFile()

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

    private fun getDefaultConfigInputStream(): InputStream = javaClass.classLoader.getResourceAsStream(defaultConfigResourcePath) ?: throw IllegalArgumentException("Cannot find resource $defaultConfigResourcePath")

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

    fun getMap(key: String): Map<String, Any?>? {
        val section = yamlConfig.getConfigurationSection(key) ?: return null
        return section.getValues(false)
    }

    fun getFlattenedMap(key: String): Map<String, Any?>? {
        val section = yamlConfig.getConfigurationSection(key) ?: return null
        return section.getValues(true)
    }

    /**
     * 主动更新配置文件的数据
     */
    fun save() {
        yamlConfig.save(configFile)
    }

    companion object {
        private val DEFAULT_LOGGER: Logger = LoggerFactory.getLogger("config")
    }
}