package vn.io.lcx.processor.utility;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link MethodInfo} data class and its builder.
 * <p>
 * MethodInfo holds method metadata used during annotation processing:
 * methodName, inputParameters (List of VariableElement), and outputParameter (TypeMirror).
 * Since VariableElement and TypeMirror are compile-time types, we test with nulls
 * and verify the builder/getter contract.
 */
class MethodInfoTest {

    // ------------------------------------------------------------------ //
    //  No-arg constructor
    // ------------------------------------------------------------------ //

    @Nested
    class NoArgConstructor {

        @Test
        void createsInstanceWithNullFields() {
            MethodInfo info = new MethodInfo();

            assertNull(info.getMethodName());
            assertNull(info.getInputParameters());
            assertNull(info.getOutputParameter());
        }
    }

    // ------------------------------------------------------------------ //
    //  All-args constructor
    // ------------------------------------------------------------------ //

    @Nested
    class AllArgsConstructor {

        @Test
        void createsInstanceWithGivenValues() {
            MethodInfo info = new MethodInfo("findById", Collections.emptyList(), null);

            assertEquals("findById", info.getMethodName());
            assertNotNull(info.getInputParameters());
            assertEquals(0, info.getInputParameters().size());
            assertNull(info.getOutputParameter());
        }

        @Test
        void acceptsNullMethodName() {
            MethodInfo info = new MethodInfo(null, null, null);

            assertNull(info.getMethodName());
            assertNull(info.getInputParameters());
            assertNull(info.getOutputParameter());
        }
    }

    // ------------------------------------------------------------------ //
    //  Builder
    // ------------------------------------------------------------------ //

    @Nested
    class Builder {

        @Test
        void builderReturnsNonNull() {
            MethodInfo.MethodInfoBuilder builder = MethodInfo.builder();

            assertNotNull(builder);
        }

        @Test
        void buildsWithMethodNameOnly() {
            MethodInfo info = MethodInfo.builder()
                    .methodName("save")
                    .build();

            assertEquals("save", info.getMethodName());
            assertNull(info.getInputParameters());
            assertNull(info.getOutputParameter());
        }

        @Test
        void buildsWithAllFields() {
            List<javax.lang.model.element.VariableElement> emptyParams = Collections.emptyList();
            MethodInfo info = MethodInfo.builder()
                    .methodName("update")
                    .inputParameters(emptyParams)
                    .outputParameter(null)
                    .build();

            assertEquals("update", info.getMethodName());
            assertNotNull(info.getInputParameters());
            assertEquals(0, info.getInputParameters().size());
            assertNull(info.getOutputParameter());
        }

        @Test
        void buildsWithNoFieldsSet() {
            MethodInfo info = MethodInfo.builder().build();

            assertNull(info.getMethodName());
            assertNull(info.getInputParameters());
            assertNull(info.getOutputParameter());
        }
    }

    // ------------------------------------------------------------------ //
    //  Setters
    // ------------------------------------------------------------------ //

    @Nested
    class Setters {

        @Test
        void setMethodNameUpdatesValue() {
            MethodInfo info = new MethodInfo();
            info.setMethodName("delete");

            assertEquals("delete", info.getMethodName());
        }

        @Test
        void setInputParametersUpdatesValue() {
            MethodInfo info = new MethodInfo();
            List<javax.lang.model.element.VariableElement> params = Collections.emptyList();
            info.setInputParameters(params);

            assertNotNull(info.getInputParameters());
            assertEquals(0, info.getInputParameters().size());
        }

        @Test
        void setOutputParameterUpdatesValue() {
            MethodInfo info = new MethodInfo();
            // TypeMirror is a compile-time API type; we verify null-set works
            info.setOutputParameter(null);

            assertNull(info.getOutputParameter());
        }

        @Test
        void settersOverrideConstructorValues() {
            MethodInfo info = new MethodInfo("original", Collections.emptyList(), null);
            info.setMethodName("overridden");

            assertEquals("overridden", info.getMethodName());
        }
    }
}
