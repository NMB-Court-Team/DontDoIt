package net.astrorbits.lib.codec

import org.bukkit.plugin.java.JavaPlugin
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

object ByteFileHelper {
    fun getConfigPath(plugin: JavaPlugin, relativePath: String) = plugin.dataPath.resolve(relativePath).toString()

    fun saveToFile(path: String, data: ByteArray) {
        val directoryPath = Paths.get(path).parent
        if (directoryPath != null && !Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath)
        }

        try {
            FileOutputStream(path).use { os ->
                os.write(data)
            }
        } catch (e: IOException) {
            throw IOException("Failed to write data to file at $path: ", e)
        }
    }

    fun <T> saveToFile(path: String, obj: T, codec: Codec<T>) {
        saveToFile(path, codec.encode(obj))
    }

    fun saveToFile(plugin: JavaPlugin, relativePath: String, data: ByteArray) {
        saveToFile(getConfigPath(plugin, relativePath), data)
    }

    fun <T> saveToFile(plugin: JavaPlugin, relativePath: String, obj: T, codec: Codec<T>) {
        saveToFile(getConfigPath(plugin, relativePath), obj, codec)
    }

    fun readFromFile(path: String): ByteArray {
        val path1 = Paths.get(path)

        if (!Files.exists(path1)) {
            throw FileNotFoundException("Cannot find file at '$path'")
        }

        try {
            return Files.readAllBytes(path1)
        } catch (e: IOException) {
            throw IOException("Failed to read data from file at '$path':", e)
        }
    }

    fun <T> readFromFile(path: String, codec: Codec<T>): T {
        return codec.decode(readFromFile(path))
    }

    fun readFromFile(plugin: JavaPlugin, relativePath: String): ByteArray {
        return readFromFile(getConfigPath(plugin, relativePath))
    }

    fun <T> readFromFile(plugin: JavaPlugin, relativePath: String, codec: Codec<T>): T {
        return readFromFile(getConfigPath(plugin, relativePath), codec)
    }
}