package net.astrorbits.util.codec.builder


class CodecBuilder<O> {
    fun <T1> group(
        c1: CodecWithGetter<O, T1>,
    ): GroupedCodec1<O, T1> = GroupedCodec1(c1)

    fun <T1, T2> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
    ): GroupedCodec2<O, T1, T2> = GroupedCodec2(c1, c2)

    fun <T1, T2, T3> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
    ): GroupedCodec3<O, T1, T2, T3> = GroupedCodec3(c1, c2, c3)

    fun <T1, T2, T3, T4> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
    ): GroupedCodec4<O, T1, T2, T3, T4> = GroupedCodec4(c1, c2, c3, c4)

    fun <T1, T2, T3, T4, T5> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
        c5: CodecWithGetter<O, T5>,
    ): GroupedCodec5<O, T1, T2, T3, T4, T5> = GroupedCodec5(c1, c2, c3, c4, c5)

    fun <T1, T2, T3, T4, T5, T6> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
        c5: CodecWithGetter<O, T5>,
        c6: CodecWithGetter<O, T6>,
    ): GroupedCodec6<O, T1, T2, T3, T4, T5, T6> = GroupedCodec6(c1, c2, c3, c4, c5, c6)

    fun <T1, T2, T3, T4, T5, T6, T7> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
        c5: CodecWithGetter<O, T5>,
        c6: CodecWithGetter<O, T6>,
        c7: CodecWithGetter<O, T7>,
    ): GroupedCodec7<O, T1, T2, T3, T4, T5, T6, T7> = GroupedCodec7(c1, c2, c3, c4, c5, c6, c7)

    fun <T1, T2, T3, T4, T5, T6, T7, T8> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
        c5: CodecWithGetter<O, T5>,
        c6: CodecWithGetter<O, T6>,
        c7: CodecWithGetter<O, T7>,
        c8: CodecWithGetter<O, T8>,
    ): GroupedCodec8<O, T1, T2, T3, T4, T5, T6, T7, T8> = GroupedCodec8(c1, c2, c3, c4, c5, c6, c7, c8)

    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
        c5: CodecWithGetter<O, T5>,
        c6: CodecWithGetter<O, T6>,
        c7: CodecWithGetter<O, T7>,
        c8: CodecWithGetter<O, T8>,
        c9: CodecWithGetter<O, T9>,
    ): GroupedCodec9<O, T1, T2, T3, T4, T5, T6, T7, T8, T9> = GroupedCodec9(c1, c2, c3, c4, c5, c6, c7, c8, c9)

    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
        c5: CodecWithGetter<O, T5>,
        c6: CodecWithGetter<O, T6>,
        c7: CodecWithGetter<O, T7>,
        c8: CodecWithGetter<O, T8>,
        c9: CodecWithGetter<O, T9>,
        c10: CodecWithGetter<O, T10>,
    ): GroupedCodec10<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> = GroupedCodec10(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10)

    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
        c5: CodecWithGetter<O, T5>,
        c6: CodecWithGetter<O, T6>,
        c7: CodecWithGetter<O, T7>,
        c8: CodecWithGetter<O, T8>,
        c9: CodecWithGetter<O, T9>,
        c10: CodecWithGetter<O, T10>,
        c11: CodecWithGetter<O, T11>,
    ): GroupedCodec11<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> = GroupedCodec11(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11)

    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
        c5: CodecWithGetter<O, T5>,
        c6: CodecWithGetter<O, T6>,
        c7: CodecWithGetter<O, T7>,
        c8: CodecWithGetter<O, T8>,
        c9: CodecWithGetter<O, T9>,
        c10: CodecWithGetter<O, T10>,
        c11: CodecWithGetter<O, T11>,
        c12: CodecWithGetter<O, T12>,
    ): GroupedCodec12<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> = GroupedCodec12(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12)

    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
        c5: CodecWithGetter<O, T5>,
        c6: CodecWithGetter<O, T6>,
        c7: CodecWithGetter<O, T7>,
        c8: CodecWithGetter<O, T8>,
        c9: CodecWithGetter<O, T9>,
        c10: CodecWithGetter<O, T10>,
        c11: CodecWithGetter<O, T11>,
        c12: CodecWithGetter<O, T12>,
        c13: CodecWithGetter<O, T13>,
    ): GroupedCodec13<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> = GroupedCodec13(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13)

    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
        c5: CodecWithGetter<O, T5>,
        c6: CodecWithGetter<O, T6>,
        c7: CodecWithGetter<O, T7>,
        c8: CodecWithGetter<O, T8>,
        c9: CodecWithGetter<O, T9>,
        c10: CodecWithGetter<O, T10>,
        c11: CodecWithGetter<O, T11>,
        c12: CodecWithGetter<O, T12>,
        c13: CodecWithGetter<O, T13>,
        c14: CodecWithGetter<O, T14>,
    ): GroupedCodec14<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> = GroupedCodec14(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14)

    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
        c5: CodecWithGetter<O, T5>,
        c6: CodecWithGetter<O, T6>,
        c7: CodecWithGetter<O, T7>,
        c8: CodecWithGetter<O, T8>,
        c9: CodecWithGetter<O, T9>,
        c10: CodecWithGetter<O, T10>,
        c11: CodecWithGetter<O, T11>,
        c12: CodecWithGetter<O, T12>,
        c13: CodecWithGetter<O, T13>,
        c14: CodecWithGetter<O, T14>,
        c15: CodecWithGetter<O, T15>,
    ): GroupedCodec15<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> = GroupedCodec15(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15)

    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> group(
        c1: CodecWithGetter<O, T1>,
        c2: CodecWithGetter<O, T2>,
        c3: CodecWithGetter<O, T3>,
        c4: CodecWithGetter<O, T4>,
        c5: CodecWithGetter<O, T5>,
        c6: CodecWithGetter<O, T6>,
        c7: CodecWithGetter<O, T7>,
        c8: CodecWithGetter<O, T8>,
        c9: CodecWithGetter<O, T9>,
        c10: CodecWithGetter<O, T10>,
        c11: CodecWithGetter<O, T11>,
        c12: CodecWithGetter<O, T12>,
        c13: CodecWithGetter<O, T13>,
        c14: CodecWithGetter<O, T14>,
        c15: CodecWithGetter<O, T15>,
        c16: CodecWithGetter<O, T16>,
    ): GroupedCodec16<O, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> = GroupedCodec16(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16)
}