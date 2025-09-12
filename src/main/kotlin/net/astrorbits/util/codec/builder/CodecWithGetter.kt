package net.astrorbits.util.codec.builder

import net.astrorbits.util.codec.Codec
import net.astrorbits.util.codec.ObjectDecoder
import net.astrorbits.util.codec.ObjectEncoder

class CodecWithGetter<O, T>(val codec: Codec<T>, val getter: (O) -> T) {
    companion object {
        fun <O, T> ObjectEncoder.put(obj: O, codec: CodecWithGetter<O, T>): ObjectEncoder {
            return put(codec.getter(obj), codec.codec)
        }

        fun <O, T> ObjectDecoder.get(codec: CodecWithGetter<O, T>): T {
            return get(codec.codec)
        }
    }
}