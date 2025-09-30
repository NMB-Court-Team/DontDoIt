package net.astrorbits.lib.codec

import java.nio.ByteBuffer
import java.util.UUID

class ObjectDecoder(data: ByteArray) {
    private val buffer: ByteBuffer = ByteBuffer.wrap(data)

    fun getBool(): Boolean {
        if (buffer.remaining() < 1) {
            throw NotEnoughDataException("Not enough data to read boolean: " + buffer.array().contentToString())
        }
        val next = buffer.get().toInt()
        if (next != 0 && next != 1) {
            throw InvalidDataException("Invalid boolean value: $next")
        }
        return next == 1
    }

    fun getByte(): Byte {
        if (buffer.remaining() < 1) {
            throw NotEnoughDataException("Not enough data to read byte: " + buffer.array().contentToString())
        }
        return buffer.get()
    }

    fun getShort(): Short {
        if (buffer.remaining() < 2) {
            throw NotEnoughDataException("Not enough data to read short: " + buffer.array().contentToString())
        }
        return buffer.getShort()
    }

    fun getInt(): Int {
        if (buffer.remaining() < 4) {
            throw NotEnoughDataException("Not enough data to read int: " + buffer.array().contentToString())
        }
        return buffer.getInt()
    }

    fun getLong(): Long {
        if (buffer.remaining() < 8) {
            throw NotEnoughDataException("Not enough data to read long: " + buffer.array().contentToString())
        }
        return buffer.getLong()
    }

    fun getFloat(): Float {
        if (buffer.remaining() < 4) {
            throw NotEnoughDataException("Not enough data to read float: " + buffer.array().contentToString())
        }
        return buffer.getFloat()
    }

    fun getDouble(): Double {
        if (buffer.remaining() < 8) {
            throw NotEnoughDataException("Not enough data to read double: " + buffer.array().contentToString())
        }
        return buffer.getDouble()
    }

    fun getChar(): Char {
        if (buffer.remaining() < 2) {
            throw NotEnoughDataException("Not enough data to read char: " + buffer.array().contentToString())
        }
        return buffer.getShort().toInt().toChar()
    }

    fun getBytes(): ByteArray {
        if (buffer.remaining() < 4) {
            throw NotEnoughDataException("Not enough data to read byte array size: " + buffer.array().contentToString())
        }
        val size = buffer.getInt()
        if (buffer.remaining() < size) {
            throw NotEnoughDataException("Not enough data to read byte array: " + buffer.array().contentToString())
        }
        val bytes = ByteArray(size)
        buffer.get(bytes)
        return bytes
    }

    fun getUuid(): UUID {
        if (buffer.remaining() < 16) {
            throw NotEnoughDataException("Not enough data to read UUID: " + buffer.array().contentToString())
        }
        return UUID(buffer.getLong(), buffer.getLong())
    }

    fun getString(): String = String(getBytes())

    fun <E : Enum<E>> getEnum(enumClass: Class<E>): E {
        val ordinal = getInt()
        try {
            return enumClass.getEnumConstants()[ordinal]
        } catch (e: IndexOutOfBoundsException) {
            throw InvalidDataException("Ordinal out of range: $ordinal", e)
        }
    }

    fun <T> get(codec: Codec<T>): T {
        if (codec is SizedCodec) {
            val bytes = ByteArray(codec.size)
            buffer.get(bytes)
            return codec.decode(bytes)
        } else {
            return codec.decode(getBytes())
        }
    }

    fun <T> getNullable(codec: Codec<T>): T? {
        val notNull = getBool()
        return if (notNull) get(codec) else null
    }

    fun <T> getList(codec: Codec<T>): List<T> {
        val size = getInt()
        if (size < 0) throw InvalidDataException("Invalid list size: $size")
        val list = ArrayList<T>()
        for (i in 0 until size) {
            list.add(get(codec))
        }
        return list
    }

    fun <K, V> getMap(keyCodec: Codec<K>, valueCodec: Codec<V>): Map<K, V> {
        val size = getInt()
        if (size < 0) throw InvalidDataException("Invalid map size: $size")
        val map = HashMap<K, V>()
        for (i in 0 until size) {
            map[get(keyCodec)] = get(valueCodec)
        }
        return map
    }
}