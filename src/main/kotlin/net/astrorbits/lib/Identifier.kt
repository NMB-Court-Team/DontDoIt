package net.astrorbits.lib

import net.astrorbits.lib.codec.Codec
import net.kyori.adventure.key.Key
import org.bukkit.plugin.Plugin

/**
 * 完全照抄`net.minecraft.util.Identifier`
 */
data class Identifier private constructor(val namespace: String, val path: String) : Comparable<Identifier> {
    override fun toString(): String {
        return "$namespace:$path"
    }

    override fun compareTo(other: Identifier): Int {
        var i = path.compareTo(other.path)
        if (i == 0) {
            i = namespace.compareTo(other.namespace)
        }
        return i
    }

    fun withPath(path: String): Identifier {
        return Identifier(namespace, validatePath(namespace, path))
    }

    fun withPath(pathFunction: (String) -> String): Identifier {
        return withPath(pathFunction(path))
    }

    fun withPrefixedPath(prefix: String): Identifier {
        return withPath(prefix + path)
    }

    fun withSuffixedPath(suffix: String): Identifier {
        return withPath(path + suffix)
    }

    fun toKey(): Key {
        return Key.key(namespace, path)
    }

    companion object {
        const val VANILLA_NAMESPACE = "minecraft"

        val CODEC: Codec<Identifier> = Codec.STRING.xmap(::of, Identifier::toString)
        
        fun of(namespace: String, path: String): Identifier {
            validateNamespace(namespace, path)
            validatePath(namespace, path)
            return Identifier(namespace, path)
        }
        
        fun ofVanilla(path: String): Identifier = of(VANILLA_NAMESPACE, path)
        
        fun of(plugin: Plugin, path: String): Identifier = of(plugin.namespace(), path)

        fun of(id: String): Identifier = splitOn(id, ':')

        fun tryParse(id: String): Identifier? = trySplitOn(id, ':')

        fun tryParse(namespace: String, path: String): Identifier? {
            return if (isNamespaceValid(namespace) && isPathValid(path)) Identifier(namespace, path) else null
        }

        fun splitOn(id: String, delimiter: Char): Identifier {
            val i = id.indexOf(delimiter)
            if (i >= 0) {
                val path = id.substring(i + 1)
                if (i != 0) {
                    val namespace = id.substring(0, i)
                    return of(namespace, path)
                } else {
                    return ofVanilla(path)
                }
            } else {
                return ofVanilla(id)
            }
        }

        fun trySplitOn(id: String, delimiter: Char): Identifier? {
            val i = id.indexOf(delimiter)
            if (i >= 0) {
                val path = id.substring(i + 1)
                if (!isPathValid(path)) {
                    return null
                } else if (i != 0) {
                    val namespace = id.substring(0, i)
                    return if (isNamespaceValid(namespace)) Identifier(namespace, path) else null
                } else {
                    return Identifier(VANILLA_NAMESPACE, path)
                }
            } else {
                return if (isPathValid(id)) Identifier(VANILLA_NAMESPACE, id) else null
            }
        }

        fun isCharValid(c: Char): Boolean {
            return c in '0'..'9' || c in 'a'..'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-'
        }

        fun isPathValid(path: String): Boolean {
            for (element in path) {
                if (!isPathCharValid(element)) {
                    return false
                }
            }
            return true
        }

        fun isNamespaceValid(namespace: String): Boolean {
            for (element in namespace) {
                if (!isNamespaceCharValid(element)) {
                    return false
                }
            }
            return true
        }

        private fun validateNamespace(namespace: String, path: String): String {
            if (!isNamespaceValid(namespace)) {
                throw IllegalArgumentException("Non [a-z0-9_.-] character in namespace of location: $namespace:$path")
            } else {
                return namespace
            }
        }

        fun isPathCharValid(c: Char): Boolean {
            return c == '_' || c == '-' || c in 'a'..'z' || c in '0'..'9' || c == '/' || c == '.'
        }

        private fun isNamespaceCharValid(c: Char): Boolean {
            return c == '_' || c == '-' || c in 'a'..'z' || c in '0'..'9' || c == '.'
        }

        private fun validatePath(namespace: String, path: String): String {
            if (!isPathValid(path)) {
                throw IllegalArgumentException("Non [a-z0-9/._-] character in path of location: $namespace:$path")
            } else {
                return path
            }
        }
    }
}