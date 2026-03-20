package vn.io.lcx.processor.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link FieldMappingInfo} model class.
 * <p>
 * FieldMappingInfo carries metadata about a single field mapping between source
 * and target classes, including mapping type (SIMPLE, NESTED_OBJECT, COLLECTION,
 * CUSTOM_CODE, SKIPPED) and optional custom code.
 */
class FieldMappingInfoTest {

    // ------------------------------------------------------------------ //
    //  Constructor
    // ------------------------------------------------------------------ //

    @Nested
    class Constructor {

        @Test
        void storesAllFields() {
            FieldMappingInfo info = new FieldMappingInfo(
                    "name", "userName",
                    "java.lang.String", "java.lang.String",
                    null, null,
                    FieldMappingInfo.MappingType.SIMPLE, null);

            assertEquals("name", info.getSourceFieldName());
            assertEquals("userName", info.getTargetFieldName());
            assertEquals("java.lang.String", info.getSourceType());
            assertEquals("java.lang.String", info.getTargetType());
            assertNull(info.getSourceElement());
            assertNull(info.getTargetElement());
            assertEquals(FieldMappingInfo.MappingType.SIMPLE, info.getMappingType());
            assertNull(info.getCustomCode());
        }

        @Test
        void acceptsNullValues() {
            FieldMappingInfo info = new FieldMappingInfo(
                    null, null, null, null, null, null, null, null);

            assertNull(info.getSourceFieldName());
            assertNull(info.getTargetFieldName());
            assertNull(info.getMappingType());
        }
    }

    // ------------------------------------------------------------------ //
    //  Builder
    // ------------------------------------------------------------------ //

    @Nested
    class BuilderTest {

        @Test
        void builderReturnsNonNull() {
            FieldMappingInfo.Builder builder = FieldMappingInfo.builder();

            assertNotNull(builder);
        }

        @Test
        void buildsWithAllFieldsSet() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .sourceFieldName("email")
                    .targetFieldName("emailAddress")
                    .sourceType("java.lang.String")
                    .targetType("java.lang.String")
                    .mappingType(FieldMappingInfo.MappingType.SIMPLE)
                    .customCode(null)
                    .build();

            assertEquals("email", info.getSourceFieldName());
            assertEquals("emailAddress", info.getTargetFieldName());
            assertEquals("java.lang.String", info.getSourceType());
            assertEquals("java.lang.String", info.getTargetType());
            assertEquals(FieldMappingInfo.MappingType.SIMPLE, info.getMappingType());
        }

        @Test
        void defaultMappingTypeIsSimple() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .sourceFieldName("id")
                    .targetFieldName("id")
                    .build();

