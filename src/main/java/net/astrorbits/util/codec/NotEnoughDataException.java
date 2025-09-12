package net.astrorbits.util.codec;

/**
 * 若反序列化数据时，剩余的数据不足以完成序列化，则会抛出该异常
 */
public class NotEnoughDataException extends ObjectDeserializeException {
    public NotEnoughDataException(String message) {
        super(message);
    }

    public NotEnoughDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughDataException(Throwable cause) {
        super(cause);
    }

    public NotEnoughDataException() {
        super();
    }
}
