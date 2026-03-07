package vn.io.lcx.processor.template;

/**
 * Code generation templates for MapperClassProcessor.
 * Centralizes all template strings for easier maintenance.
 */
public final class CodeTemplates {

    private CodeTemplates() {
    }

    /**
     * Template for generated mapper class
     * Parameters: packageName, generatedDate, className, interfaceName, className, methodsCode
     */
    public static final String CLASS_TEMPLATE = """
            package %s;

            import java.util.*;
            import javax.annotation.processing.Generated;

            @Generated(
                value = "vn.io.lcx.processor.MapperClassProcessor",
                date = "%s"
            )
            @vn.io.lcx.common.annotation.Component
            public class %s implements %s {

                public %s() {
                }
                %s
            }""";

    /**
     * Template for simple mapping method
     * Parameters: sourceClass, targetClass, paramName, targetClass,
     *             targetClass, methodName, sourceClass, paramName,
     *             paramName, targetClass, targetClass, mappingLines
     */
    public static final String MAPPING_METHOD_TEMPLATE = """

                /**
                 * Maps {@link %s} to {@link %s}
                 * @param %s source object
                 * @return mapped instance of %s, or null if source is null
                 */
                @Override
                public %s %s(%s %s) {
                    if (%s == null) {
                        return null;
                    }
                    %s instance = new %s();
                    %s
                    return instance;
                }
            """;

    /**
     * Template for merging method
     * Parameters: secondParamName, firstParamName,
     *             returnType, methodName, firstParamClass, firstParamName,
     *             secondParamClass, secondParamName,
     *             firstParamName, secondParamName, nullReturnStatement,
     *             mergingLines, returnStatement
     */
    public static final String MERGING_METHOD_TEMPLATE = """

                /**
                 * Merges fields from {@code %s} into {@code %s}
                 */
                @Override
                public %s %s(%s %s, %s %s) {
                    if (%s == null || %s == null) {
                        %s
                    }
                    %s
                    %s
                }
            """;

    /**
     * Template for simple setter line
     * Parameters: targetFieldName, sourceParamName, sourceFieldName
     */
    public static final String SETTER_LINE = "\n        instance.set%s(%s.get%s());";

    /**
     * Template for null-check setter in merging
     * Parameters: firstParam, fieldName, firstParam, fieldName, secondParam, fieldName
     */
    public static final String NULL_CHECK_SETTER = """

                    if (%s.get%s() == null) {
                        %s.set%s(%s.get%s());
                    }""";

    /**
     * Template for direct merge setter
     * Parameters: firstParam, fieldName, secondParam, fieldName
     */
    public static final String MERGE_SETTER = "\n        %s.set%s(%s.get%s());";

    /**
     * Template for collection mapping with nested mapper
     * Parameters: sourceParam, sourceField, targetField, sourceParam, sourceField, mapperMethod
     */
    public static final String COLLECTION_MAPPING = """

                    if (%s.get%s() != null) {
                        instance.set%s(
                            %s.get%s().stream()
                                .map(this::%s)
                                .collect(java.util.stream.Collectors.toList())
                        );
                    }""";

    /**
     * Template for nested object mapping
     * Parameters: sourceParam, sourceField, targetField, mapperMethod, sourceParam, sourceField
     */
    public static final String NESTED_OBJECT_MAPPING = """

                    if (%s.get%s() != null) {
                        instance.set%s(this.%s(%s.get%s()));
                    }""";

    /**
     * Template for multi-parameter mapping method.
     * Parameters: returnTypeSimple, javadocParamLines, returnTypeSimple,
     *             returnType, methodName, parameterSignature,
     *             nullChecks,
     *             returnType, returnType,
     *             mappingLines
     */
    public static final String MULTI_PARAM_MAPPING_METHOD_TEMPLATE = """

                /**
                 * Maps multiple sources to {@link %s}
%s
                 * @return mapped instance of %s, or null if any source is null
                 */
                @Override
                public %s %s(%s) {
                    if (%s) {
                        return null;
                    }
                    %s instance = new %s();
                    %s
                    return instance;
                }
            """;

    /**
     * Template for null-safe simple setter
     * Parameters: sourceParam, sourceField, targetField, sourceParam, sourceField
     */
    public static final String NULL_SAFE_SETTER = """

                    if (%s.get%s() != null) {
                        instance.set%s(%s.get%s());
                    }""";

    /**
     * Utility method to get simple class name from fully qualified name
     */
    public static String getSimpleClassName(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf('.');
        return lastDot > 0 ? fullClassName.substring(lastDot + 1) : fullClassName;
    }
}
