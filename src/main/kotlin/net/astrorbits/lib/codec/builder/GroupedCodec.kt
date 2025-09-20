package net.astrorbits.lib.codec.builder

import net.astrorbits.lib.codec.Codec
import net.astrorbits.lib.codec.ObjectDecoder
import net.astrorbits.lib.codec.ObjectEncoder
import net.astrorbits.lib.codec.builder.CodecWithGetter.Companion.put
import net.astrorbits.lib.codec.builder.CodecWithGetter.Companion.get

class GroupedCodec1<O, T1>(
    val c1: CodecWithGetter<O, T1>,
) {
    fun build(constructor: (T1) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return constructor(ObjectDecoder(data).get(c1))
            }
        }
    }
}

class GroupedCodec2<O, T1, T2>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
) {
    fun build(constructor: (T1, T2) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                ) }
            }
        }
    }
}

class GroupedCodec3<O, T1, T2, T3>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
) {
    fun build(constructor: (T1, T2, T3) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                ) }
            }
        }
    }
}

class GroupedCodec4<O, T1, T2, T3, T4>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
) {
    fun build(constructor: (T1, T2, T3, T4) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                ) }
            }
        }
    }
}

class GroupedCodec5<O, T1, T2, T3, T4, T5>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
    val c5: CodecWithGetter<O, T5>,
) {
    fun build(constructor: (T1, T2, T3, T4, T5) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .put(obj, c5)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                    it.get(c5),
                ) }
            }
        }
    }
}

class GroupedCodec6<O, T1, T2, T3, T4, T5, T6>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
    val c5: CodecWithGetter<O, T5>,
    val c6: CodecWithGetter<O, T6>,
) {
    fun build(constructor: (T1, T2, T3, T4, T5, T6) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .put(obj, c5)
                    .put(obj, c6)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                    it.get(c5),
                    it.get(c6),
                ) }
            }
        }
    }
}

class GroupedCodec7<O, T1, T2, T3, T4, T5, T6, T7>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
    val c5: CodecWithGetter<O, T5>,
    val c6: CodecWithGetter<O, T6>,
    val c7: CodecWithGetter<O, T7>,
) {
    fun build(constructor: (T1, T2, T3, T4, T5, T6, T7) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .put(obj, c5)
                    .put(obj, c6)
                    .put(obj, c7)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                    it.get(c5),
                    it.get(c6),
                    it.get(c7),
                ) }
            }
        }
    }
}

class GroupedCodec8<O, T1, T2, T3, T4, T5, T6, T7, T8>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
    val c5: CodecWithGetter<O, T5>,
    val c6: CodecWithGetter<O, T6>,
    val c7: CodecWithGetter<O, T7>,
    val c8: CodecWithGetter<O, T8>,
) {
    fun build(constructor: (T1, T2, T3, T4, T5, T6, T7, T8) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .put(obj, c5)
                    .put(obj, c6)
                    .put(obj, c7)
                    .put(obj, c8)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                    it.get(c5),
                    it.get(c6),
                    it.get(c7),
                    it.get(c8),
                ) }
            }
        }
    }
}

class GroupedCodec9<O, T1, T2, T3, T4, T5, T6, T7, T8, T9>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
    val c5: CodecWithGetter<O, T5>,
    val c6: CodecWithGetter<O, T6>,
    val c7: CodecWithGetter<O, T7>,
    val c8: CodecWithGetter<O, T8>,
    val c9: CodecWithGetter<O, T9>,
) {
    fun build(constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .put(obj, c5)
                    .put(obj, c6)
                    .put(obj, c7)
                    .put(obj, c8)
                    .put(obj, c9)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                    it.get(c5),
                    it.get(c6),
                    it.get(c7),
                    it.get(c8),
                    it.get(c9),
                ) }
            }
        }
    }
}

