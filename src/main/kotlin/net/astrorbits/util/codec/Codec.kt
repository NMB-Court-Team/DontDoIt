package net.astrorbits.util.codec

import net.astrorbits.util.codec.builder.CodecBuilder
import net.astrorbits.util.codec.builder.CodecWithGetter
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.UUID

/**
 * 实现该接口的类可以将类型[T]的实例序列化成字节数组，或从字节数组反序列化成实例
 *
 * **注意:** 大多数情况下，你不需要专门编写一个类实现这个接口来序列化/反序列化[T]，使用类内提供的工具方法足以构建出[T]的[Codec]，除非是非常复杂的情况才需要用到
 */
interface Codec<T> {
    /**
     * 序列化对象为字节数组
     */
    fun encode(obj: T): ByteArray

    /**
     * 反序列化字节数组为对象
     */
    fun decode(data: ByteArray): T

    /**
     * 将类型[T]的[Codec]转化成类型[U]的[Codec]
     *
     * @param to 从[T]映射到[U]的方法
     * @param from 从[U]映射到[T]的方法
     */
    fun <U> xmap(to: (T) -> U, from: (U) -> T): Codec<U> {
        val codec = this
        return object : Codec<U> {
            override fun encode(obj: U): ByteArray = codec.encode(from(obj))
            override fun decode(data: ByteArray): U = to(codec.decode(data))
        }
    }

    fun <U> combine(second: Codec<U>): Codec<Pair<T, U>> {
        val first = this
        return object : Codec<Pair<T, U>> {
            override fun encode(obj: Pair<T, U>): ByteArray = ObjectEncoder().put(obj.first, first).put(obj.second, second).getData()
            override fun decode(data: ByteArray): Pair<T, U> = ObjectDecoder(data).let { it.get(first) to it.get(second) }
        }
    }

    fun <U, V> combine(second: Codec<U>, third: Codec<V>): Codec<Triple<T, U, V>> {
        val first = this
        return object : Codec<Triple<T, U, V>> {
            override fun encode(obj: Triple<T, U, V>): ByteArray = ObjectEncoder().put(obj.first, first).put(obj.second, second).put(obj.third, third).getData()
            override fun decode(data: ByteArray): Triple<T, U, V> = ObjectDecoder(data).let { Triple(it.get(first), it.get(second), it.get(third)) }
        }
    }

    fun <U> or(other: Codec<U>): Codec<Either<T, U>> {
        val self = this
        return object : Codec<Either<T, U>> {
            override fun encode(obj: Either<T, U>): ByteArray {
                val isFirst = obj.isFirst()
                val encoder = ObjectEncoder()
                encoder.putBool(isFirst)
                if (isFirst) {
                    encoder.put(obj.first(), self)
                } else {
                    encoder.put(obj.second(), other)
                }
                return encoder.getData()
            }

            override fun decode(data: ByteArray): Either<T, U> {
                val decoder = ObjectDecoder(data)
                val isFirst = decoder.getBool()
                return if (isFirst) {
                    Either.first(decoder.get(self))
                } else {
                    Either.second(decoder.get(other))
                }
            }
        }
    }

    /**
     * 用于构建复杂对象的[Codec]
     *
     * @see builder
     */
    fun <O> forGetter(getter: (O) -> T): CodecWithGetter<O, T> = CodecWithGetter(this, getter)

    /**
     * 转化为可`null`类型的[Codec]
     */
    fun toNullable(): Codec<T?> {
        val codec = this
        return object : Codec<T?> {
            override fun encode(obj: T?): ByteArray = ObjectEncoder().putNullable(obj, codec).getData()
            override fun decode(data: ByteArray): T? = ObjectDecoder(data).getNullable(codec)
        }
    }