            assertEquals(FieldMappingInfo.MappingType.SIMPLE, info.getMappingType());
        }

        @Test
        void buildsWithCustomCode() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .sourceFieldName("status")
                    .targetFieldName("statusCode")
                    .mappingType(FieldMappingInfo.MappingType.CUSTOM_CODE)
                    .customCode("source.getStatus().getCode()")
                    .build();

            assertEquals(FieldMappingInfo.MappingType.CUSTOM_CODE, info.getMappingType());
            assertEquals("source.getStatus().getCode()", info.getCustomCode());
        }

        @Test
        void buildsWithSkippedType() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .sourceFieldName("password")
                    .targetFieldName("password")
                    .mappingType(FieldMappingInfo.MappingType.SKIPPED)
                    .build();

            assertEquals(FieldMappingInfo.MappingType.SKIPPED, info.getMappingType());
        }

        @Test
        void buildsWithMinimalFields() {
            FieldMappingInfo info = FieldMappingInfo.builder().build();

            assertNull(info.getSourceFieldName());
            assertNull(info.getTargetFieldName());
            assertEquals(FieldMappingInfo.MappingType.SIMPLE, info.getMappingType());
        }
    }

    // ------------------------------------------------------------------ //
    //  isSkipped
    // ------------------------------------------------------------------ //

    @Nested
    class IsSkipped {

        @Test
        void returnsTrueForSkippedType() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.SKIPPED)
                    .build();

            assertTrue(info.isSkipped());
        }

        @Test
        void returnsFalseForSimpleType() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.SIMPLE)
                    .build();

            assertFalse(info.isSkipped());
        }

        @Test
        void returnsFalseForNestedObjectType() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.NESTED_OBJECT)
                    .build();

            assertFalse(info.isSkipped());
        }

        @Test
        void returnsFalseForCollectionType() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.COLLECTION)
                    .build();

            assertFalse(info.isSkipped());
        }

        @Test
        void returnsFalseForCustomCodeType() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.CUSTOM_CODE)
                    .build();

            assertFalse(info.isSkipped());
        }
    }

    // ------------------------------------------------------------------ //
    //  requiresNestedMapping
    // ------------------------------------------------------------------ //

    @Nested
    class RequiresNestedMapping {

        @Test
        void returnsTrueForNestedObjectType() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.NESTED_OBJECT)
                    .build();

            assertTrue(info.requiresNestedMapping());
        }

        @Test
        void returnsTrueForCollectionType() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.COLLECTION)
                    .build();

            assertTrue(info.requiresNestedMapping());
        }

        @Test
        void returnsFalseForSimpleType() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.SIMPLE)
                    .build();

            assertFalse(info.requiresNestedMapping());
        }

        @Test
        void returnsFalseForSkippedType() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.SKIPPED)
                    .build();

            assertFalse(info.requiresNestedMapping());
        }

        @Test
        void returnsFalseForCustomCodeType() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.CUSTOM_CODE)
                    .build();

            assertFalse(info.requiresNestedMapping());
        }
    }

    // ------------------------------------------------------------------ //
    //  hasCustomCode
    // ------------------------------------------------------------------ //

    @Nested
    class HasCustomCode {

        @Test
        void returnsTrueWhenCustomCodeTypeAndCodePresent() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.CUSTOM_CODE)
                    .customCode("source.getValue()")
                    .build();

            assertTrue(info.hasCustomCode());
        }

        @Test
        void returnsFalseWhenCustomCodeTypeButNullCode() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.CUSTOM_CODE)
                    .customCode(null)
                    .build();

            assertFalse(info.hasCustomCode());
        }

        @Test
        void returnsFalseWhenCustomCodeTypeButEmptyCode() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.CUSTOM_CODE)
                    .customCode("")
                    .build();

            assertFalse(info.hasCustomCode());
        }

        @Test
        void returnsFalseWhenCustomCodeTypeButBlankCode() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.CUSTOM_CODE)
                    .customCode("   ")
                    .build();

            assertFalse(info.hasCustomCode());
        }

        @Test
        void returnsFalseWhenSimpleTypeEvenWithCode() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.SIMPLE)
                    .customCode("source.getValue()")
                    .build();

            assertFalse(info.hasCustomCode());
        }

        @Test
        void returnsFalseForSkippedType() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.SKIPPED)
                    .build();

            assertFalse(info.hasCustomCode());
        }
    }

    // ------------------------------------------------------------------ //
    //  toString
    // ------------------------------------------------------------------ //

    @Nested
    class ToStringTest {

        @Test
        void containsFieldNames() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .sourceFieldName("email")
                    .targetFieldName("userEmail")
                    .mappingType(FieldMappingInfo.MappingType.SIMPLE)
                    .build();

            String result = info.toString();

            assertTrue(result.contains("email"));
            assertTrue(result.contains("userEmail"));
            assertTrue(result.contains("SIMPLE"));
        }

        @Test
        void matchesExpectedFormat() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .sourceFieldName("name")
                    .targetFieldName("fullName")
                    .mappingType(FieldMappingInfo.MappingType.NESTED_OBJECT)
                    .build();

            assertEquals("FieldMappingInfo{name -> fullName, type=NESTED_OBJECT}", info.toString());
        }

        @Test
        void handlesNullFieldNames() {
            FieldMappingInfo info = FieldMappingInfo.builder()
                    .mappingType(FieldMappingInfo.MappingType.SIMPLE)
                    .build();

            String result = info.toString();
            assertNotNull(result);
            assertTrue(result.contains("null -> null"));
        }
    }

    // ------------------------------------------------------------------ //
    //  MappingType enum
    // ------------------------------------------------------------------ //

    @Nested
    class MappingTypeEnum {

        @Test
        void hasFiveValues() {
            assertEquals(5, FieldMappingInfo.MappingType.values().length);
        }

        @Test
        void containsExpectedValues() {
            assertNotNull(FieldMappingInfo.MappingType.valueOf("SIMPLE"));
            assertNotNull(FieldMappingInfo.MappingType.valueOf("NESTED_OBJECT"));
            assertNotNull(FieldMappingInfo.MappingType.valueOf("COLLECTION"));
            assertNotNull(FieldMappingInfo.MappingType.valueOf("CUSTOM_CODE"));
            assertNotNull(FieldMappingInfo.MappingType.valueOf("SKIPPED"));
        }
    }
}