class GroupedCodec10<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
    val c5: CodecWithGetter<O, T5>,
    val c6: CodecWithGetter<O, T6>,
    val c7: CodecWithGetter<O, T7>,
    val c8: CodecWithGetter<O, T8>,
    val c9: CodecWithGetter<O, T9>,
    val c10: CodecWithGetter<O, T10>,
) {
    fun build(constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .put(obj, c5)
                    .put(obj, c6)
                    .put(obj, c7)
                    .put(obj, c8)
                    .put(obj, c9)
                    .put(obj, c10)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                    it.get(c5),
                    it.get(c6),
                    it.get(c7),
                    it.get(c8),
                    it.get(c9),
                    it.get(c10),
                ) }
            }
        }
    }
}

class GroupedCodec11<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
    val c5: CodecWithGetter<O, T5>,
    val c6: CodecWithGetter<O, T6>,
    val c7: CodecWithGetter<O, T7>,
    val c8: CodecWithGetter<O, T8>,
    val c9: CodecWithGetter<O, T9>,
    val c10: CodecWithGetter<O, T10>,
    val c11: CodecWithGetter<O, T11>,
) {
    fun build(constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .put(obj, c5)
                    .put(obj, c6)
                    .put(obj, c7)
                    .put(obj, c8)
                    .put(obj, c9)
                    .put(obj, c10)
                    .put(obj, c11)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                    it.get(c5),
                    it.get(c6),
                    it.get(c7),
                    it.get(c8),
                    it.get(c9),
                    it.get(c10),
                    it.get(c11),
                ) }
            }
        }
    }
}

class GroupedCodec12<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
    val c5: CodecWithGetter<O, T5>,
    val c6: CodecWithGetter<O, T6>,
    val c7: CodecWithGetter<O, T7>,
    val c8: CodecWithGetter<O, T8>,
    val c9: CodecWithGetter<O, T9>,
    val c10: CodecWithGetter<O, T10>,
    val c11: CodecWithGetter<O, T11>,
    val c12: CodecWithGetter<O, T12>,
) {
    fun build(constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .put(obj, c5)
                    .put(obj, c6)
                    .put(obj, c7)
                    .put(obj, c8)
                    .put(obj, c9)
                    .put(obj, c10)
                    .put(obj, c11)
                    .put(obj, c12)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                    it.get(c5),
                    it.get(c6),
                    it.get(c7),
                    it.get(c8),
                    it.get(c9),
                    it.get(c10),
                    it.get(c11),
                    it.get(c12),
                ) }
            }
        }
    }
}

class GroupedCodec13<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
    val c5: CodecWithGetter<O, T5>,
    val c6: CodecWithGetter<O, T6>,
    val c7: CodecWithGetter<O, T7>,
    val c8: CodecWithGetter<O, T8>,
    val c9: CodecWithGetter<O, T9>,
    val c10: CodecWithGetter<O, T10>,
    val c11: CodecWithGetter<O, T11>,
    val c12: CodecWithGetter<O, T12>,
    val c13: CodecWithGetter<O, T13>,
) {
    fun build(constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .put(obj, c5)
                    .put(obj, c6)
                    .put(obj, c7)
                    .put(obj, c8)
                    .put(obj, c9)
                    .put(obj, c10)
                    .put(obj, c11)
                    .put(obj, c12)
                    .put(obj, c13)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                    it.get(c5),
                    it.get(c6),
                    it.get(c7),
                    it.get(c8),
                    it.get(c9),
                    it.get(c10),
                    it.get(c11),
                    it.get(c12),
                    it.get(c13),
                ) }
            }
        }
    }
}

