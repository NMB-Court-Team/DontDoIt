package net.astrorbits.lib

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE
)
@Retention(AnnotationRetention.BINARY)
annotation class NMSWarning(
    val message: String = "警告：使用了NMS，可能导致跨版本不兼容"
)