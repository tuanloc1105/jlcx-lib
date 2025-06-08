package vn.com.lcx.common.processor;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.AdditionalCode;
import vn.com.lcx.common.annotation.Clob;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.TableName;
import vn.com.lcx.common.database.utils.EntityUtils;
import vn.com.lcx.common.utils.ExceptionUtils;
import vn.com.lcx.jpa.processor.TypeHierarchyAnalyzer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static vn.com.lcx.common.constant.JavaSqlResultSetConstant.DOT;
import static vn.com.lcx.common.constant.JavaSqlResultSetConstant.RESULT_SET_DATA_TYPE_MAP;
import static vn.com.lcx.common.utils.WordCaseUtils.capitalize;
import static vn.com.lcx.common.utils.WordCaseUtils.convertCamelToConstant;

@SupportedAnnotationTypes("vn.com.lcx.common.annotation.SQLMapping")
public class SQLMappingProcessor extends AbstractProcessor {

    private static final String LOGGING_FUNCTION = "org.slf4j.LoggerFactory.error(\"Cannot mapping result set\", sqlException);";

    private static boolean isPrimitiveBoolean(Element element) {
        TypeMirror typeMirror = element.asType();
        return typeMirror.getKind() == TypeKind.BOOLEAN;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // roundEnv.getRootElements()
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(vn.com.lcx.common.annotation.SQLMapping.class)) {
            if (annotatedElement instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                try {
                    generateBuilderClass(typeElement);
                } catch (Exception e) {
                    this.processingEnv.
                            getMessager().
                            printMessage(
                                    Diagnostic.Kind.ERROR,
                                    ExceptionUtils.getStackTrace(e)
                            );
                }
            }
        }
        return true;
    }

    private void generateBuilderClass(TypeElement typeElement) throws Exception {
        String className = typeElement.getSimpleName() + "Builder";
        String packageName = this.processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
        String fullClassName = packageName + "." + className;

        JavaFileObject builderFile = this.processingEnv.getFiler().createSourceFile(fullClassName);
        try (Writer writer = builderFile.openWriter()) {
            writer.write("package " + packageName + ";\n\n");
            writer.write("public class " + className + " {\n\n");
            writer.write("    private " + className + "() {\n");
            writer.write("    }\n\n");
            writer.write("    public static " + className + " builder() {\n");
            writer.write("        return new " + className + "();\n");
            writer.write("    }\n\n");

            // StringBuilder resultSetMappingCodeLineStringBuilder = new StringBuilder("\n    public static ").append(typeElement.getSimpleName())
            //         .append(" resultSetTo")
            //         .append(typeElement.getSimpleName())
            //         .append("(java.sql.ResultSet resultSet) {")
            //         .append("\n        ").append(className).append(" instance = ").append(className).append(".builder();\n")
            //         .append("\n");

            StringBuilder resultSetMappingCodeLineStringBuilder = new StringBuilder("\n    public static ").append(typeElement.getSimpleName())
                    .append(" resultSetMapping")
                    .append("(java.sql.ResultSet resultSet) {")
                    .append("\n        ").append(className).append(" instance = ").append(className).append(".builder();\n")
                    .append("\n");
            List<Element> classElements = new ArrayList<>(TypeHierarchyAnalyzer.getAllFields(processingEnv.getTypeUtils(), typeElement));
            Optional<TableName> tableNameAnnotation = Optional.ofNullable(typeElement.getAnnotation(TableName.class));
            classElements.forEach(element -> {
                boolean elementIsField = element.getKind().isField();
                boolean fieldIsNotFinalOrStatic = !(element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC));
                if (elementIsField && fieldIsNotFinalOrStatic) {
                    // Get the annotation on this field
                    ColumnName columnNameAnnotation = element.getAnnotation(ColumnName.class);
                    String fieldName = element.getSimpleName().toString();
                    String fieldType = element.asType().toString();
                    String dataTypeSimpleName;
                    String resultSetFunctionWillBeUse;
                    if (fieldType.matches(".*\\..*")) {
                        List<String> fieldTypeSplitDot = new ArrayList<>(Arrays.asList(fieldType.split(DOT)));
                        dataTypeSimpleName = fieldTypeSplitDot.get(fieldTypeSplitDot.size() - 1);
                    } else {
                        dataTypeSimpleName = fieldType;
                    }
                    resultSetFunctionWillBeUse = RESULT_SET_DATA_TYPE_MAP.get(dataTypeSimpleName);
                    String exceptionLoggingCodeLine = String.format(LOGGING_FUNCTION, "sqlException.getMessage(), sqlException");

                    String databaseColumnName = Optional
                            .ofNullable(columnNameAnnotation)
                            .filter(a -> StringUtils.isNotBlank(a.name()))
                            .map(ColumnName::name)
                            .orElse(convertCamelToConstant(fieldName));

                    String databaseColumnNameToBeGet = tableNameAnnotation
                            .map(tableName -> String.format("%s_%s", EntityUtils.getTableShortenedName(tableName.value()).toUpperCase(), databaseColumnName))
                            .orElse(databaseColumnName);

                    AdditionalCode additionalCode = element.getAnnotation(AdditionalCode.class);

                    String valueCodeLine = Optional
                            .ofNullable(additionalCode)
                            .filter(a -> StringUtils.isNotBlank(a.codeLine()))
                            .map(AdditionalCode::codeLine)
                            .orElse("value");

                    if (resultSetFunctionWillBeUse != null && !resultSetFunctionWillBeUse.isEmpty()) {
                        var getValueFromResultSetCode = "resultSet." + resultSetFunctionWillBeUse + "(\"%s\")";
                        if (element.getAnnotation(Clob.class) != null) {
                            getValueFromResultSetCode = "vn.com.lcx.common.database.handler.resultset.SqlClobReader.parseClobToString(resultSet.getClob(\"%s\"))";
                        }
                        String resultSetMappingCodeLine = String.format("" +
                                        "        try {" +
                                        "\n            %s value = %s;" +
                                        "\n            instance.%s(%s);" +
                                        "\n        } catch (java.sql.SQLException sqlException) {" +
                                        "\n            // %s" +
                                        "\n        }" +
                                        "\n",
                                fieldType,
                                String.format(getValueFromResultSetCode, databaseColumnNameToBeGet),
                                fieldName,
                                valueCodeLine,
                                exceptionLoggingCodeLine
                        );
                        resultSetMappingCodeLineStringBuilder.append(resultSetMappingCodeLine).append("\n");
                        if (tableNameAnnotation.isPresent()) {
                            String resultSetMappingCodeLine2 = String.format("" +
                                            "        try {" +
                                            "\n            %s value = %s;" +
                                            "\n            instance.%s(%s);" +
                                            "\n        } catch (java.sql.SQLException sqlException) {" +
                                            "\n            // %s" +
                                            "\n        }" +
                                            "\n",
                                    fieldType,
                                    String.format(getValueFromResultSetCode, databaseColumnName),
                                    fieldName,
                                    valueCodeLine,
                                    exceptionLoggingCodeLine
                            );
                            resultSetMappingCodeLineStringBuilder.append(resultSetMappingCodeLine2).append("\n");
                        }
                    } else {
                        if (LocalDateTime.class.getSimpleName().equals(dataTypeSimpleName)) {
                            String resultSetMappingCodeLine = String.format(
                                    "" +
                                            "        try {" +
                                            "\n            java.sql.Timestamp time = resultSet.getTimestamp(\"%s\");" +
                                            "\n            instance.%s(time != null ? time.toLocalDateTime() : null);" +
                                            "\n        } catch (java.sql.SQLException sqlException) {" +
                                            "\n            // %s" +
                                            "\n        }" +
                                            "\n",
                                    databaseColumnNameToBeGet,
                                    fieldName,
                                    exceptionLoggingCodeLine
                            );
                            resultSetMappingCodeLineStringBuilder.append(resultSetMappingCodeLine).append("\n");
                            if (tableNameAnnotation.isPresent()) {
                                String resultSetMappingCodeLine2 = String.format(
                                        "" +
                                                "        try {" +
                                                "\n            java.sql.Timestamp time = resultSet.getTimestamp(\"%s\");" +
                                                "\n            instance.%s(time != null ? time.toLocalDateTime() : null);" +
                                                "\n        } catch (java.sql.SQLException sqlException) {" +
                                                "\n            // %s" +
                                                "\n        }" +
                                                "\n",
                                        databaseColumnName,
                                        fieldName,
                                        exceptionLoggingCodeLine
                                );
                                resultSetMappingCodeLineStringBuilder.append(resultSetMappingCodeLine2).append("\n");
                            }
                        }
                        if (LocalDate.class.getSimpleName().equals(dataTypeSimpleName)) {
                            String resultSetMappingCodeLine = String.format(
                                    "" +
                                            "        try {" +
                                            "\n            java.sql.Date time = resultSet.getDate(\"%s\");" +
                                            "\n            instance.%s(time != null ? time.toLocalDate() : null);" +
                                            "\n        } catch (java.sql.SQLException sqlException) {" +
                                            "\n            // %s" +
                                            "\n        }" +
                                            "\n",
                                    databaseColumnNameToBeGet,
                                    fieldName,
                                    exceptionLoggingCodeLine
                            );
                            resultSetMappingCodeLineStringBuilder.append(resultSetMappingCodeLine).append("\n");
                            if (tableNameAnnotation.isPresent()) {
                                String resultSetMappingCodeLine2 = String.format(
                                        "" +
                                                "        try {" +
                                                "\n            java.sql.Date time = resultSet.getDate(\"%s\");" +
                                                "\n            instance.%s(time != null ? time.toLocalDate() : null);" +
                                                "\n        } catch (java.sql.SQLException sqlException) {" +
                                                "\n            // %s" +
                                                "\n        }" +
                                                "\n",
                                        databaseColumnName,
                                        fieldName,
                                        exceptionLoggingCodeLine
                                );
                                resultSetMappingCodeLineStringBuilder.append(resultSetMappingCodeLine2).append("\n");
                            }
                        }
                        if (BigDecimal.class.getSimpleName().equals(dataTypeSimpleName)) {
                            String resultSetMappingCodeLine = String.format(
                                    "" +
                                            "        try {" +
                                            "\n            String resultNumberInString = resultSet.getString(\"%s\");" +
                                            "\n            instance.%s(resultNumberInString != null && !resultNumberInString.isEmpty() ? new java.math.BigDecimal(resultNumberInString) : new java.math.BigDecimal(\"0\"));" +
                                            "\n        } catch (java.sql.SQLException sqlException) {" +
                                            "\n            // %s" +
                                            "\n        }" +
                                            "\n",
                                    databaseColumnNameToBeGet,
                                    fieldName,
                                    exceptionLoggingCodeLine
                            );
                            resultSetMappingCodeLineStringBuilder.append(resultSetMappingCodeLine).append("\n");
                            if (tableNameAnnotation.isPresent()) {
                                String resultSetMappingCodeLine2 = String.format(
                                        "" +
                                                "        try {" +
                                                "\n            String resultNumberInString = resultSet.getString(\"%s\");" +
                                                "\n            instance.%s(resultNumberInString != null && !resultNumberInString.isEmpty() ? new java.math.BigDecimal(resultNumberInString) : new java.math.BigDecimal(\"0\"));" +
                                                "\n        } catch (java.sql.SQLException sqlException) {" +
                                                "\n            // %s" +
                                                "\n        }" +
                                                "\n",
                                        databaseColumnName,
                                        fieldName,
                                        exceptionLoggingCodeLine
                                );
                                resultSetMappingCodeLineStringBuilder.append(resultSetMappingCodeLine2).append("\n");
                            }
                        }
                        if (BigInteger.class.getSimpleName().equals(dataTypeSimpleName)) {
                            String resultSetMappingCodeLine = String.format("" +
                                            "        try {" +
                                            "\n            String resultNumberInString = resultSet.getString(\"%s\");" +
                                            "\n            instance.%s(resultNumberInString != null && !resultNumberInString.isEmpty() ? new java.math.BigInteger(resultNumberInString) : new java.math.BigInteger(\"0\"));" +
                                            "\n        } catch (java.sql.SQLException sqlException) {" +
                                            "\n            // %s" +
                                            "\n        }" +
                                            "\n",
                                    databaseColumnNameToBeGet,
                                    fieldName,
                                    exceptionLoggingCodeLine);
                            resultSetMappingCodeLineStringBuilder.append(resultSetMappingCodeLine).append("\n");
                            if (tableNameAnnotation.isPresent()) {
                                String resultSetMappingCodeLine2 = String.format("" +
                                                "        try {" +
                                                "\n            String resultNumberInString = resultSet.getString(\"%s\");" +
                                                "\n            instance.%s(resultNumberInString != null && !resultNumberInString.isEmpty() ? new java.math.BigInteger(resultNumberInString) : new java.math.BigInteger(\"0\"));" +
                                                "\n        } catch (java.sql.SQLException sqlException) {" +
                                                "\n            // %s" +
                                                "\n        }" +
                                                "\n",
                                        databaseColumnName,
                                        fieldName,
                                        exceptionLoggingCodeLine);
                                resultSetMappingCodeLineStringBuilder.append(resultSetMappingCodeLine2).append("\n");
                            }
                        }
                    }
                    try {
                        writer.write("    private " + fieldType + " " + fieldName + ";\n\n");
                        // writer.write("    public " + className + " set" + capitalize(fieldName) + "(" + fieldType + " " + fieldName + ") {\n");
                        writer.write("    public " + className + " " + fieldName + "(" + fieldType + " " + fieldName + ") {\n");
                        writer.write("        this." + fieldName + " = " + fieldName + ";\n");
                        writer.write("        return this;\n");
                        writer.write("    }\n\n");
                    } catch (Exception e) {
                        this.processingEnv.
                                getMessager().
                                printMessage(
                                        Diagnostic.Kind.ERROR,
                                        ExceptionUtils.getStackTrace(e)
                                );
                    }
                }
            });
            resultSetMappingCodeLineStringBuilder.append("        return instance.build();\n    }\n");

            writer.write("    public " + typeElement.getSimpleName() + " build() {\n");
            writer.write("        " + typeElement.getSimpleName() + " instance = new " + typeElement.getSimpleName() + "();\n");
            classElements.forEach(element -> {
                boolean elementIsField = element.getKind().isField();
                boolean fieldIsNotFinalOrStatic = !(element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC));
                if (elementIsField && fieldIsNotFinalOrStatic) {
                    String fieldName = element.getSimpleName().toString();
                    try {
                        if (isPrimitiveBoolean(element) && fieldName.toLowerCase().startsWith("is")) {
                            writer.write("        instance.set" + fieldName.substring(2) + "(this." + fieldName + ");\n");
                        } else {
                            writer.write("        instance.set" + capitalize(fieldName) + "(this." + fieldName + ");\n");
                        }
                    } catch (Exception e) {
                        this.processingEnv.
                                getMessager().
                                printMessage(
                                        Diagnostic.Kind.ERROR,
                                        ExceptionUtils.getStackTrace(e)
                                );
                    }
                }
            });
            writer.write("        return instance;\n");
            writer.write("    }\n\n");
            writer.write(resultSetMappingCodeLineStringBuilder.toString());
            writer.write(SqlStatementBuilder.insertStatementBuilder(typeElement, classElements));
            writer.write(SqlStatementBuilder.updateStatementBuilder(typeElement, classElements));
            writer.write(SqlStatementBuilder.deleteStatementBuilder(typeElement, classElements));
            writer.write(SqlStatementBuilder.insertStatementBuilderV2(typeElement, classElements));
            writer.write(SqlStatementBuilder.updateStatementBuilderV2(typeElement, classElements));
            // writer.write(SqlStatementBuilder.selectStatementBuilder(typeElement, classElements));
            writer.write("}\n");
        }
    }

}
