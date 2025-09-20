package net.astrorbits.lib.collection

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

object CollectionHelper {
    fun <K, V> Map<K, V>.toBiMap(): BiMap<K, V> {
        return HashBiMap.create(this)
    }
}