package net.astrorbits.util.codec

sealed class Either<A, B>(private val first: A?, private val second: B?) {
    fun isFirst(): Boolean = first != null

    fun isSecond(): Boolean = second != null

    fun first(): A = first ?: throw IllegalStateException("First object does not present")

    fun second(): B = second ?: throw IllegalStateException("Second object does not present")

    operator fun component1(): A? = first

    operator fun component2(): B? = second

    private class First<A, B>(first: A) : Either<A, B>(first, null)

    private class Second<A, B>(second: B) : Either<A, B>(null, second)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Either<A, B>) return false
        return this.first == other.first && this.second == other.second
    }

    override fun hashCode(): Int {
        return first.hashCode() * 31 + second.hashCode()
    }

    override fun toString(): String {
        return "Either($first, $second)"
    }

    companion object {
        fun <A, B> first(first: A): Either<A, B> = First(first)

        fun <A, B> second(second: B): Either<A, B> = Second(second)
    }
}