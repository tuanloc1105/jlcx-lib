package vn.io.lcx.processor.exception;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link InvalidMappingException} and {@link MapperProcessingException}.
 * <p>
 * InvalidMappingException provides factory methods for common mapping error
 * scenarios. MapperProcessingException is the base class that formats error
 * messages with element context as "[elementName] message".
 */
class InvalidMappingExceptionTest {

    // ------------------------------------------------------------------ //
    //  MapperProcessingException (base class)
    // ------------------------------------------------------------------ //

    @Nested
    class MapperProcessingExceptionTest {

        @Test
        void formatsMessageWithElementName() {
            MapperProcessingException ex = new MapperProcessingException(
                    "something went wrong", "mapUser");

            assertEquals("[mapUser] something went wrong", ex.getMessage());
        }

        @Test
        void storesElementName() {
            MapperProcessingException ex = new MapperProcessingException(
                    "error", "convertDto");

            assertEquals("convertDto", ex.getElementName());
        }

        @Test
        void extendsRuntimeException() {
            MapperProcessingException ex = new MapperProcessingException(
                    "error", "method");

            assertTrue(ex instanceof RuntimeException);
        }

        @Test
        void constructorWithCausePreservesCause() {
            RuntimeException cause = new RuntimeException("root cause");
            MapperProcessingException ex = new MapperProcessingException(
                    "processing failed", "mapEntity", cause);

            assertEquals(cause, ex.getCause());
            assertEquals("[mapEntity] processing failed", ex.getMessage());
            assertEquals("mapEntity", ex.getElementName());
        }
    }

    // ------------------------------------------------------------------ //
    //  InvalidMappingException constructors
    // ------------------------------------------------------------------ //

    @Nested
    class Constructors {

        @Test
        void twoArgConstructorFormatsMessage() {
            InvalidMappingException ex = new InvalidMappingException(
                    "bad config", "toDto");

            assertEquals("[toDto] bad config", ex.getMessage());
            assertEquals("toDto", ex.getElementName());
        }

        @Test
        void threeArgConstructorPreservesCause() {
            Exception cause = new IllegalArgumentException("detail");
            InvalidMappingException ex = new InvalidMappingException(
                    "processing error", "mergeUser", cause);

            assertEquals(cause, ex.getCause());
            assertEquals("[mergeUser] processing error", ex.getMessage());
        }

        @Test
        void extendsMapperProcessingException() {
            InvalidMappingException ex = new InvalidMappingException("msg", "method");

            assertTrue(ex instanceof MapperProcessingException);
        }
    }

    // ------------------------------------------------------------------ //
    //  invalidParameterCount
    // ------------------------------------------------------------------ //

    @Nested
    class InvalidParameterCount {

        @Test
        void containsExpectedAndActualCounts() {
            InvalidMappingException ex = InvalidMappingException.invalidParameterCount(
                    "mapUser", 1, 3);

            assertTrue(ex.getMessage().contains("1"));
            assertTrue(ex.getMessage().contains("3"));
            assertEquals("mapUser", ex.getElementName());
        }

