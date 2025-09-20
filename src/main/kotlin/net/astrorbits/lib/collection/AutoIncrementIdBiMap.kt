package net.astrorbits.lib.collection

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

/**
 * claude写的
 *
 * 自动递增ID的双向映射
 *
 * ID从0开始递增，支持跳过指定ID
 */
class AutoIncrementIdBiMap<K> : BiMap<K, Int> {
    private val delegate: BiMap<K, Int> = HashBiMap.create()
    private var nextId: Int = 0

    /**
     * 添加元素并自动分配ID
     * @param key 要添加的元素
     * @return 分配的ID
     */
    fun put(key: K): Int {
        val id = getNextAvailableId()
        delegate[key] = id
        return id
    }

    fun putAll(vararg key: K) {
        for (k in key) {
            put(k)
        }
    }

    fun putAll(keys: Iterable<K>) {
        for (k in keys) {
            put(k)
        }
    }

    fun putAllSkippable(vararg keySkippable: K?) {
        for (k in keySkippable) {
            if (k == null) {
                skipId()
            } else {
                put(k)
            }
        }
    }

    fun putAllSkippable(keysSkippable: Iterable<K?>) {
        for (k in keysSkippable) {
            if (k == null) {
                skipId()
            } else {
                put(k)
            }
        }
    }

    /**
     * 跳过下一个ID
     * 调用此方法后，下一个分配的ID将跳过当前的nextId
     */
    fun skipId() {
        nextId++
    }

    /**
     * 跳过指定数量的ID
     * @param count 要跳过的ID数量
     */
    fun skipIds(count: Int) {
        require(count >= 0) { "Skip count must be non-negative" }
        nextId += count
    }

    fun getById(id: Int): K? = inverse()[id]

    /**
     * 获取下一个可用的ID
     */
    private fun getNextAvailableId(): Int {
        while (delegate.containsValue(nextId)) {
            nextId++
        }
        return nextId++
    }

    /**
     * 手动指定key和ID的映射
     * 注意：如果ID已存在，会抛出异常
     */
    override fun put(key: K, value: Int): Int? {
        val oldValue = delegate.put(key, value)
        // 更新nextId以避免重复
        if (value >= nextId) {
            nextId = value + 1
        }
        return oldValue
    }

    /**
     * 强制替换映射
     */
    override fun forcePut(key: K, value: Int): Int? {
        val oldValue = delegate.forcePut(key, value)
        // 更新nextId以避免重复
        if (value >= nextId) {
            nextId = value + 1
        }
        return oldValue
    }

    /**
     * 获取当前的下一个ID值（不消耗）
     */
    fun peekNextId(): Int = nextId

    /**
     * 重置ID计数器到指定值
     */
    fun resetIdCounter(startId: Int = 0) {
        require(startId >= 0) { "Start ID must be non-negative" }
        nextId = startId
    }

    // 委托所有BiMap接口方法到内部实例
    override val size: Int get() = delegate.size
    override fun isEmpty(): Boolean = delegate.isEmpty()
    override fun containsKey(key: K): Boolean = delegate.containsKey(key)
    override fun containsValue(value: Int): Boolean = delegate.containsValue(value)
    override fun get(key: K): Int? = delegate[key]
    override fun remove(key: K): Int? = delegate.remove(key)
    override fun putAll(from: Map<out K, Int>) {
        from.forEach { (key, value) ->
            put(key, value)
        }
    }
    override fun clear() {
        delegate.clear()
        nextId = 0
    }
    override val keys: MutableSet<K> get() = delegate.keys
    override val values: MutableSet<Int> get() = delegate.values
    override val entries: MutableSet<MutableMap.MutableEntry<K, Int>> get() = delegate.entries

    override fun inverse(): BiMap<Int, K> = delegate.inverse()

    override fun equals(other: Any?): Boolean = delegate == other
    override fun hashCode(): Int = delegate.hashCode()
    override fun toString(): String = delegate.toString()

    companion object {
        fun <T> of(vararg key: T): AutoIncrementIdBiMap<T> {
            val map = AutoIncrementIdBiMap<T>()
            map.putAll(*key)
            return map
        }

        fun <T> ofSkippable(vararg keySkippable: T?): AutoIncrementIdBiMap<T> {
            val map = AutoIncrementIdBiMap<T>()
            map.putAllSkippable(*keySkippable)
            return map
        }
    }
}