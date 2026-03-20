package vn.io.lcx.processor.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vn.io.lcx.processor.model.FieldMappingInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link MappingCodeGenerator}.
 * <p>
 * MappingCodeGenerator produces Java source code strings from FieldMappingInfo
 * instances. It generates setter lines, null-check setters, and merge code
 * using CodeTemplates and WordCaseUtils for field name conversion.
 */
class MappingCodeGeneratorTest {

    // ------------------------------------------------------------------ //
    //  generateSingleMappingCode - SIMPLE
    // ------------------------------------------------------------------ //

    @Nested
    class GenerateSingleMappingCodeSimple {

        @Test
        void generatesSetterLineForSimpleMapping() {
            MappingCodeGenerator generator = new MappingCodeGenerator("source");
            FieldMappingInfo mapping = FieldMappingInfo.builder()
                    .sourceFieldName("name")
                    .targetFieldName("name")
                    .mappingType(FieldMappingInfo.MappingType.SIMPLE)
                    .build();

            String code = generator.generateSingleMappingCode(mapping);

            assertNotNull(code);
            assertTrue(code.contains("instance.set"));
            assertTrue(code.contains("source.get"));
        }

        @Test
        void handlesFieldNameWithPascalCaseConversion() {
            MappingCodeGenerator generator = new MappingCodeGenerator("dto");
            FieldMappingInfo mapping = FieldMappingInfo.builder()
                    .sourceFieldName("firstName")
                    .targetFieldName("firstName")
                    .mappingType(FieldMappingInfo.MappingType.SIMPLE)
                    .build();

            String code = generator.generateSingleMappingCode(mapping);

            assertNotNull(code);
            // WordCaseUtils.toPascalCase(fromCamelCase("firstName")) -> "FirstName"
            assertTrue(code.contains("setFirstName"));
            assertTrue(code.contains("dto.getFirstName()"));
        }

        @Test
        void usesProvidedSourceParamName() {
            MappingCodeGenerator generator = new MappingCodeGenerator("entity");
            FieldMappingInfo mapping = FieldMappingInfo.builder()
                    .sourceFieldName("id")
                    .targetFieldName("id")
                    .mappingType(FieldMappingInfo.MappingType.SIMPLE)
                    .build();

            String code = generator.generateSingleMappingCode(mapping);

            assertTrue(code.contains("entity.get"));
        }
    }

    // ------------------------------------------------------------------ //
    //  generateSingleMappingCode - SKIPPED
    // ------------------------------------------------------------------ //

    @Nested
    class GenerateSingleMappingCodeSkipped {

        @Test
        void returnsEmptyStringForSkippedMapping() {
            MappingCodeGenerator generator = new MappingCodeGenerator("src");
            FieldMappingInfo mapping = FieldMappingInfo.builder()
                    .sourceFieldName("password")
                    .targetFieldName("password")
                    .mappingType(FieldMappingInfo.MappingType.SKIPPED)
                    .build();

            String code = generator.generateSingleMappingCode(mapping);

            assertEquals("", code);
        }
    }

    // ------------------------------------------------------------------ //
    //  generateSingleMappingCode - CUSTOM_CODE
    // ------------------------------------------------------------------ //

    @Nested
    class GenerateSingleMappingCodeCustom {

        @Test
        void generatesCustomCodeSetter() {
            MappingCodeGenerator generator = new MappingCodeGenerator("src");
            FieldMappingInfo mapping = FieldMappingInfo.builder()
                    .sourceFieldName("status")
                    .targetFieldName("statusCode")
                    .mappingType(FieldMappingInfo.MappingType.CUSTOM_CODE)
                    .customCode("src.getStatus().getCode()")
                    .build();

            String code = generator.generateSingleMappingCode(mapping);

            assertNotNull(code);
            assertTrue(code.contains("instance.set"));
            assertTrue(code.contains("src.getStatus().getCode()"));
        }
    }

    // ------------------------------------------------------------------ //
    //  generateMappingCode (list of mappings)
    // ------------------------------------------------------------------ //

    @Nested
    class GenerateMappingCode {

        @Test
        void generatesCodeForAllNonSkippedMappings() {
            MappingCodeGenerator generator = new MappingCodeGenerator("src");
            List<FieldMappingInfo> mappings = Arrays.asList(
                    FieldMappingInfo.builder()
                            .sourceFieldName("name")
                            .targetFieldName("name")
                            .mappingType(FieldMappingInfo.MappingType.SIMPLE)
                            .build(),
                    FieldMappingInfo.builder()
                            .sourceFieldName("password")
                            .targetFieldName("password")
                            .mappingType(FieldMappingInfo.MappingType.SKIPPED)
                            .build(),
                    FieldMappingInfo.builder()
                            .sourceFieldName("email")
                            .targetFieldName("email")
                            .mappingType(FieldMappingInfo.MappingType.SIMPLE)
                            .build()
            );

            List<String> codeLines = generator.generateMappingCode(mappings);

            // Skipped mapping should be excluded
            assertEquals(2, codeLines.size());
        }

