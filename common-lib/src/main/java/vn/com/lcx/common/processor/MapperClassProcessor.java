package vn.com.lcx.common.processor;

import vn.com.lcx.common.annotation.mapper.MapperClass;
import vn.com.lcx.common.annotation.mapper.Mapping;
import vn.com.lcx.common.annotation.mapper.Merging;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.WordCaseUtils;
import vn.com.lcx.jpa.processor.utility.TypeHierarchyAnalyzer;

import javax.annotation.processing.AbstractProcessor;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("vn.com.lcx.common.annotation.mapper.MapperClass")
public class MapperClassProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(MapperClass.class)) {
            if (annotatedElement instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                try {
                    this.processMapperClass(typeElement);
                } catch (Exception e) {
                    processingEnv.
                            getMessager().
                            printMessage(
                                    Diagnostic.Kind.ERROR,
                                    e.getMessage()
                            );
                }
            }
        }
        return true;
    }

    public void processMapperClass(TypeElement typeElement) throws IOException {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                vn.com.lcx.common.utils.DateTimeUtils.toUnixMil(vn.com.lcx.common.utils.DateTimeUtils.generateCurrentTimeDefault()) + ": " +
                        String.format(
                                "Generating code for mapper class : %s",
                                typeElement.getQualifiedName()
                        )
        );
        List<ExecutableElement> allMethodsOfClass = processingEnv.getElementUtils().getAllMembers(typeElement).stream()
                .filter(e -> {
                    boolean elementIsAMethod = e.getKind() == ElementKind.METHOD;
                    boolean isNotStaticAndFinal = !(e.getModifiers().contains(Modifier.FINAL) || e.getModifiers().contains(Modifier.STATIC));
                    boolean notHashCodeMethod = !"hashCode".equalsIgnoreCase(e.getSimpleName().toString());
                    boolean notEqualsMethod = !"equals".equalsIgnoreCase(e.getSimpleName().toString());
                    boolean notToStringMethod = !"toString".equalsIgnoreCase(e.getSimpleName().toString());
                    return elementIsAMethod && isNotStaticAndFinal && notHashCodeMethod && notEqualsMethod && notToStringMethod;
                })
                .map(member -> (ExecutableElement) member).collect(Collectors.toList());
        List<String> listOfImplementMethodCode = new ArrayList<>();
        for (ExecutableElement methodsOfClass : allMethodsOfClass) {
            String methodName = methodsOfClass.getSimpleName() + CommonConstant.EMPTY_STRING;
            String methodReturnClass = methodsOfClass.getReturnType() + CommonConstant.EMPTY_STRING;
            List<? extends VariableElement> methodParameters = methodsOfClass.getParameters();

            // Currently support mapping for class to class only
            if (methodParameters.isEmpty() || methodParameters.size() > 2) {
                throw new RuntimeException("Method must have 1 or 2 parameter(s)");
            }

            String firstInputParameterClass = methodParameters.get(0).asType() + CommonConstant.EMPTY_STRING;
            String firstInputParameterName = methodParameters.get(0).getSimpleName() + CommonConstant.EMPTY_STRING;

            Merging merging = methodsOfClass.getAnnotation(Merging.class);

            if (merging != null) {
                if (methodParameters.size() != 2) {
                    throw new RuntimeException("Merging 2 parameters");
                }
                String secondInputParameterClass = methodParameters.get(1).asType() + CommonConstant.EMPTY_STRING;
                String secondInputParameterName = methodParameters.get(1).getSimpleName() + CommonConstant.EMPTY_STRING;
                listOfImplementMethodCode.addAll(
                        this.buildMergingCode(
                                methodName,
                                methodReturnClass,
                                firstInputParameterClass,
                                firstInputParameterName,
                                secondInputParameterClass,
                                secondInputParameterName,
                                merging.mergeNonNullField()
                        )
                );
            } else {
                listOfImplementMethodCode.addAll(
                        this.buildMappingCode(
                                methodsOfClass,
                                methodName,
                                methodReturnClass,
                                firstInputParameterClass,
                                firstInputParameterName
                        )
                );
            }
        }
        String className = typeElement.getSimpleName() + "Impl";
        String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
        String fullClassName = packageName + "." + className;
        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(fullClassName);
        try (Writer writer = builderFile.openWriter()) {
            String codeToWrite = String.format(
                    "package %s;\n\nimport java.util.*;\n\npublic class %s implements %s {\n\n    public %s() {\n    }\n    %s\n\n}",
                    packageName,
                    className,
                    typeElement.getSimpleName() + CommonConstant.EMPTY_STRING,
                    className,
                    String.join("", listOfImplementMethodCode)
            );
            writer.write(codeToWrite);
        }
    }

    public List<String> buildMergingCode(String methodName,
                                         String methodReturnClass,
                                         String firstInputParameterClass,
                                         String firstInputParameterName,
                                         String secondInputParameterClass,
                                         String secondInputParameterName,
                                         boolean mergeNonNullField) {
        List<String> listOfImplementMethodCode = new ArrayList<>();
        if (!(firstInputParameterClass.equals(secondInputParameterClass))) {
            throw new RuntimeException("Merging method can not mapping 2 different classes");
        }

        TypeElement firstClassTypeElement = processingEnv.getElementUtils().getTypeElement(firstInputParameterClass);
        String mappingLineCodeTemplate = mergeNonNullField ?
                "\n        %s.set%s(%s.get%s());" :
                "\n        if (%s.get%s() == null) {\n            %s.set%s(%s.get%s());\n        }";
        List<Element> firstClassFields = new ArrayList<>(TypeHierarchyAnalyzer.getAllFields(processingEnv.getTypeUtils(), firstClassTypeElement));
        List<String> listOfMappingLineCodes = new ArrayList<>();
        for (Element field : firstClassFields) {
            String fieldName = WordCaseUtils.toPascalCase(WordCaseUtils.fromCamelCase(field.getSimpleName() + CommonConstant.EMPTY_STRING));
            final String mappingLineCode;
            if (mergeNonNullField) {
                mappingLineCode = String.format(
                        mappingLineCodeTemplate,
                        firstInputParameterName,
                        fieldName,
                        secondInputParameterName,
                        fieldName
                );
            } else {
                mappingLineCode = String.format(
                        mappingLineCodeTemplate,
                        firstInputParameterName,
                        fieldName,
                        firstInputParameterName,
                        fieldName,
                        secondInputParameterName,
                        fieldName
                );
            }
            listOfMappingLineCodes.add(mappingLineCode);
        }
        String implementMethodCode = String.format(
                "\n    public %s %s(%s %s, %s %s) {\n        if (%s == null || %s == null) {\n            %s\n        }\n        %s\n        %s\n    }",
                methodReturnClass,
                methodName,
                firstInputParameterClass,
                firstInputParameterName,
                secondInputParameterClass,
                secondInputParameterName,
                firstInputParameterName,
                secondInputParameterName,
                methodReturnClass.equals("void") ? "return;" : "return null;",
                String.join("", listOfMappingLineCodes),
                methodReturnClass.equals("void") ? "" : String.format("return %s;", firstInputParameterName)
        );
        listOfImplementMethodCode.add(implementMethodCode);
        return listOfImplementMethodCode;
    }

    public List<String> buildMappingCode(ExecutableElement methodsOfClass,
                                         String methodName,
                                         String methodReturnClass,
                                         String firstInputParameterClass,
                                         String firstInputParameterName) {
        List<String> listOfImplementMethodCode = new ArrayList<>();
        List<Mapping> listOfMappingAnnotations = new ArrayList<>(Arrays.asList(methodsOfClass.getAnnotationsByType(Mapping.class)));
        TypeElement inputClassTypeElement = processingEnv.getElementUtils().getTypeElement(firstInputParameterClass);
        TypeElement outputClassTypeElement = processingEnv.getElementUtils().getTypeElement(methodReturnClass);
        String mappingLineCodeTemplate = "\n        instance.set%s(%s.get%s());";
        List<Element> inputClassFields = TypeHierarchyAnalyzer.getAllFields(processingEnv.getTypeUtils(), inputClassTypeElement)
                .stream()
                .filter(element -> !(element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC)))
                .collect(Collectors.toList());
        List<Element> outputClassFields = TypeHierarchyAnalyzer.getAllFields(processingEnv.getTypeUtils(), outputClassTypeElement)
                .stream()
                .filter(element -> !(element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC)))
                .collect(Collectors.toList());
        List<String> listOfMappingLineCodes = new ArrayList<>();
        if (listOfMappingAnnotations.isEmpty()) {
            for (Element outputClassField : outputClassFields) {
                String toFieldName = WordCaseUtils.toPascalCase(WordCaseUtils.fromCamelCase(outputClassField.getSimpleName() + CommonConstant.EMPTY_STRING));
                String toFieldType = outputClassField.asType() + CommonConstant.EMPTY_STRING;
                Element inputClassField = this.findAppropriateFieldOfInputClassFromOutputFieldName(
                        inputClassFields,
                        outputClassField.getSimpleName() + CommonConstant.EMPTY_STRING,
                        toFieldType
                );
                if (inputClassField == null) {
                    continue;
                }
                String fromFieldName = WordCaseUtils.toPascalCase(WordCaseUtils.fromCamelCase(inputClassField.getSimpleName() + CommonConstant.EMPTY_STRING));
                if (fromFieldName.equals(toFieldName)) {
                    String mappingLineCode = String.format(
                            mappingLineCodeTemplate,
                            fromFieldName,
                            firstInputParameterName,
                            toFieldName
                    );
                    listOfMappingLineCodes.add(mappingLineCode);
                }
            }
        } else {
            final Set<String> handledFieldName = new HashSet<>();
            for (Mapping mappingAnnotation : listOfMappingAnnotations) {
                String toField = WordCaseUtils.toPascalCase(WordCaseUtils.fromCamelCase(mappingAnnotation.toField()));

                if (mappingAnnotation.skip()) {
                    handledFieldName.add(toField);
                    continue;
                }

                String fromField = WordCaseUtils.toPascalCase(WordCaseUtils.fromCamelCase(mappingAnnotation.fromField()));
                // if (!(inputClassTypeElement.getQualifiedName() + Constant.EMPTY_STRING).equals(outputClassTypeElement.getQualifiedName() + Constant.EMPTY_STRING)) {
                //     continue;
                // }
                String mappingLineCode = String.format(
                        mappingLineCodeTemplate,
                        toField,
                        firstInputParameterName,
                        fromField
                );
                listOfMappingLineCodes.add(mappingLineCode);
                handledFieldName.add(toField);
            }
            for (Element outputClassField : outputClassFields) {
                String toFieldType = outputClassField.asType() + CommonConstant.EMPTY_STRING;
                if (handledFieldName.contains(toFieldType)) {
                    continue;
                }
                String toFieldName = WordCaseUtils.toPascalCase(WordCaseUtils.fromCamelCase(outputClassField.getSimpleName() + CommonConstant.EMPTY_STRING));
                Element inputClassField = this.findAppropriateFieldOfInputClassFromOutputFieldName(
                        inputClassFields,
                        outputClassField.getSimpleName() + CommonConstant.EMPTY_STRING,
                        toFieldType
                );
                if (inputClassField == null) {
                    continue;
                }
                String fromFieldName = WordCaseUtils.toPascalCase(WordCaseUtils.fromCamelCase(inputClassField.getSimpleName() + CommonConstant.EMPTY_STRING));
                if (fromFieldName.equals(toFieldName)) {
                    String mappingLineCode = String.format(
                            mappingLineCodeTemplate,
                            fromFieldName,
                            firstInputParameterName,
                            toFieldName
                    );
                    listOfMappingLineCodes.add(mappingLineCode);
                }
            }
        }

        String implementMethodCode = String.format(
                "\n    public %s %s(%s %s) {\n        if (%s == null) {\n            return null;\n        }\n        %s instance = new %s();\n        %s\n        return instance;\n    }",
                methodReturnClass,
                methodName,
                firstInputParameterClass,
                firstInputParameterName,
                firstInputParameterName,
                methodReturnClass,
                methodReturnClass,
                String.join("", listOfMappingLineCodes)
        );
        listOfImplementMethodCode.add(implementMethodCode);
        return listOfImplementMethodCode;
    }

    public Element findAppropriateFieldOfInputClassFromOutputFieldName(List<Element> allElementOfAClass, final String fieldName, final String fieldDataType) {
        return allElementOfAClass.stream()
                .filter(
                        e ->
                                (e.getSimpleName() + CommonConstant.EMPTY_STRING).equals(fieldName) && (e.asType() + CommonConstant.EMPTY_STRING).equals(fieldDataType)
                )
                .findAny().orElse(null);
    }

}
