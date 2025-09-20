package net.astrorbits.lib.codec.builder

import net.astrorbits.lib.codec.Codec
import net.astrorbits.lib.codec.ObjectDecoder
import net.astrorbits.lib.codec.ObjectEncoder

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