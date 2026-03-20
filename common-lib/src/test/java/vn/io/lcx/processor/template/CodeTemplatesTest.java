package vn.io.lcx.processor.template;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link CodeTemplates}.
 * <p>
 * CodeTemplates contains string constants used by MapperClassProcessor to generate
 * mapper implementation classes. We test the static utility method and validate
 * that templates contain expected format placeholders.
 */
class CodeTemplatesTest {

    // ------------------------------------------------------------------ //
    //  getSimpleClassName
    // ------------------------------------------------------------------ //

    @Nested
    class GetSimpleClassName {

        @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
        @CsvSource({
                "com.example.UserDto,      UserDto",
                "vn.io.lcx.common.MyClass, MyClass",
                "java.lang.String,         String",
                "a.b.c.d.E,                E"
        })
        void extractsSimpleNameFromQualifiedName(String fullName, String expected) {
            assertEquals(expected, CodeTemplates.getSimpleClassName(fullName));
        }

        @Test
        void returnsInputWhenNoDot() {
            assertEquals("MyClass", CodeTemplates.getSimpleClassName("MyClass"));
        }

        @Test
        void handlesSingleCharacterClassName() {
            assertEquals("X", CodeTemplates.getSimpleClassName("a.b.X"));
        }

        @Test
        void handlesEmptyString() {
            assertEquals("", CodeTemplates.getSimpleClassName(""));
        }

        @Test
        void handlesClassNameStartingWithDot() {
            // Edge case: ".MyClass" has lastIndexOf('.') = 0, and condition is lastDot > 0
            // so it returns the full string unchanged
            assertEquals(".MyClass", CodeTemplates.getSimpleClassName(".MyClass"));
        }
    }

    // ------------------------------------------------------------------ //
    //  CLASS_TEMPLATE
    // ------------------------------------------------------------------ //

    @Nested
    class ClassTemplate {

        @Test
        void isNotNullOrEmpty() {
            assertNotNull(CodeTemplates.CLASS_TEMPLATE);
            assertFalse(CodeTemplates.CLASS_TEMPLATE.isEmpty());
        }

        @Test
        void containsPackageDeclaration() {
            assertTrue(CodeTemplates.CLASS_TEMPLATE.contains("package %s"));
        }

        @Test
        void containsGeneratedAnnotation() {
            assertTrue(CodeTemplates.CLASS_TEMPLATE.contains("@Generated"));
            assertTrue(CodeTemplates.CLASS_TEMPLATE.contains("MapperClassProcessor"));
        }

        @Test
        void containsComponentAnnotation() {
            assertTrue(CodeTemplates.CLASS_TEMPLATE.contains("@vn.io.lcx.common.annotation.Component"));
        }

        @Test
        void containsImplementsClause() {
            assertTrue(CodeTemplates.CLASS_TEMPLATE.contains("implements %s"));
        }

        @Test
        void hasCorrectNumberOfFormatPlaceholders() {
            // Template uses %s for: packageName, generatedDate, className, interfaceName,
            // className (constructor), methodsCode
            long placeholderCount = CodeTemplates.CLASS_TEMPLATE.chars()
                    .filter(ch -> ch == '%')
                    .count();
            // Each %s = one %, plus the date format string
            assertTrue(placeholderCount >= 6,
                    "Expected at least 6 format placeholders, found " + placeholderCount);
        }

        @Test
        void canBeFormattedWithoutException() {
            String result = String.format(CodeTemplates.CLASS_TEMPLATE,
                    "com.example", "2024-01-01", "UserMapperImpl",
                    "UserMapper", "UserMapperImpl", "// methods");

            assertTrue(result.contains("com.example"));
            assertTrue(result.contains("UserMapperImpl"));
            assertTrue(result.contains("UserMapper"));
        }
    }

    // ------------------------------------------------------------------ //
    //  MAPPING_METHOD_TEMPLATE
    // ------------------------------------------------------------------ //

    @Nested
    class MappingMethodTemplate {

        @Test
        void isNotNullOrEmpty() {
            assertNotNull(CodeTemplates.MAPPING_METHOD_TEMPLATE);
            assertFalse(CodeTemplates.MAPPING_METHOD_TEMPLATE.isEmpty());
        }

        @Test
        void containsOverrideAnnotation() {
            assertTrue(CodeTemplates.MAPPING_METHOD_TEMPLATE.contains("@Override"));
        }

        @Test
        void containsNullCheck() {
            assertTrue(CodeTemplates.MAPPING_METHOD_TEMPLATE.contains("== null"));
            assertTrue(CodeTemplates.MAPPING_METHOD_TEMPLATE.contains("return null"));
        }

        @Test
        void containsInstanceCreation() {
            assertTrue(CodeTemplates.MAPPING_METHOD_TEMPLATE.contains("instance = new"));
            assertTrue(CodeTemplates.MAPPING_METHOD_TEMPLATE.contains("return instance"));
        }

        @Test
        void containsJavadoc() {
            assertTrue(CodeTemplates.MAPPING_METHOD_TEMPLATE.contains("/**"));
            assertTrue(CodeTemplates.MAPPING_METHOD_TEMPLATE.contains("Maps"));
        }
    }

    // ------------------------------------------------------------------ //
    //  MERGING_METHOD_TEMPLATE
    // ------------------------------------------------------------------ //

    @Nested
    class MergingMethodTemplate {

        @Test
        void isNotNullOrEmpty() {
            assertNotNull(CodeTemplates.MERGING_METHOD_TEMPLATE);
            assertFalse(CodeTemplates.MERGING_METHOD_TEMPLATE.isEmpty());
        }

