package net.astrorbits.util.codec;

/**
 * 若反序列化数据时，数据无效，无法读取为一个正常的对象，则会抛出该异常
 */
public class InvalidDataException extends ObjectDeserializeException {
    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDataException(Throwable cause) {
        super(cause);
    }

    public InvalidDataException() {
        super();
    }
}
