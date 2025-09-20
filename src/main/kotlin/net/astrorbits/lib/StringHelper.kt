package net.astrorbits.lib

import java.util.*

object StringHelper {
    fun String.removePrefixIgnoreCase(prefix: String): String {
        return if (this.startsWith(prefix, ignoreCase = true)) {
            this.substring(prefix.length)
        } else {
            this
        }
    }

    fun String.removeSuffixIgnoreCase(suffix: String): String {
        return if (this.endsWith(suffix, ignoreCase = true)) {
            this.substring(0, this.length - suffix.length)
        } else {
            this
        }
    }

    fun <E : Enum<E>> parseToEnum(name: String, enumClass: Class<E>): E {
        return enumClass.enumConstants.first { it.name.lowercase() == name.lowercase() }
    }

    fun String.isUuid(): Boolean {
        return runCatching { UUID.fromString(this) }.isSuccess
    }
}