        @Test
        void containsOverrideAnnotation() {
            assertTrue(CodeTemplates.MERGING_METHOD_TEMPLATE.contains("@Override"));
        }

        @Test
        void containsMergesJavadoc() {
            assertTrue(CodeTemplates.MERGING_METHOD_TEMPLATE.contains("Merges fields"));
        }

        @Test
        void containsDualNullCheck() {
            assertTrue(CodeTemplates.MERGING_METHOD_TEMPLATE.contains("== null || %s == null"));
        }
    }

    // ------------------------------------------------------------------ //
    //  SETTER_LINE
    // ------------------------------------------------------------------ //

    @Nested
    class SetterLine {

        @Test
        void containsSetterAndGetterPattern() {
            assertTrue(CodeTemplates.SETTER_LINE.contains("instance.set%s"));
            assertTrue(CodeTemplates.SETTER_LINE.contains(".get%s()"));
        }

        @Test
        void canBeFormattedCorrectly() {
            String result = String.format(CodeTemplates.SETTER_LINE, "Name", "source", "Name");

            assertTrue(result.contains("instance.setName(source.getName())"));
        }
    }

    // ------------------------------------------------------------------ //
    //  NULL_CHECK_SETTER
    // ------------------------------------------------------------------ //

    @Nested
    class NullCheckSetter {

        @Test
        void containsNullCheckCondition() {
            assertTrue(CodeTemplates.NULL_CHECK_SETTER.contains(".get%s() == null"));
        }

        @Test
        void containsSetterInsideBlock() {
            assertTrue(CodeTemplates.NULL_CHECK_SETTER.contains(".set%s("));
        }

        @Test
        void canBeFormattedCorrectly() {
            String result = String.format(CodeTemplates.NULL_CHECK_SETTER,
                    "target", "Name", "target", "Name", "source", "Name");

            assertTrue(result.contains("target.getName() == null"));
            assertTrue(result.contains("target.setName(source.getName())"));
        }
    }

    // ------------------------------------------------------------------ //
    //  MERGE_SETTER
    // ------------------------------------------------------------------ //

    @Nested
    class MergeSetter {

        @Test
        void containsDirectSetterPattern() {
            assertTrue(CodeTemplates.MERGE_SETTER.contains("%s.set%s(%s.get%s())"));
        }

        @Test
        void canBeFormattedCorrectly() {
            String result = String.format(CodeTemplates.MERGE_SETTER,
                    "target", "Name", "source", "Name");

            assertTrue(result.contains("target.setName(source.getName())"));
        }
    }

    // ------------------------------------------------------------------ //
    //  COLLECTION_MAPPING
    // ------------------------------------------------------------------ //

    @Nested
    class CollectionMapping {

        @Test
        void containsStreamOperations() {
            assertTrue(CodeTemplates.COLLECTION_MAPPING.contains(".stream()"));
            assertTrue(CodeTemplates.COLLECTION_MAPPING.contains(".map(this::"));
            assertTrue(CodeTemplates.COLLECTION_MAPPING.contains("Collectors.toList()"));
        }

        @Test
        void containsNullCheck() {
            assertTrue(CodeTemplates.COLLECTION_MAPPING.contains("!= null"));
        }
    }

    // ------------------------------------------------------------------ //
    //  NESTED_OBJECT_MAPPING
    // ------------------------------------------------------------------ //

    @Nested
    class NestedObjectMapping {

        @Test
        void containsNestedMapperCall() {
            assertTrue(CodeTemplates.NESTED_OBJECT_MAPPING.contains("this.%s("));
        }

        @Test
        void containsNullCheck() {
            assertTrue(CodeTemplates.NESTED_OBJECT_MAPPING.contains("!= null"));
        }

        @Test
        void containsSetterCall() {
            assertTrue(CodeTemplates.NESTED_OBJECT_MAPPING.contains("instance.set%s"));
        }
    }

    // ------------------------------------------------------------------ //
    //  MULTI_PARAM_MAPPING_METHOD_TEMPLATE
    // ------------------------------------------------------------------ //

    @Nested
    class MultiParamMappingMethodTemplate {

        @Test
        void isNotNullOrEmpty() {
            assertNotNull(CodeTemplates.MULTI_PARAM_MAPPING_METHOD_TEMPLATE);
            assertFalse(CodeTemplates.MULTI_PARAM_MAPPING_METHOD_TEMPLATE.isEmpty());
        }

        @Test
        void containsOverrideAnnotation() {
            assertTrue(CodeTemplates.MULTI_PARAM_MAPPING_METHOD_TEMPLATE.contains("@Override"));
        }

        @Test
        void containsJavadocForMultipleSources() {
            assertTrue(CodeTemplates.MULTI_PARAM_MAPPING_METHOD_TEMPLATE.contains("multiple sources"));
        }

        @Test
        void containsInstanceCreation() {
            assertTrue(CodeTemplates.MULTI_PARAM_MAPPING_METHOD_TEMPLATE.contains("instance = new"));
        }
    }

    // ------------------------------------------------------------------ //
    //  NULL_SAFE_SETTER
    // ------------------------------------------------------------------ //

    @Nested
    class NullSafeSetter {

        @Test
        void containsNullCheckAndSetter() {
            assertTrue(CodeTemplates.NULL_SAFE_SETTER.contains(".get%s() != null"));
            assertTrue(CodeTemplates.NULL_SAFE_SETTER.contains("instance.set%s("));
        }

        @Test
        void canBeFormattedCorrectly() {
            String result = String.format(CodeTemplates.NULL_SAFE_SETTER,
                    "src", "Email", "Email", "src", "Email");

            assertTrue(result.contains("src.getEmail() != null"));
            assertTrue(result.contains("instance.setEmail(src.getEmail())"));
        }
    }
}
