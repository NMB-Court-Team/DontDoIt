package net.astrorbits.lib.codec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.UUID

/**
 * 用来编码对象为字节数组的类，与[ObjectDecoder]搭配使用
 *
 * @param size 缓冲区长度
 * @see ObjectDecoder
 */
class ObjectEncoder(size: Int = 32) {
    private val out: ByteArrayOutputStream = ByteArrayOutputStream(size)

    fun putBool(bl: Boolean): ObjectEncoder = apply { out.writeBytes(byteArrayOf(if (bl) 1 else 0)) }
    fun putByte(b: Byte): ObjectEncoder = apply { out.writeBytes(byteArrayOf(b)) }
    fun putShort(s: Short): ObjectEncoder = apply { out.writeBytes(ByteBuffer.allocate(2).putShort(s).array()) }
    fun putInt(i: Int): ObjectEncoder = apply { out.writeBytes(ByteBuffer.allocate(4).putInt(i).array()) }
    fun putLong(l: Long): ObjectEncoder = apply { out.writeBytes(ByteBuffer.allocate(8).putLong(l).array()) }
    fun putFloat(f: Float): ObjectEncoder = apply { out.writeBytes(ByteBuffer.allocate(4).putFloat(f).array()) }
    fun putDouble(d: Double): ObjectEncoder = apply { out.writeBytes(ByteBuffer.allocate(8).putDouble(d).array()) }
    fun putChar(c: Char): ObjectEncoder = apply { putShort(c.code.toShort()) }

    fun putBytes(byteArr: ByteArray): ObjectEncoder = apply {
        putInt(byteArr.size)
        out.writeBytes(byteArr)
    }

    fun putUuid(uuid: UUID): ObjectEncoder = apply { putLong(uuid.mostSignificantBits); putLong(uuid.leastSignificantBits) }
    fun putString(str: String): ObjectEncoder = putBytes(str.toByteArray())
    fun <E : Enum<E>> putEnum(e: E): ObjectEncoder = putInt(e.ordinal)

    fun <T> put(obj: T, codec: Codec<T>): ObjectEncoder = putBytes(codec.encode(obj))

    fun <T> putNullable(obj: T?, codec: Codec<T>): ObjectEncoder = apply {
        if (obj == null) {
            putBool(false)
        } else {
            putBool(true)
            put(obj, codec)
        }
    }

    fun <T> putList(list: List<T>, codec: Codec<T>): ObjectEncoder = apply {
        putInt(list.size)
        for (obj in list) {
            put(obj, codec)
        }
    }

    fun <K, V> putMap(map: Map<K, V>, keyCodec: Codec<K>, valueCodec: Codec<V>) = apply {
        putInt(map.size)
        for ((key, value) in map) {
            put(key, keyCodec)
            put(value, valueCodec)
        }
    }

    fun getData(): ByteArray = out.toByteArray()
}