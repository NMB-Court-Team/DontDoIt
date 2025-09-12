package net.astrorbits.util.codec

abstract class SizedCodec<T>(val size: Int) : Codec<T> {
    protected abstract fun encodeSized(obj: T): ByteArray

    protected abstract fun decodeSized(data: ByteArray): T

    override fun encode(obj: T): ByteArray {
        val data = ByteArray(size)
        System.arraycopy(encodeSized(obj), 0, data, 0, size);
        return data;
    }

    override fun decode(data: ByteArray): T {
        if (data.size != size) {
            throw InvalidDataException("Data size is not equal to the expected size")
        }
        return decodeSized(data);
    }
}