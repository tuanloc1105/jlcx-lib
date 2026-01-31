package vn.com.lcx.processor.exception;

/**
 * Exception thrown when mapping configuration is invalid.
 * Provides factory methods for common error scenarios.
 */
public class InvalidMappingException extends MapperProcessingException {

    public InvalidMappingException(String message, String methodName) {
        super(message, methodName);
    }

    public InvalidMappingException(String message, String methodName, Throwable cause) {
        super(message, methodName, cause);
    }

    /**
     * Creates exception for invalid parameter count
     */
    public static InvalidMappingException invalidParameterCount(String methodName, int expected, int actual) {
        return new InvalidMappingException(
                String.format("Expected %d parameter(s) but found %d", expected, actual),
                methodName
        );
    }

    /**
     * Creates exception for type mismatch between source and target fields
     */
    public static InvalidMappingException typeMismatch(String methodName, String fieldName,
                                                       String sourceType, String targetType) {
        return new InvalidMappingException(
                String.format("Field '%s' has incompatible types: %s -> %s", fieldName, sourceType, targetType),
                methodName
        );
    }

    /**
     * Creates exception when merging two different classes
     */
    public static InvalidMappingException mergingDifferentClasses(String methodName,
                                                                   String firstClass, String secondClass) {
        return new InvalidMappingException(
                String.format("Cannot merge different classes: %s and %s", firstClass, secondClass),
                methodName
        );
    }

    /**
     * Creates exception when field is not found
     */
    public static InvalidMappingException fieldNotFound(String methodName, String fieldName, String className) {
        return new InvalidMappingException(
                String.format("Field '%s' not found in class %s", fieldName, className),
                methodName
        );
    }

    /**
     * Creates exception when mapping annotation has invalid configuration
     */
    public static InvalidMappingException invalidMappingConfig(String methodName, String details) {
        return new InvalidMappingException(
                String.format("Invalid @Mapping configuration: %s", details),
                methodName
        );
    }
}