        @Test
        void messageFormat() {
            InvalidMappingException ex = InvalidMappingException.invalidParameterCount(
                    "convert", 2, 0);

            assertEquals("[convert] Expected 2 parameter(s) but found 0", ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  typeMismatch
    // ------------------------------------------------------------------ //

    @Nested
    class TypeMismatch {

        @Test
        void containsFieldNameAndTypes() {
            InvalidMappingException ex = InvalidMappingException.typeMismatch(
                    "mapEntity", "age", "java.lang.String", "int");

            assertTrue(ex.getMessage().contains("age"));
            assertTrue(ex.getMessage().contains("java.lang.String"));
            assertTrue(ex.getMessage().contains("int"));
            assertEquals("mapEntity", ex.getElementName());
        }

        @Test
        void messageFormat() {
            InvalidMappingException ex = InvalidMappingException.typeMismatch(
                    "toDto", "status", "StatusEnum", "String");

            assertEquals("[toDto] Field 'status' has incompatible types: StatusEnum -> String",
                    ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  mergingDifferentClasses
    // ------------------------------------------------------------------ //

    @Nested
    class MergingDifferentClasses {

        @Test
        void containsBothClassNames() {
            InvalidMappingException ex = InvalidMappingException.mergingDifferentClasses(
                    "mergeData", "UserDto", "OrderDto");

            assertTrue(ex.getMessage().contains("UserDto"));
            assertTrue(ex.getMessage().contains("OrderDto"));
            assertEquals("mergeData", ex.getElementName());
        }

        @Test
        void messageFormat() {
            InvalidMappingException ex = InvalidMappingException.mergingDifferentClasses(
                    "merge", "A", "B");

            assertEquals("[merge] Cannot merge different classes: A and B", ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  fieldNotFound
    // ------------------------------------------------------------------ //

    @Nested
    class FieldNotFound {

        @Test
        void containsFieldNameAndClassName() {
            InvalidMappingException ex = InvalidMappingException.fieldNotFound(
                    "mapUser", "middleName", "UserDto");

            assertTrue(ex.getMessage().contains("middleName"));
            assertTrue(ex.getMessage().contains("UserDto"));
            assertEquals("mapUser", ex.getElementName());
        }

        @Test
        void messageFormat() {
            InvalidMappingException ex = InvalidMappingException.fieldNotFound(
                    "convert", "xyz", "MyClass");

            assertEquals("[convert] Field 'xyz' not found in class MyClass", ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  invalidMappingConfig
    // ------------------------------------------------------------------ //

    @Nested
    class InvalidMappingConfig {

        @Test
        void containsDetails() {
            InvalidMappingException ex = InvalidMappingException.invalidMappingConfig(
                    "toEntity", "source and target cannot both be empty");

            assertTrue(ex.getMessage().contains("source and target cannot both be empty"));
            assertTrue(ex.getMessage().contains("@Mapping configuration"));
            assertEquals("toEntity", ex.getElementName());
        }

        @Test
        void messageFormat() {
            InvalidMappingException ex = InvalidMappingException.invalidMappingConfig(
                    "mapIt", "duplicate target field");

            assertEquals("[mapIt] Invalid @Mapping configuration: duplicate target field",
                    ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  unknownFromParameter
    // ------------------------------------------------------------------ //

    @Nested
    class UnknownFromParameter {

        @Test
        void containsParameterNameAndAvailableParams() {
            List<String> available = Arrays.asList("source", "extra");

            InvalidMappingException ex = InvalidMappingException.unknownFromParameter(
                    "mapUser", "unknown", available);

            assertTrue(ex.getMessage().contains("unknown"));
            assertTrue(ex.getMessage().contains("source"));
            assertTrue(ex.getMessage().contains("extra"));
            assertEquals("mapUser", ex.getElementName());
        }

        @Test
        void messageFormat() {
            List<String> available = Collections.singletonList("dto");

            InvalidMappingException ex = InvalidMappingException.unknownFromParameter(
                    "convert", "entity", available);

            assertEquals("[convert] @Mapping references parameter 'entity' but method only has parameters: [dto]",
                    ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  noParameters
    // ------------------------------------------------------------------ //

    @Nested
    class NoParameters {

        @Test
        void containsExpectedMessage() {
            InvalidMappingException ex = InvalidMappingException.noParameters("emptyMap");

            assertTrue(ex.getMessage().contains("at least one parameter"));
            assertEquals("emptyMap", ex.getElementName());
        }

        @Test
        void messageFormat() {
            InvalidMappingException ex = InvalidMappingException.noParameters("doMap");

            assertEquals("[doMap] Mapping method must have at least one parameter",
                    ex.getMessage());
        }
    }
}
