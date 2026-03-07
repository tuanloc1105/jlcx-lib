package vn.io.lcx.processor.exception;

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

    /**
     * Creates exception when @Mapping references a parameter name that does not exist in the method signature
     */
    public static InvalidMappingException unknownFromParameter(String methodName, String fromParameter, java.util.List<String> availableParams) {
        return new InvalidMappingException(
                String.format("@Mapping references parameter '%s' but method only has parameters: %s", fromParameter, availableParams),
                methodName
        );
    }

    /**
     * Creates exception when a mapping method has no parameters
     */
    public static InvalidMappingException noParameters(String methodName) {
        return new InvalidMappingException(
                "Mapping method must have at least one parameter",
                methodName
        );
    }
}
