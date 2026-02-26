package vn.com.lcx.processor.model;

import javax.lang.model.element.Element;

/**
 * Represents mapping information between source and target fields.
 * Contains all metadata needed to generate mapping code.
 */
public class FieldMappingInfo {

    private final String sourceFieldName;
    private final String targetFieldName;
    private final String sourceType;
    private final String targetType;
    private final Element sourceElement;
    private final Element targetElement;
    private final MappingType mappingType;
    private final String customCode;

    /**
     * Types of field mapping
     */
    public enum MappingType {
        /**
         * Direct field copy (same or compatible types)
         */
        SIMPLE,
        /**
         * Requires nested mapper call for complex object
         */
        NESTED_OBJECT,
        /**
         * List/Set mapping with element transformation
         */
        COLLECTION,
        /**
         * User-provided custom code
         */
        CUSTOM_CODE,
        /**
         * Explicitly skipped field
         */
        SKIPPED
    }

    public FieldMappingInfo(String sourceFieldName, String targetFieldName,
                            String sourceType, String targetType,
                            Element sourceElement, Element targetElement,
                            MappingType mappingType, String customCode) {
        this.sourceFieldName = sourceFieldName;
        this.targetFieldName = targetFieldName;
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.sourceElement = sourceElement;
        this.targetElement = targetElement;
        this.mappingType = mappingType;
        this.customCode = customCode;
    }

    public String getSourceFieldName() {
        return sourceFieldName;
    }

    public String getTargetFieldName() {
        return targetFieldName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getTargetType() {
        return targetType;
    }

    public Element getSourceElement() {
        return sourceElement;
    }

    public Element getTargetElement() {
        return targetElement;
    }

    public MappingType getMappingType() {
        return mappingType;
    }

    public String getCustomCode() {
        return customCode;
    }

    public boolean isSkipped() {
        return mappingType == MappingType.SKIPPED;
    }

    public boolean requiresNestedMapping() {
        return mappingType == MappingType.NESTED_OBJECT || mappingType == MappingType.COLLECTION;
    }

    public boolean hasCustomCode() {
        return mappingType == MappingType.CUSTOM_CODE && customCode != null && !customCode.isBlank();
    }

    /**
     * Builder for creating FieldMappingInfo instances
     */
    public static class Builder {
        private String sourceFieldName;
        private String targetFieldName;
        private String sourceType;
        private String targetType;
        private Element sourceElement;
        private Element targetElement;
        private MappingType mappingType = MappingType.SIMPLE;
        private String customCode;

        public Builder sourceFieldName(String sourceFieldName) {
            this.sourceFieldName = sourceFieldName;
            return this;
        }

        public Builder targetFieldName(String targetFieldName) {
            this.targetFieldName = targetFieldName;
            return this;
        }

        public Builder sourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public Builder targetType(String targetType) {
            this.targetType = targetType;
            return this;
        }

        public Builder sourceElement(Element sourceElement) {
            this.sourceElement = sourceElement;
            return this;
        }

        public Builder targetElement(Element targetElement) {
            this.targetElement = targetElement;
            return this;
        }

        public Builder mappingType(MappingType mappingType) {
            this.mappingType = mappingType;
            return this;
        }

        public Builder customCode(String customCode) {
            this.customCode = customCode;
            return this;
        }

        public FieldMappingInfo build() {
            return new FieldMappingInfo(
                    sourceFieldName, targetFieldName,
                    sourceType, targetType,
                    sourceElement, targetElement,
                    mappingType, customCode
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return String.format("FieldMappingInfo{%s -> %s, type=%s}", sourceFieldName, targetFieldName, mappingType);
    }
}
