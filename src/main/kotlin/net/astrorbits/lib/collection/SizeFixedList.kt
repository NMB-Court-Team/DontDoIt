package net.astrorbits.lib.collection

import java.util.function.Predicate

open class SizeFixedList<E>(
    final override val size: Int,
    protected val emptyElement: () -> E,
    protected val isElementEmpty: (E) -> Boolean = { false }
) : MutableList<E> {
    private val delegate: ArrayList<E> = createDelegate(size, emptyElement)

    constructor(elements: Collection<E>, emptyElement: () -> E, isElementEmpty: (E) -> Boolean) : this(elements.size, emptyElement, isElementEmpty) {
        set(elements)
    }

    override operator fun get(index: Int): E = delegate[index]
    override fun indexOf(element: E): Int = delegate.indexOf(element)
    override fun lastIndexOf(element: E): Int = delegate.lastIndexOf(element)

    override fun iterator(): MutableIterator<E> {
        val it = delegate.iterator()
        return object : MutableIterator<E> {
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): E = it.next()
            override fun remove() = throw UnsupportedOperationException("Use removeAt(index) or setEmpty(index) instead")
        }
    }

    override fun listIterator(): MutableListIterator<E> = listIterator(0)

    override fun listIterator(index: Int): MutableListIterator<E> {
        val it = delegate.listIterator(index)
        return object : MutableListIterator<E> {
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): E = it.next()
            override fun hasPrevious(): Boolean = it.hasPrevious()
            override fun previous(): E = it.previous()
            override fun nextIndex(): Int = it.nextIndex()
            override fun previousIndex(): Int = it.previousIndex()
            override fun add(element: E) = throw UnsupportedOperationException("Use add()/addAfter()/addBefore() instead")
            override fun remove() = throw UnsupportedOperationException("Use removeAt(index) or setEmpty(index) instead")
            override fun set(element: E) = it.set(element)
        }
    }

    /**
     * 将第一个空元素设置为[element]
     * @return 如果所有元素都不为空，则返回`false`
     */
    override fun add(element: E): Boolean {
        val firstEmptyIndex = getFirstEmptyIndex()
        if (firstEmptyIndex == -1) return false
        set(firstEmptyIndex, element)
        return true
    }

    @Deprecated(
        message = "方法含义不清晰",
        replaceWith = ReplaceWith("setIfEmpty"),
        level = DeprecationLevel.WARNING
    )
    override fun add(index: Int, element: E) {
        if (!this[index].isEmpty()) return
        set(index, element)
    }

    /**
     * 从第一个空元素开始逐个替换掉空元素
     * @return 如果并不是[elements]中的所有元素都替换进去了，则返回`false`
     */
    override fun addAll(elements: Collection<E>): Boolean {
        for (element in elements) {
            if (!add(element)) {
                return false
            }
        }
        return true
    }

    /**
     * 从[index]开始逐个替换掉空元素
     * @return 如果并不是[elements]中的所有元素都替换进去了，则返回`false`
     */
    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        if (elements.isEmpty()) return true
        val collection = elements.toList()
        var current = 0
        for (i in index until size) {
            val e = this[i]
            if (e.isEmpty()) {
                set(i, collection[current])
                current += 1
            }
            if (current >= collection.size) return true
        }
        return false
    }

    @Deprecated(
        message = "方法不返回添加元素是否成功",
        replaceWith = ReplaceWith("addAfter"),
        level = DeprecationLevel.WARNING
    )
    override fun addFirst(e: E) {
        add(e)
    }

    /**
     * 将[after]之后(包含)的第一个空元素替换为[element]
     * @return 如果没找到空元素则返回`false`
     */
    fun addAfter(element: E, after: Int = 0): Boolean {
        val firstEmptyIndex = getFirstEmptyIndex(after)
        if (firstEmptyIndex == -1) return false
        set(firstEmptyIndex, element)
        return true
    }

    @Deprecated(
        message = "方法不返回添加元素是否成功",
        replaceWith = ReplaceWith("addBefore"),
        level = DeprecationLevel.WARNING
    )
    override fun addLast(e: E) {
        addBefore(e)
    }

    /**
     * 将[before]之前(不包含)的空元素替换为[element]
     * @return 如果没找到空元素则返回`false`
     */
    fun addBefore(element: E, before: Int = size): Boolean {
        val lastEmptyIndex = getLastEmptyIndex(before)
        if (lastEmptyIndex == -1) return false
        set(lastEmptyIndex, element)
        return true
    }

    override operator fun set(index: Int, element: E): E = delegate.set(index, element)

    fun setIfEmpty(index: Int, element: E): Boolean {
        if (this[index].isEmpty()) {
            set(index, element)
            return true
        }
        return false
    }

    fun set(index: Int, elements: Collection<E>) {
        if (elements.isEmpty()) return
        val collection = elements.toList()
        var current = 0
        for (i in index until size) {
            set(i, collection[current])
            current += 1
            if (current >= collection.size) return
        }
    }

    fun set(elements: Collection<E>) {
        set(0, elements)
    }

    override fun contains(element: E): Boolean = delegate.contains(element)
    override fun containsAll(elements: Collection<E>): Boolean = delegate.containsAll(elements)

    protected fun E.isEmpty(): Boolean = isElementEmpty(this)
    protected fun E.isNotEmpty(): Boolean = !isEmpty()

    fun setEmpty(index: Int) = set(index, emptyElement())

    fun anyEmpty(): Boolean = any(isElementEmpty)

    fun isEmpty(index: Int): Boolean = this[index].isEmpty()

    /**
     * 获取[after]之后(包含)的第一个空元素的索引
     * @return 如果没找到空元素，则返回-1
     */
    fun getFirstEmptyIndex(after: Int = 0): Int {
        for (i in after until size) {
            if (this[i].isEmpty()) return i
        }
        return -1
    }

    fun getFirstNotEmpty(after: Int = 0): E? {
        for (i in after until size) {
            val element = this[i]
            if (element.isNotEmpty()) return element
        }
        return null
    }

    fun getFirstNotEmptyIndex(after: Int = 0): Int {
        for (i in after until size) {
            if (this[i].isNotEmpty()) return i
        }
        return -1
    }

    /**
     * 获取[before]之前(不包含)的最后一个空元素的索引
     * @return 如果没找到空元素，则返回-1
     */
    fun getLastEmptyIndex(before: Int = size): Int {
        for (i in 0 until before) {
            if (this[i].isEmpty()) return i
        }
        return -1
    }

    fun getLastNotEmpty(before: Int = size): E? {
        for (i in 0 until before) {
            val element = this[i]
            if (element.isNotEmpty()) return element
        }
        return null
    }

    fun getLastNotEmptyIndex(before: Int = size): Int {
        for (i in 0 until before) {
            if (this[i].isNotEmpty()) return i
        }
        return -1
    }

    fun getEmptyCount(): Int = count { it.isEmpty() }
    fun getNotEmptyCount(): Int = count { it.isNotEmpty() }

    /**
     * 设置所有元素为空
     */
    override fun clear() {
        for (i in indices) {
            set(i, emptyElement())
        }
    }

    /**
     * 检查是否所有元素都为空
     */
    override fun isEmpty(): Boolean = all(isElementEmpty)

    override fun reversed(): SizeFixedList<E> {
        val newList = ArrayList<E>()
        for (i in (size - 1) downTo 0) {
            newList.add(get(i))
        }
        return SizeFixedList(newList, emptyElement, isElementEmpty)
    }

    fun copy(): SizeFixedList<E> {
        return SizeFixedList(this, emptyElement, isElementEmpty)
    }

    override fun removeAt(index: Int): E {
        val element = get(index)
        set(index, emptyElement())
        return element
    }

    override fun remove(element: E): Boolean {
        for (i in indices) {
            val e = get(i)
            if (e == element) {
                set(i, emptyElement())
                return true
            }
        }
        return false
    }

    override fun removeIf(filter: Predicate<in E>): Boolean {
        var result = false
        for (i in indices) {
            val e = get(i)
            if (filter.test(e)) {
                set(i, emptyElement())
                result = true
            }
        }
        return result
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var result = false
        for (i in indices) {
            val e = get(i)
            if (e in elements) {
                set(i, emptyElement())
                result = true
            }
        }
        return result
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        var result = false
        for (i in indices) {
            val e = get(i)
            if (e !in elements) {
                set(i, emptyElement())
                result = true
            }
        }
        return result
    }

    override fun removeFirst(): E {
        val firstNotEmptyIndex = getFirstNotEmptyIndex()
        if (firstNotEmptyIndex == -1) return emptyElement()
        val element = get(firstNotEmptyIndex)
        set(firstNotEmptyIndex, emptyElement())
        return element
    }

    fun removeFirst(after: Int): E {
        val firstNotEmptyIndex = getFirstNotEmptyIndex(after)
        if (firstNotEmptyIndex == -1) return emptyElement()
        val element = get(firstNotEmptyIndex)
        set(firstNotEmptyIndex, emptyElement())
        return element
    }

    fun removeFirstOrNull(after: Int = 0): E? {
        val firstNotEmptyIndex = getFirstNotEmptyIndex(after)
        if (firstNotEmptyIndex == -1) return null
        val element = get(firstNotEmptyIndex)
        set(firstNotEmptyIndex, emptyElement())
        return element
    }

    override fun removeLast(): E {
        val lastNotEmptyIndex = getLastNotEmptyIndex()
        if (lastNotEmptyIndex == -1) return emptyElement()
        val element = get(lastNotEmptyIndex)
        set(lastNotEmptyIndex, emptyElement())
        return element
    }

    fun removeLast(before: Int = 0): E {
        val lastNotEmptyIndex = getLastNotEmptyIndex(before)
        if (lastNotEmptyIndex == -1) return emptyElement()
        val element = get(lastNotEmptyIndex)
        set(lastNotEmptyIndex, emptyElement())
        return element
    }

    fun removeLastOrNull(before: Int = 0): E? {
        val lastNotEmptyIndex = getLastNotEmptyIndex(before)
        if (lastNotEmptyIndex == -1) return null
        val element = get(lastNotEmptyIndex)
        set(lastNotEmptyIndex, emptyElement())
        return element
    }

    override fun subList(fromIndex: Int, toIndex: Int): SizeFixedList<E> {
        val subList = ArrayList<E>()
        for (i in fromIndex until toIndex) {
            subList.add(get(i))
        }
        return SizeFixedList(subList, emptyElement, isElementEmpty)
    }

    fun toArrayList(): ArrayList<E> {
        return ArrayList(this)
    }

    companion object {
        private fun <E> createDelegate(size: Int, emptyElement: () -> E): ArrayList<E> {
            val list = ArrayList<E>(size)
            repeat(size) { list.add(emptyElement()) }
            return list
        }
    }
}