package net.astrorbits.util.codec;

/**
 * 反序列化数据时出错导致抛出的所有异常的基类异常
 */
public class ObjectDeserializeException extends RuntimeException {
    public ObjectDeserializeException(String message) {
        super(message);
    }

    public ObjectDeserializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectDeserializeException(Throwable cause) {
        super(cause);
    }

    public ObjectDeserializeException() {
        super();
    }
}