        @Test
        void returnsEmptyListForAllSkippedMappings() {
            MappingCodeGenerator generator = new MappingCodeGenerator("src");
            List<FieldMappingInfo> mappings = Collections.singletonList(
                    FieldMappingInfo.builder()
                            .sourceFieldName("secret")
                            .targetFieldName("secret")
                            .mappingType(FieldMappingInfo.MappingType.SKIPPED)
                            .build()
            );

            List<String> codeLines = generator.generateMappingCode(mappings);

            assertTrue(codeLines.isEmpty());
        }

        @Test
        void returnsEmptyListForEmptyMappings() {
            MappingCodeGenerator generator = new MappingCodeGenerator("src");

            List<String> codeLines = generator.generateMappingCode(Collections.emptyList());

            assertNotNull(codeLines);
            assertTrue(codeLines.isEmpty());
        }
    }

    // ------------------------------------------------------------------ //
    //  generateMergingCode
    // ------------------------------------------------------------------ //

    @Nested
    class GenerateMergingCode {

        @Test
        void generatesDirectMergeSetterWhenMergeNonNullIsTrue() {
            MappingCodeGenerator generator = new MappingCodeGenerator("src");

            String code = generator.generateMergingCode("name", "target", "source", true);

            assertNotNull(code);
            assertTrue(code.contains("target.set"));
            assertTrue(code.contains("source.get"));
            // Direct merge should NOT have null check
            assertFalse(code.contains("== null"));
        }

        @Test
        void generatesNullCheckSetterWhenMergeNonNullIsFalse() {
            MappingCodeGenerator generator = new MappingCodeGenerator("src");

            String code = generator.generateMergingCode("name", "target", "source", false);

            assertNotNull(code);
            // Null check merge should check if target field is null
            assertTrue(code.contains("target.get"));
            assertTrue(code.contains("== null"));
            assertTrue(code.contains("target.set"));
            assertTrue(code.contains("source.get"));
        }

        @Test
        void convertsFieldNameToPascalCase() {
            MappingCodeGenerator generator = new MappingCodeGenerator("dto");

            String code = generator.generateMergingCode("firstName", "a", "b", true);

            assertTrue(code.contains("setFirstName"));
            assertTrue(code.contains("getFirstName"));
        }

        @Test
        void usesCorrectParameterNamesInDirectMerge() {
            MappingCodeGenerator generator = new MappingCodeGenerator("src");

            String code = generator.generateMergingCode("email", "existing", "incoming", true);

            assertTrue(code.contains("existing.setEmail(incoming.getEmail())"));
        }

        @Test
        void usesCorrectParameterNamesInNullCheckMerge() {
            MappingCodeGenerator generator = new MappingCodeGenerator("src");

            String code = generator.generateMergingCode("email", "existing", "incoming", false);

            assertTrue(code.contains("existing.getEmail() == null"));
            assertTrue(code.contains("existing.setEmail(incoming.getEmail())"));
        }
    }

    // ------------------------------------------------------------------ //
    //  generateSingleMappingCode - COLLECTION and NESTED_OBJECT
    // ------------------------------------------------------------------ //

    @Nested
    class GenerateSingleMappingCodeCollectionAndNested {

        @Test
        void collectionMappingFallsBackToSimpleMapping() {
            // Current implementation generates simple mapping for collections
            MappingCodeGenerator generator = new MappingCodeGenerator("src");
            FieldMappingInfo mapping = FieldMappingInfo.builder()
                    .sourceFieldName("items")
                    .targetFieldName("items")
                    .mappingType(FieldMappingInfo.MappingType.COLLECTION)
                    .build();

            String code = generator.generateSingleMappingCode(mapping);

            assertNotNull(code);
            assertFalse(code.isEmpty());
            assertTrue(code.contains("instance.set"));
            assertTrue(code.contains("src.get"));
        }

        @Test
        void nestedObjectMappingFallsBackToSimpleMapping() {
            // Current implementation generates simple mapping for nested objects
            MappingCodeGenerator generator = new MappingCodeGenerator("src");
            FieldMappingInfo mapping = FieldMappingInfo.builder()
                    .sourceFieldName("address")
                    .targetFieldName("address")
                    .mappingType(FieldMappingInfo.MappingType.NESTED_OBJECT)
                    .build();

            String code = generator.generateSingleMappingCode(mapping);

            assertNotNull(code);
            assertFalse(code.isEmpty());
            assertTrue(code.contains("instance.set"));
            assertTrue(code.contains("src.get"));
        }
    }
}