    companion object {
        val BOOL: SizedCodec<Boolean> = object : SizedCodec<Boolean>(1) {
            override fun encodeSized(obj: Boolean): ByteArray = ObjectEncoder(size).putBool(obj).getData()
            override fun decodeSized(data: ByteArray): Boolean = ObjectDecoder(data).getBool()
        }

        val BYTE: SizedCodec<Byte> = object : SizedCodec<Byte>(1) {
            override fun encodeSized(obj: Byte): ByteArray = ObjectEncoder(size).putByte(obj).getData()
            override fun decodeSized(data: ByteArray): Byte = ObjectDecoder(data).getByte()
        }

        val SHORT: SizedCodec<Short> = object : SizedCodec<Short>(2) {
            override fun encodeSized(obj: Short): ByteArray = ObjectEncoder(size).putShort(obj).getData()
            override fun decodeSized(data: ByteArray): Short = ObjectDecoder(data).getShort()
        }

        val INT: SizedCodec<Int> = object : SizedCodec<Int>(4) {
            override fun encodeSized(obj: Int): ByteArray = ObjectEncoder(size).putInt(obj).getData()
            override fun decodeSized(data: ByteArray): Int = ObjectDecoder(data).getInt()
        }

        val LONG: SizedCodec<Long> = object : SizedCodec<Long>(8) {
            override fun encodeSized(obj: Long): ByteArray = ObjectEncoder(size).putLong(obj).getData()
            override fun decodeSized(data: ByteArray): Long = ObjectDecoder(data).getLong()
        }

        val FLOAT: SizedCodec<Float> = object : SizedCodec<Float>(4) {
            override fun encodeSized(obj: Float): ByteArray = ObjectEncoder(size).putFloat(obj).getData()
            override fun decodeSized(data: ByteArray): Float = ObjectDecoder(data).getFloat()
        }

        val DOUBLE: SizedCodec<Double> = object : SizedCodec<Double>(8) {
            override fun encodeSized(obj: Double): ByteArray = ObjectEncoder(size).putDouble(obj).getData()
            override fun decodeSized(data: ByteArray): Double = ObjectDecoder(data).getDouble()
        }

        val CHAR: SizedCodec<Char> = object : SizedCodec<Char>(2) {
            override fun encodeSized(obj: Char): ByteArray = ObjectEncoder(size).putChar(obj).getData()
            override fun decodeSized(data: ByteArray): Char = ObjectDecoder(data).getChar()
        }

        val UUID: SizedCodec<UUID> = object : SizedCodec<UUID>(16) {
            override fun encodeSized(obj: UUID): ByteArray = ObjectEncoder(size).putUuid(obj).getData()
            override fun decodeSized(data: ByteArray): UUID = ObjectDecoder(data).getUuid()
        }

        val BYTE_ARRAY: Codec<ByteArray> = object : Codec<ByteArray> {
            override fun encode(obj: ByteArray): ByteArray = ObjectEncoder().putBytes(obj).getData()
            override fun decode(data: ByteArray): ByteArray = ObjectDecoder(data).getBytes()
        }

        val STRING: Codec<String> = object : Codec<String> {
            override fun encode(obj: String): ByteArray = ObjectEncoder().putString(obj).getData()
            override fun decode(data: ByteArray): String = ObjectDecoder(data).getString()
        }

        val BLOCK_VECTOR: Codec<Vector> = object : SizedCodec<Vector>(24) {
            override fun encodeSized(obj: Vector): ByteArray = ObjectEncoder(size).putDouble(obj.x).putDouble(obj.y).putDouble(obj.z).getData()
            override fun decodeSized(data: ByteArray): Vector = ObjectDecoder(data).let { Vector(it.getDouble(), it.getDouble(), it.getDouble()) }
        }

        val ITEM_STACK: Codec<ItemStack> = object : Codec<ItemStack> {
            override fun encode(obj: ItemStack): ByteArray {
                val encoder = ObjectEncoder()
                val isItemValid = !obj.isEmpty
                encoder.putBool(isItemValid)
                if (isItemValid) {
                    encoder.putBytes(obj.serializeAsBytes())
                }
                return encoder.getData()
            }

            override fun decode(data: ByteArray): ItemStack {
                val decoder = ObjectDecoder(data)
                val isItemValid = decoder.getBool()
                return if (isItemValid) {
                    ItemStack.deserializeBytes(decoder.getBytes())
                } else {
                    ItemStack.empty()
                }
            }
        }

        val WORLD: Codec<World> = STRING.xmap({ Bukkit.getWorld(it) ?: throw InvalidDataException("World not found: $it"); }, { it.name })

        val LOCATION: Codec<Location> = builder<Location>().group(
            WORLD.toNullable().forGetter(Location::getWorld),
            DOUBLE.forGetter(Location::x),
            DOUBLE.forGetter(Location::y),
            DOUBLE.forGetter(Location::z),
            FLOAT.forGetter(Location::getYaw),
            FLOAT.forGetter(Location::getPitch)
        ).build(::Location)

        fun <T> list(codec: Codec<T>) : Codec<List<T>> {
            return object : Codec<List<T>> {
                override fun encode(obj: List<T>): ByteArray = ObjectEncoder().putList(obj, codec).getData()
                override fun decode(data: ByteArray): List<T> = ObjectDecoder(data).getList(codec)
            }
        }

        fun <K, V> map(keyCodec: Codec<K>, valueCodec: Codec<V>): Codec<Map<K, V>> {
            return object : Codec<Map<K, V>> {
                override fun encode(obj: Map<K, V>): ByteArray = ObjectEncoder().putMap(obj, keyCodec, valueCodec).getData()
                override fun decode(data: ByteArray): Map<K, V> = ObjectDecoder(data).getMap(keyCodec, valueCodec)
            }
        }

        fun <E : Enum<E>> ofEnum(enumClass: Class<E>): Codec<E> {
            return object : Codec<E> {
                override fun encode(obj: E): ByteArray = ObjectEncoder().putEnum(obj).getData()
                override fun decode(data: ByteArray): E = ObjectDecoder(data).getEnum(enumClass)
            }
        }

        fun <E : Enum<E>> ofStringifiedEnum(enumClass: Class<E>, stringifier: (E) -> String = { it.name }): Codec<E> {
            return object : Codec<E> {
                override fun encode(obj: E): ByteArray = ObjectEncoder().putString(stringifier(obj)).getData()
                override fun decode(data: ByteArray): E = enumClass.enumConstants.first { stringifier(it) == ObjectDecoder(data).getString() }
            }
        }

        /**
         * 使用方法参考[LOCATION]
         */
        fun <O> builder(): CodecBuilder<O> = CodecBuilder()
    }
}