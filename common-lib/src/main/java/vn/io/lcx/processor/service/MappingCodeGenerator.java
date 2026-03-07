package vn.io.lcx.processor.service;

import vn.io.lcx.common.utils.WordCaseUtils;
import vn.io.lcx.processor.model.FieldMappingInfo;
import vn.io.lcx.processor.model.FieldMappingInfo.MappingType;
import vn.io.lcx.processor.template.CodeTemplates;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates mapping code from FieldMappingInfo.
 * Handles different mapping types: simple, collection, nested, custom.
 */
public class MappingCodeGenerator {

    private final String sourceParamName;

    public MappingCodeGenerator(String sourceParamName) {
        this.sourceParamName = sourceParamName;
    }

    /**
     * Generates all mapping code lines for the given mappings
     */
    public List<String> generateMappingCode(List<FieldMappingInfo> mappings) {
        List<String> codeLines = new ArrayList<>();

        for (FieldMappingInfo mapping : mappings) {
            if (mapping.isSkipped()) {
                continue;
            }

            String code = generateSingleMappingCode(mapping);
            if (code != null && !code.isEmpty()) {
                codeLines.add(code);
            }
        }

        return codeLines;
    }

    /**
     * Generates code for a single field mapping
     */
    public String generateSingleMappingCode(FieldMappingInfo mapping) {
        if (mapping.isSkipped()) {
            return "";
        }

        String targetField = toPascalCase(mapping.getTargetFieldName());
        String sourceField = toPascalCase(mapping.getSourceFieldName());

        return switch (mapping.getMappingType()) {
            case SIMPLE -> generateSimpleMappingCode(targetField, sourceField);
            case CUSTOM_CODE -> generateCustomMappingCode(targetField, mapping.getCustomCode());
            case COLLECTION -> generateCollectionMappingCode(targetField, sourceField, mapping);
            case NESTED_OBJECT -> generateNestedMappingCode(targetField, sourceField, mapping);
            case SKIPPED -> "";
        };
    }

    private String generateSimpleMappingCode(String targetField, String sourceField) {
        return String.format(CodeTemplates.SETTER_LINE, targetField, sourceParamName, sourceField);
    }

    private String generateCustomMappingCode(String targetField, String customCode) {
        return String.format("\n        instance.set%s(%s);", targetField, customCode);
    }

    private String generateCollectionMappingCode(String targetField, String sourceField, FieldMappingInfo mapping) {
        // For now, generate simple mapping. Collection support can be expanded later
        // to include element-wise mapping with a nested mapper
        return generateSimpleMappingCode(targetField, sourceField);
    }

    private String generateNestedMappingCode(String targetField, String sourceField, FieldMappingInfo mapping) {
        // For now, generate simple mapping. Nested object support can be expanded later
        return generateSimpleMappingCode(targetField, sourceField);
    }

    /**
     * Generates merging code for the given field name
     */
    public String generateMergingCode(String fieldName, String firstParam, String secondParam, boolean mergeNonNull) {
        String pascalFieldName = toPascalCase(fieldName);

        if (mergeNonNull) {
            return String.format(CodeTemplates.MERGE_SETTER,
                    firstParam, pascalFieldName, secondParam, pascalFieldName);
        } else {
            return String.format(CodeTemplates.NULL_CHECK_SETTER,
                    firstParam, pascalFieldName,
                    firstParam, pascalFieldName, secondParam, pascalFieldName);
        }
    }

    /**
     * Converts field name to PascalCase for getter/setter methods
     */
    private String toPascalCase(String fieldName) {
        return WordCaseUtils.toPascalCase(WordCaseUtils.fromCamelCase(fieldName));
    }
}
