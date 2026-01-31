package vn.com.lcx.processor.service;

import vn.com.lcx.processor.model.FieldMappingInfo;
import vn.com.lcx.processor.model.FieldMappingInfo.MappingType;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves field mappings between source and target classes.
 * Determines the appropriate mapping type for each field pair.
 */
public class FieldMappingResolver {

    private final Types typeUtils;

    private static final Set<String> COLLECTION_TYPES = Set.of(
            "java.util.List",
            "java.util.Set",
            "java.util.Collection",
            "java.util.ArrayList",
            "java.util.HashSet",
            "java.util.LinkedList",
            "java.util.LinkedHashSet"
    );

    private static final Set<String> PRIMITIVE_WRAPPER_TYPES = Set.of(
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Boolean",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Character",
            "java.math.BigDecimal",
            "java.math.BigInteger"
    );

    public FieldMappingResolver(Types typeUtils) {
        this.typeUtils = typeUtils;
    }

    /**
     * Resolves all field mappings between source and target types.
     * Matches fields by name and determines appropriate mapping type.
     *
     * @param sourceFields list of source class fields
     * @param targetFields list of target class fields
     * @return list of resolved field mappings
     */
    public List<FieldMappingInfo> resolveFieldMappings(
            List<Element> sourceFields,
            List<Element> targetFields) {

        List<FieldMappingInfo> mappings = new ArrayList<>();
        Map<String, Element> sourceFieldMap = buildFieldMap(sourceFields);

        for (Element targetField : targetFields) {
            if (!isValidField(targetField)) {
                continue;
            }

            String targetName = targetField.getSimpleName().toString();
            String targetType = targetField.asType().toString();

            Element sourceField = sourceFieldMap.get(targetName);
            if (sourceField == null) {
                continue; // No matching source field
            }

            String sourceType = sourceField.asType().toString();
            MappingType mappingType = determineMappingType(sourceField, targetField);

            FieldMappingInfo mappingInfo = FieldMappingInfo.builder()
                    .sourceFieldName(targetName)
                    .targetFieldName(targetName)
                    .sourceType(sourceType)
                    .targetType(targetType)
                    .sourceElement(sourceField)
                    .targetElement(targetField)
                    .mappingType(mappingType)
                    .build();

            mappings.add(mappingInfo);
        }

        return mappings;
    }

    /**
     * Resolves a single field mapping with custom configuration
     */
    public FieldMappingInfo resolveCustomMapping(
            String sourceFieldName,
            String targetFieldName,
            Element sourceField,
            Element targetField,
            String customCode,
            boolean skip) {

        if (skip) {
            return FieldMappingInfo.builder()
                    .sourceFieldName(sourceFieldName)
                    .targetFieldName(targetFieldName)
                    .mappingType(MappingType.SKIPPED)
                    .build();
        }

        if (customCode != null && !customCode.isBlank()) {
            return FieldMappingInfo.builder()
                    .sourceFieldName(sourceFieldName)
                    .targetFieldName(targetFieldName)
                    .mappingType(MappingType.CUSTOM_CODE)
                    .customCode(customCode)
                    .build();
        }

        MappingType mappingType = MappingType.SIMPLE;
        if (sourceField != null && targetField != null) {
            mappingType = determineMappingType(sourceField, targetField);
        }

        return FieldMappingInfo.builder()
                .sourceFieldName(sourceFieldName)
                .targetFieldName(targetFieldName)
                .sourceType(sourceField != null ? sourceField.asType().toString() : null)
                .targetType(targetField != null ? targetField.asType().toString() : null)
                .sourceElement(sourceField)
                .targetElement(targetField)
                .mappingType(mappingType)
                .build();
    }

    private Map<String, Element> buildFieldMap(List<Element> fields) {
        Map<String, Element> map = new HashMap<>();
        for (Element field : fields) {
            if (isValidField(field)) {
                map.put(field.getSimpleName().toString(), field);
            }
        }
        return map;
    }

    /**
     * Checks if a field is valid for mapping (not static, not final)
     */
    public boolean isValidField(Element field) {
        Set<Modifier> modifiers = field.getModifiers();
        return !modifiers.contains(Modifier.FINAL) && !modifiers.contains(Modifier.STATIC);
    }

    /**
     * Determines the mapping type based on source and target field types
     */
    public MappingType determineMappingType(Element sourceField, Element targetField) {
        TypeMirror sourceType = sourceField.asType();
        TypeMirror targetType = targetField.asType();

        // Check if types are directly assignable (same type or compatible)
        if (typeUtils.isSameType(sourceType, targetType)) {
            return MappingType.SIMPLE;
        }

        // Check if types are assignable (inheritance/interface)
        if (typeUtils.isAssignable(sourceType, targetType)) {
            return MappingType.SIMPLE;
        }

        // Check for primitive types
        if (sourceType.getKind().isPrimitive() || targetType.getKind().isPrimitive()) {
            return MappingType.SIMPLE;
        }

        // Check for wrapper/string types
        String sourceTypeName = getBaseTypeName(sourceType);
        String targetTypeName = getBaseTypeName(targetType);

        if (PRIMITIVE_WRAPPER_TYPES.contains(sourceTypeName) ||
                PRIMITIVE_WRAPPER_TYPES.contains(targetTypeName)) {
            return MappingType.SIMPLE;
        }

        // Check for collection types
        if (isCollectionType(sourceType) && isCollectionType(targetType)) {
            return MappingType.COLLECTION;
        }

        // Check for complex object types (potential nested mapping)
        if (isDeclaredType(sourceType) && isDeclaredType(targetType)) {
            return MappingType.NESTED_OBJECT;
        }

        return MappingType.SIMPLE;
    }

    private String getBaseTypeName(TypeMirror type) {
        if (type instanceof DeclaredType declaredType) {
            TypeElement element = (TypeElement) declaredType.asElement();
            return element.getQualifiedName().toString();
        }
        return type.toString();
    }

    /**
     * Checks if the given type is a collection type
     */
    public boolean isCollectionType(TypeMirror type) {
        if (!(type instanceof DeclaredType declaredType)) {
            return false;
        }

        TypeElement element = (TypeElement) declaredType.asElement();
        String typeName = element.getQualifiedName().toString();

        // Direct check
        if (COLLECTION_TYPES.contains(typeName)) {
            return true;
        }

        // Check interfaces
        for (TypeMirror iface : element.getInterfaces()) {
            if (isCollectionType(iface)) {
                return true;
            }
        }

        // Check superclass
        TypeMirror superclass = element.getSuperclass();
        if (superclass.getKind() != TypeKind.NONE) {
            return isCollectionType(superclass);
        }

        return false;
    }

    /**
     * Gets the generic type argument of a collection
     */
    public TypeMirror getCollectionElementType(TypeMirror collectionType) {
        if (!(collectionType instanceof DeclaredType declaredType)) {
            return null;
        }

        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.isEmpty()) {
            return null;
        }

        return typeArguments.get(0);
    }

    private boolean isDeclaredType(TypeMirror type) {
        return type instanceof DeclaredType;
    }

    /**
     * Checks if two types are compatible for simple mapping
     */
    public boolean areTypesCompatible(TypeMirror source, TypeMirror target) {
        return typeUtils.isSameType(source, target) || typeUtils.isAssignable(source, target);
    }
}
