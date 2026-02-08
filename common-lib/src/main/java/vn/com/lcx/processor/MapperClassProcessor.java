package vn.com.lcx.processor;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.mapper.MapperClass;
import vn.com.lcx.common.annotation.mapper.Mapping;
import vn.com.lcx.common.annotation.mapper.Merging;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.common.utils.WordCaseUtils;
import vn.com.lcx.processor.exception.InvalidMappingException;
import vn.com.lcx.processor.exception.MapperProcessingException;
import vn.com.lcx.processor.model.FieldMappingInfo;
import vn.com.lcx.processor.model.SourceParameterInfo;
import vn.com.lcx.processor.service.FieldMappingResolver;
import vn.com.lcx.processor.service.MappingCodeGenerator;
import vn.com.lcx.processor.template.CodeTemplates;
import vn.com.lcx.processor.utility.TypeHierarchyAnalyzer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Annotation processor for generating mapper implementation classes.
 * Processes interfaces annotated with {@link MapperClass} and generates
 * implementation classes with mapping/merging methods.
 */
@SupportedAnnotationTypes("vn.com.lcx.common.annotation.mapper.MapperClass")
public class MapperClassProcessor extends AbstractProcessor {

    private static final Set<String> EXCLUDED_METHODS = Set.of("hashcode", "equals", "tostring");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private FieldMappingResolver fieldMappingResolver;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.fieldMappingResolver = new FieldMappingResolver(processingEnv.getTypeUtils());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(MapperClass.class)) {
            if (annotatedElement instanceof TypeElement typeElement) {
                try {
                    processMapperClass(typeElement);
                } catch (MapperProcessingException e) {
                    printError(e.getMessage(), annotatedElement);
                } catch (IOException e) {
                    printError("Failed to generate mapper: " + e.getMessage(), annotatedElement);
                } catch (Exception e) {
                    printError("Unexpected error: " + e.getMessage(), annotatedElement);
                }
            }
        }
        return true;
    }

    /**
     * Processes a single mapper class and generates its implementation
     */
    public void processMapperClass(TypeElement typeElement) throws IOException {
        logProcessingNote(typeElement);

        List<ExecutableElement> allMethods = extractMappableMethods(typeElement);
        List<String> methodImplementations = new ArrayList<>();

        for (ExecutableElement method : allMethods) {
            String methodCode = processMethod(method);
            methodImplementations.add(methodCode);
        }

        generateMapperClass(typeElement, methodImplementations);
    }

    /**
     * Extracts all methods that should be implemented
     */
    private List<ExecutableElement> extractMappableMethods(TypeElement typeElement) {
        return processingEnv.getElementUtils().getAllMembers(typeElement).stream()
                .filter(this::isMappableMethod)
                .map(member -> (ExecutableElement) member)
                .toList();
    }

    /**
     * Checks if an element is a mappable method
     */
    private boolean isMappableMethod(Element element) {
        if (element.getKind() != ElementKind.METHOD) {
            return false;
        }

        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.FINAL) || modifiers.contains(Modifier.STATIC)) {
            return false;
        }

        String methodName = element.getSimpleName().toString().toLowerCase();
        return !EXCLUDED_METHODS.contains(methodName);
    }

    /**
     * Processes a single method and returns its implementation code
     */
    private String processMethod(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        String returnType = method.getReturnType().toString();
        List<? extends VariableElement> parameters = method.getParameters();

        validateParameters(methodName, parameters);

        String firstParamType = parameters.get(0).asType().toString();
        String firstParamName = parameters.get(0).getSimpleName().toString();

        Merging merging = method.getAnnotation(Merging.class);

        if (merging != null) {
            validateMergingParameters(methodName, parameters);

            String secondParamType = parameters.get(1).asType().toString();
            String secondParamName = parameters.get(1).getSimpleName().toString();

            return buildMergingCode(
                    methodName, returnType,
                    firstParamType, firstParamName,
                    secondParamType, secondParamName,
                    merging.mergeNonNullField()
            );
        } else if (parameters.size() == 1) {
            return buildMappingCode(method, methodName, returnType, firstParamType, firstParamName);
        } else {
            List<SourceParameterInfo> sourceParams = new ArrayList<>();
            for (VariableElement param : parameters) {
                String paramType = param.asType().toString();
                String paramName = param.getSimpleName().toString();
                TypeElement paramTypeElement = processingEnv.getElementUtils().getTypeElement(paramType);
                List<Element> fields = getValidFields(paramTypeElement);
                sourceParams.add(new SourceParameterInfo(paramName, paramType, fields));
            }
            return buildMultiParamMappingCode(method, methodName, returnType, sourceParams);
        }
    }

    /**
     * Validates method parameters count
     */
    private void validateParameters(String methodName, List<? extends VariableElement> parameters) {
        if (parameters.isEmpty()) {
            throw InvalidMappingException.noParameters(methodName);
        }
    }

    /**
     * Validates merging method has exactly 2 parameters
     */
    private void validateMergingParameters(String methodName, List<? extends VariableElement> parameters) {
        if (parameters.size() != 2) {
            throw InvalidMappingException.invalidParameterCount(methodName, 2, parameters.size());
        }
    }

    /**
     * Builds merging method implementation code
     */
    public String buildMergingCode(String methodName,
                                   String returnType,
                                   String firstParamType,
                                   String firstParamName,
                                   String secondParamType,
                                   String secondParamName,
                                   boolean mergeNonNullField) {

        if (!firstParamType.equals(secondParamType)) {
            throw InvalidMappingException.mergingDifferentClasses(methodName, firstParamType, secondParamType);
        }

        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(firstParamType);
        List<Element> fields = new ArrayList<>(TypeHierarchyAnalyzer.getAllFields(processingEnv.getTypeUtils(), typeElement));

        // Filter valid fields
        List<Element> validFields = fields.stream()
                .filter(fieldMappingResolver::isValidField)
                .toList();

        MappingCodeGenerator codeGenerator = new MappingCodeGenerator(firstParamName);
        List<String> mergingLines = new ArrayList<>();

        for (Element field : validFields) {
            String fieldName = field.getSimpleName().toString();
            String code = codeGenerator.generateMergingCode(fieldName, firstParamName, secondParamName, mergeNonNullField);
            mergingLines.add(code);
        }

        String nullReturnStatement = "void".equals(returnType) ? "return;" : "return null;";
        String returnStatement = "void".equals(returnType) ? "" : String.format("return %s;", firstParamName);

        return String.format(CodeTemplates.MERGING_METHOD_TEMPLATE,
                secondParamName, firstParamName,
                returnType, methodName,
                firstParamType, firstParamName,
                secondParamType, secondParamName,
                firstParamName, secondParamName,
                nullReturnStatement,
                String.join("", mergingLines),
                returnStatement
        );
    }

    /**
     * Builds mapping method implementation code
     */
    public String buildMappingCode(ExecutableElement method,
                                   String methodName,
                                   String returnType,
                                   String sourceType,
                                   String sourceParamName) {

        List<Mapping> mappingAnnotations = new ArrayList<>(Arrays.asList(method.getAnnotationsByType(Mapping.class)));

        TypeElement sourceTypeElement = processingEnv.getElementUtils().getTypeElement(sourceType);
        TypeElement targetTypeElement = processingEnv.getElementUtils().getTypeElement(returnType);

        List<Element> sourceFields = getValidFields(sourceTypeElement);
        List<Element> targetFields = getValidFields(targetTypeElement);

        List<String> mappingLines = buildMappingLines(
                mappingAnnotations, sourceFields, targetFields, sourceParamName
        );

        return String.format(CodeTemplates.MAPPING_METHOD_TEMPLATE,
                CodeTemplates.getSimpleClassName(sourceType),
                CodeTemplates.getSimpleClassName(returnType),
                sourceParamName,
                CodeTemplates.getSimpleClassName(returnType),
                returnType, methodName, sourceType, sourceParamName,
                sourceParamName,
                returnType, returnType,
                String.join("", mappingLines)
        );
    }

    /**
     * Builds mapping method implementation code for methods with multiple source parameters
     */
    private String buildMultiParamMappingCode(ExecutableElement method,
                                               String methodName,
                                               String returnType,
                                               List<SourceParameterInfo> sourceParams) {

        List<Mapping> mappingAnnotations = new ArrayList<>(Arrays.asList(method.getAnnotationsByType(Mapping.class)));

        TypeElement targetTypeElement = processingEnv.getElementUtils().getTypeElement(returnType);
        List<Element> targetFields = getValidFields(targetTypeElement);

        // Validate all fromParameter references
        Set<String> paramNames = sourceParams.stream()
                .map(SourceParameterInfo::getParamName)
                .collect(Collectors.toSet());
        for (Mapping mapping : mappingAnnotations) {
            String fp = mapping.fromParameter();
            if (!fp.isEmpty() && !paramNames.contains(fp)) {
                throw InvalidMappingException.unknownFromParameter(
                        methodName, fp, sourceParams.stream()
                                .map(SourceParameterInfo::getParamName).toList()
                );
            }
        }

        List<String> mappingLines = buildMultiParamMappingLines(
                mappingAnnotations, sourceParams, targetFields
        );

        String returnTypeSimple = CodeTemplates.getSimpleClassName(returnType);

        String javadocParams = sourceParams.stream()
                .map(sp -> String.format("             * @param %s source %s",
                        sp.getParamName(), CodeTemplates.getSimpleClassName(sp.getParamType())))
                .collect(Collectors.joining("\n"));

        String paramSignature = sourceParams.stream()
                .map(sp -> sp.getParamType() + " " + sp.getParamName())
                .collect(Collectors.joining(", "));

        String nullChecks = sourceParams.stream()
                .map(sp -> sp.getParamName() + " == null")
                .collect(Collectors.joining(" || "));

        return String.format(CodeTemplates.MULTI_PARAM_MAPPING_METHOD_TEMPLATE,
                returnTypeSimple,
                javadocParams,
                returnTypeSimple,
                returnType, methodName, paramSignature,
                nullChecks,
                returnType, returnType,
                String.join("", mappingLines)
        );
    }

    /**
     * Builds mapping code lines for multi-parameter methods.
     * Processes explicit @Mapping annotations first, then auto-matches
     * remaining target fields across all source parameters (first param has priority).
     */
    private List<String> buildMultiParamMappingLines(List<Mapping> annotations,
                                                      List<SourceParameterInfo> sourceParams,
                                                      List<Element> targetFields) {

        List<String> mappingLines = new ArrayList<>();
        Set<String> handledFields = new HashSet<>();

        // Phase 1: Process explicit @Mapping annotations
        for (Mapping mapping : annotations) {
            String toField = toPascalCase(mapping.toField());

            if (mapping.skip()) {
                handledFields.add(toField);
                continue;
            }

            if (StringUtils.isNotBlank(mapping.code())) {
                String code = String.format("\n        instance.set%s(%s);", toField, mapping.code());
                mappingLines.add(code);
                handledFields.add(toField);
                continue;
            }

            String fromField = toPascalCase(mapping.fromField());
            String sourceParamName;

            if (StringUtils.isNotBlank(mapping.fromParameter())) {
                sourceParamName = mapping.fromParameter();
            } else {
                sourceParamName = sourceParams.get(0).getParamName();
            }

            String code = String.format(CodeTemplates.SETTER_LINE, toField, sourceParamName, fromField);
            mappingLines.add(code);
            handledFields.add(toField);
        }

        // Phase 2: Auto-match remaining target fields across source parameters (first param has priority)
        for (Element targetField : targetFields) {
            String targetFieldName = toPascalCase(targetField.getSimpleName().toString());

            if (handledFields.contains(targetFieldName)) {
                continue;
            }

            String targetFieldType = targetField.asType().toString();

            for (SourceParameterInfo sp : sourceParams) {
                Element sourceField = findMatchingSourceField(
                        sp.getFields(),
                        targetField.getSimpleName().toString(),
                        targetFieldType
                );

                if (sourceField != null) {
                    String sourceFieldName = toPascalCase(sourceField.getSimpleName().toString());
                    if (sourceFieldName.equals(targetFieldName)) {
                        String code = String.format(CodeTemplates.SETTER_LINE,
                                targetFieldName, sp.getParamName(), sourceFieldName);
                        mappingLines.add(code);
                        handledFields.add(targetFieldName);
                        break;
                    }
                }
            }
        }

        return mappingLines;
    }

    /**
     * Gets valid (non-static, non-final) fields from a type
     */
    private List<Element> getValidFields(TypeElement typeElement) {
        return TypeHierarchyAnalyzer.getAllFields(processingEnv.getTypeUtils(), typeElement)
                .stream()
                .filter(fieldMappingResolver::isValidField)
                .collect(Collectors.toList());
    }

    /**
     * Builds mapping code lines based on annotations and field matching
     */
    private List<String> buildMappingLines(List<Mapping> annotations,
                                           List<Element> sourceFields,
                                           List<Element> targetFields,
                                           String sourceParamName) {

        MappingCodeGenerator codeGenerator = new MappingCodeGenerator(sourceParamName);
        List<String> mappingLines = new ArrayList<>();
        Set<String> handledFields = new HashSet<>();

        // Process explicit mappings first
        if (!annotations.isEmpty()) {
            for (Mapping mapping : annotations) {
                String toField = toPascalCase(mapping.toField());

                if (mapping.skip()) {
                    handledFields.add(toField);
                    continue;
                }

                String code;
                if (StringUtils.isNotBlank(mapping.code())) {
                    code = String.format("\n        instance.set%s(%s);", toField, mapping.code());
                } else {
                    String fromField = toPascalCase(mapping.fromField());
                    code = String.format(CodeTemplates.SETTER_LINE, toField, sourceParamName, fromField);
                }

                mappingLines.add(code);
                handledFields.add(toField);
            }
        }

        // Process remaining fields by name matching
        for (Element targetField : targetFields) {
            String targetFieldName = toPascalCase(targetField.getSimpleName().toString());

            if (handledFields.contains(targetFieldName)) {
                continue;
            }

            String targetFieldType = targetField.asType().toString();
            Element sourceField = findMatchingSourceField(sourceFields, targetField.getSimpleName().toString(), targetFieldType);

            if (sourceField == null) {
                continue;
            }

            String sourceFieldName = toPascalCase(sourceField.getSimpleName().toString());
            if (sourceFieldName.equals(targetFieldName)) {
                String code = String.format(CodeTemplates.SETTER_LINE, targetFieldName, sourceParamName, sourceFieldName);
                mappingLines.add(code);
            }
        }

        return mappingLines;
    }

    /**
     * Finds a matching source field by name and type
     */
    private Element findMatchingSourceField(List<Element> sourceFields, String fieldName, String fieldType) {
        return sourceFields.stream()
                .filter(field -> field.getSimpleName().toString().equals(fieldName)
                        && field.asType().toString().equals(fieldType))
                .findFirst()
                .orElse(null);
    }

    /**
     * Generates the mapper implementation class file
     */
    private void generateMapperClass(TypeElement typeElement, List<String> methodImplementations) throws IOException {
        String className = typeElement.getSimpleName() + "Impl";
        String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
        String fullClassName = packageName + "." + className;
        String generatedDate = LocalDateTime.now().format(DATE_FORMATTER);

        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(fullClassName);
        try (Writer writer = builderFile.openWriter()) {
            String code = String.format(CodeTemplates.CLASS_TEMPLATE,
                    packageName,
                    generatedDate,
                    className,
                    typeElement.getSimpleName().toString(),
                    className,
                    String.join("", methodImplementations)
            );
            writer.write(code);
        }
    }

    /**
     * Converts field name to PascalCase for getter/setter
     */
    private String toPascalCase(String fieldName) {
        return WordCaseUtils.toPascalCase(WordCaseUtils.fromCamelCase(fieldName));
    }

    /**
     * Logs a processing note with timestamp
     */
    private void logProcessingNote(TypeElement typeElement) {
        String timestamp = String.valueOf(DateTimeUtils.toUnixMil(DateTimeUtils.generateCurrentTimeDefault()));
        String message = String.format("%s: Generating code for mapper class: %s",
                timestamp, typeElement.getQualifiedName());
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    /**
     * Prints an error message
     */
    private void printError(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
