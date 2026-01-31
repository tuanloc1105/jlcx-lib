package vn.com.lcx.processor.exception;

/**
 * Base exception for mapper processing errors.
 * Provides structured error messages with element context.
 */
public class MapperProcessingException extends RuntimeException {

    private final String elementName;

    public MapperProcessingException(String message, String elementName) {
        super(String.format("[%s] %s", elementName, message));
        this.elementName = elementName;
    }

    public MapperProcessingException(String message, String elementName, Throwable cause) {
        super(String.format("[%s] %s", elementName, message), cause);
        this.elementName = elementName;
    }

    public String getElementName() {
        return elementName;
    }
}