class GroupedCodec14<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
    val c5: CodecWithGetter<O, T5>,
    val c6: CodecWithGetter<O, T6>,
    val c7: CodecWithGetter<O, T7>,
    val c8: CodecWithGetter<O, T8>,
    val c9: CodecWithGetter<O, T9>,
    val c10: CodecWithGetter<O, T10>,
    val c11: CodecWithGetter<O, T11>,
    val c12: CodecWithGetter<O, T12>,
    val c13: CodecWithGetter<O, T13>,
    val c14: CodecWithGetter<O, T14>,
) {
    fun build(constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .put(obj, c5)
                    .put(obj, c6)
                    .put(obj, c7)
                    .put(obj, c8)
                    .put(obj, c9)
                    .put(obj, c10)
                    .put(obj, c11)
                    .put(obj, c12)
                    .put(obj, c13)
                    .put(obj, c14)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                    it.get(c5),
                    it.get(c6),
                    it.get(c7),
                    it.get(c8),
                    it.get(c9),
                    it.get(c10),
                    it.get(c11),
                    it.get(c12),
                    it.get(c13),
                    it.get(c14),
                ) }
            }
        }
    }
}

class GroupedCodec15<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
    val c5: CodecWithGetter<O, T5>,
    val c6: CodecWithGetter<O, T6>,
    val c7: CodecWithGetter<O, T7>,
    val c8: CodecWithGetter<O, T8>,
    val c9: CodecWithGetter<O, T9>,
    val c10: CodecWithGetter<O, T10>,
    val c11: CodecWithGetter<O, T11>,
    val c12: CodecWithGetter<O, T12>,
    val c13: CodecWithGetter<O, T13>,
    val c14: CodecWithGetter<O, T14>,
    val c15: CodecWithGetter<O, T15>,
) {
    fun build(constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .put(obj, c5)
                    .put(obj, c6)
                    .put(obj, c7)
                    .put(obj, c8)
                    .put(obj, c9)
                    .put(obj, c10)
                    .put(obj, c11)
                    .put(obj, c12)
                    .put(obj, c13)
                    .put(obj, c14)
                    .put(obj, c15)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                    it.get(c5),
                    it.get(c6),
                    it.get(c7),
                    it.get(c8),
                    it.get(c9),
                    it.get(c10),
                    it.get(c11),
                    it.get(c12),
                    it.get(c13),
                    it.get(c14),
                    it.get(c15),
                ) }
            }
        }
    }
}

class GroupedCodec16<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>(
    val c1: CodecWithGetter<O, T1>,
    val c2: CodecWithGetter<O, T2>,
    val c3: CodecWithGetter<O, T3>,
    val c4: CodecWithGetter<O, T4>,
    val c5: CodecWithGetter<O, T5>,
    val c6: CodecWithGetter<O, T6>,
    val c7: CodecWithGetter<O, T7>,
    val c8: CodecWithGetter<O, T8>,
    val c9: CodecWithGetter<O, T9>,
    val c10: CodecWithGetter<O, T10>,
    val c11: CodecWithGetter<O, T11>,
    val c12: CodecWithGetter<O, T12>,
    val c13: CodecWithGetter<O, T13>,
    val c14: CodecWithGetter<O, T14>,
    val c15: CodecWithGetter<O, T15>,
    val c16: CodecWithGetter<O, T16>,
) {
    fun build(constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) -> O): Codec<O> {
        return object : Codec<O> {
            override fun encode(obj: O): ByteArray {
                return ObjectEncoder()
                    .put(obj, c1)
                    .put(obj, c2)
                    .put(obj, c3)
                    .put(obj, c4)
                    .put(obj, c5)
                    .put(obj, c6)
                    .put(obj, c7)
                    .put(obj, c8)
                    .put(obj, c9)
                    .put(obj, c10)
                    .put(obj, c11)
                    .put(obj, c12)
                    .put(obj, c13)
                    .put(obj, c14)
                    .put(obj, c15)
                    .put(obj, c16)
                    .getData()
            }

            override fun decode(data: ByteArray): O {
                return ObjectDecoder(data).let { constructor(
                    it.get(c1),
                    it.get(c2),
                    it.get(c3),
                    it.get(c4),
                    it.get(c5),
                    it.get(c6),
                    it.get(c7),
                    it.get(c8),
                    it.get(c9),
                    it.get(c10),
                    it.get(c11),
                    it.get(c12),
                    it.get(c13),
                    it.get(c14),
                    it.get(c15),
                    it.get(c16),
                ) }
            }
        }
    }
}