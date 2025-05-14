package vn.com.lcx.common.processor;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.AdditionalCode;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.IdColumn;
import vn.com.lcx.common.annotation.SecondaryIdColumn;
import vn.com.lcx.common.annotation.TableName;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.utils.EntityUtils;
import vn.com.lcx.common.utils.ExceptionUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static vn.com.lcx.common.constant.JavaSqlResultSetConstant.DOT;
import static vn.com.lcx.common.constant.JavaSqlResultSetConstant.RESULT_SET_DATA_TYPE_MAP;
import static vn.com.lcx.common.utils.WordCaseUtils.capitalize;
import static vn.com.lcx.common.utils.WordCaseUtils.convertCamelToConstant;

@SupportedAnnotationTypes("vn.com.lcx.common.annotation.SQLMapping")
public class SQLMappingProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private static final String LOGGING_FUNCTION = "org.slf4j.LoggerFactory.error(\"Cannot mapping result set\", sqlException);";

    private static boolean isPrimitiveBoolean(Element element) {
        TypeMirror typeMirror = element.asType();
        return typeMirror.getKind() == TypeKind.BOOLEAN;
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
            List<Element> classElements = new ArrayList<>(getAllFields(typeElement));
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
                        String resultSetMappingCodeLine = String.format("" +
                                        "        try {" +
                                        "\n            %s value = resultSet.%s(\"%s\");" +
                                        "\n            instance.%s(%s);" +
                                        "\n        } catch (java.sql.SQLException sqlException) {" +
                                        "\n            // %s" +
                                        "\n        }" +
                                        "\n",
                                fieldType,
                                resultSetFunctionWillBeUse,
                                databaseColumnNameToBeGet,
                                fieldName,
                                valueCodeLine,
                                exceptionLoggingCodeLine
                        );
                        resultSetMappingCodeLineStringBuilder.append(resultSetMappingCodeLine).append("\n");
                        if (tableNameAnnotation.isPresent()) {
                            String resultSetMappingCodeLine2 = String.format("" +
                                            "        try {" +
                                            "\n            %s value = resultSet.%s(\"%s\");" +
                                            "\n            instance.%s(%s);" +
                                            "\n        } catch (java.sql.SQLException sqlException) {" +
                                            "\n            // %s" +
                                            "\n        }" +
                                            "\n",
                                    fieldType,
                                    resultSetFunctionWillBeUse,
                                    databaseColumnName,
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

    @SuppressWarnings("UnnecessaryLocalVariable")
    private Set<Element> getAllFields(TypeElement typeElement) {

        // Collect fields from the current class
        Set<Element> fields = new HashSet<>(ElementFilter.fieldsIn(typeElement.getEnclosedElements()));

        // Get the superclass and repeat the process
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass != null && !superclass.toString().equals(Object.class.getCanonicalName())) {
            Element superclassElement = processingEnv.getTypeUtils().asElement(superclass);
            if (superclassElement instanceof TypeElement) {
                fields.addAll(getAllFields((TypeElement) superclassElement));
            }
        }

        return fields.stream()
                .filter(element -> {
                    boolean elementIsField = element.getKind().isField();
                    boolean fieldIsNotFinalOrStatic = !(element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC));
                    ColumnName columnName = element.getAnnotation(ColumnName.class);
                    final boolean isAnnotatedWithColumnNameAnnotation = columnName != null;
                    return elementIsField && fieldIsNotFinalOrStatic && isAnnotatedWithColumnNameAnnotation;
                })
                .collect(Collectors.toSet());
    }

    public static final class SqlStatementBuilder {

        private SqlStatementBuilder() {
        }

        public static String insertStatementBuilder(TypeElement typeElement, List<Element> classElements) {

            String result = CommonConstant.EMPTY_STRING;

            TableName tableNameAnnotation = typeElement.getAnnotation(TableName.class);

            if (tableNameAnnotation == null) {
                return result;
            }
            String tableName;
            String tableNameValue = tableNameAnnotation.value();

            if (StringUtils.isNotBlank(tableNameAnnotation.schema())) {
                String schemaName = tableNameAnnotation.schema() + ".";
                if (tableNameValue.contains(".")) {
                    final String[] tableNameValueArray = tableNameValue.split(DOT);
                    tableName = schemaName + tableNameValueArray[tableNameValueArray.length - 1];
                } else {
                    tableName = schemaName + tableNameValue;
                }
            } else {
                tableName = tableNameValue;
            }

            String insertStatement = "INSERT INTO\\n    " + tableName;
            ArrayList<String> columnList = new ArrayList<>();
            String entityFieldGetCode = CommonConstant.EMPTY_STRING;
            ArrayList<String> putEntityFieldIntoMapCodeLines = new ArrayList<String>();
            String idColumnFieldName = CommonConstant.EMPTY_STRING;
            boolean idColumnIsNumber = false;

            for (Element classElement : classElements) {
                ColumnName columnNameAnnotation = classElement.getAnnotation(ColumnName.class);
                if (columnNameAnnotation == null || StringUtils.isBlank(columnNameAnnotation.name())) {
                    continue;
                }
                IdColumn idColumnAnnotation = classElement.getAnnotation(IdColumn.class);
                if (idColumnAnnotation != null) {
                    idColumnFieldName = classElement.getSimpleName().toString();
                    if (
                            classElement.asType().toString().equals("java.math.BigDecimal") ||
                                    classElement.asType().toString().equals("java.lang.Long")
                    ) {
                        idColumnIsNumber = true;
                        continue;
                    }
                }
                columnList.add(columnNameAnnotation.name());
                putEntityFieldIntoMapCodeLines.add(
                        String.format(
                                "map.put(++startingPosition, entity.get%s())",
                                capitalize(classElement.getSimpleName().toString())
                        )
                );
            }
            if (StringUtils.isBlank(idColumnFieldName)) {
                return result;
            }
            insertStatement += "(\\n        " + String.join(",\\n        ", columnList) + "\\n    )";
            // insertStatement += "(" + String.join(", ", columnList) + ")";

            insertStatement += "\\nVALUES\\n    " + "(\\n        " + columnList.stream().map(v -> "?").collect(Collectors.joining(",\\n        ")) + "\\n    )";
            // insertStatement += "\\nVALUES\\n    " + "(" + String.join(", ", columnList.stream().map(v -> "?").toList()) + ")";
            if (idColumnIsNumber) {
                entityFieldGetCode += String.format(
                        "\n" +
                                "\n    public static java.util.Map<Integer, Object> insertMapInputParameter(%s entity) {" +
                                "\n        java.util.Map<Integer, Object> map = new java.util.HashMap<>();" +
                                "\n        int startingPosition = 0;" +
                                "\n        %s;" +
                                "\n        return map;" +
                                "\n    }",
                        typeElement.getSimpleName(),
                        String.join(";\n        ", putEntityFieldIntoMapCodeLines)
                );
            } else {
                entityFieldGetCode += String.format(
                        "" +
                                "    public static java.util.Map<Integer, Object> insertMapInputParameter(%s entity) {" +
                                "\n        if (entity.get%s() == null) {" +
                                "\n            throw new RuntimeException(\"ID is null\");" +
                                "\n        }" +
                                "\n        java.util.Map<Integer, Object> map = new java.util.HashMap<>();" +
                                "\n        int startingPosition = 0;" +
                                "\n        %s;" +
                                "\n        return map;" +
                                "\n    }",
                        typeElement.getSimpleName(),
                        capitalize(idColumnFieldName),
                        String.join(";\n        ", putEntityFieldIntoMapCodeLines)
                );
            }

            insertStatement = "\n    public static String insertSql() {\n        " + "return \"" + insertStatement + "\";" + "\n    }";

            // System.out.println(entityFieldGetCode + "\n" + insertStatement);
            result += entityFieldGetCode + "\n" + insertStatement + "\n";
            return result;
        }

        public static String insertStatementBuilderV2(TypeElement typeElement, List<Element> classElements) {

            TableName tableNameAnnotation = typeElement.getAnnotation(TableName.class);

            if (tableNameAnnotation == null) {
                return "";
            }
            String tableName;
            String tableNameValue = tableNameAnnotation.value();

            if (StringUtils.isNotBlank(tableNameAnnotation.schema())) {
                String schemaName = tableNameAnnotation.schema() + ".";
                if (tableNameValue.contains(".")) {
                    final String[] tableNameValueArray = tableNameValue.split(DOT);
                    tableName = schemaName + tableNameValueArray[tableNameValueArray.length - 1];
                } else {
                    tableName = schemaName + tableNameValue;
                }
            } else {
                tableName = tableNameValue;
            }

            String insertStatement = "INSERT INTO\\n    " + tableName;
            String entityFieldGetCode = CommonConstant.EMPTY_STRING;
            ArrayList<String> codeLines = new ArrayList<>();
            ArrayList<String> putEntityFieldIntoMapCodeLines = new ArrayList<>();
            String idColumnFieldName = CommonConstant.EMPTY_STRING;
            boolean idColumnIsNumber = false;

            for (Element classElement : classElements) {
                ColumnName columnNameAnnotation = classElement.getAnnotation(ColumnName.class);
                if (columnNameAnnotation == null || StringUtils.isBlank(columnNameAnnotation.name())) {
                    continue;
                }
                codeLines.add(String.format(
                        "        if (entity.get%s() != null) {\n            cols.add(\"\\n        %s\");\n        }\n",
                        capitalize(classElement.getSimpleName().toString()),
                        columnNameAnnotation.name()
                ));
                codeLines.add(String.format(
                        "        if (entity.get%s() != null) {\n            params.add(\"\\n        ?\");\n        }\n",
                        capitalize(classElement.getSimpleName().toString())
                ));
                putEntityFieldIntoMapCodeLines.add(String.format(
                        "        if (entity.get%1$s() != null) {\n            map.put(++startingPosition, entity.get%1$s());\n        }\n",
                        capitalize(classElement.getSimpleName().toString())
                ));
            }
            return String.format(
                    "\n" +
                            "    public static String insertSqlV2(%s entity) {\n" +
                            "        java.util.List<String> cols = new java.util.ArrayList<>();\n" +
                            "        java.util.List<String> params = new java.util.ArrayList<>();\n" +
                            "%s\n" +
                            "        return \"INSERT INTO\\n    \" + \"%s\" + \"(\" + String.join(\",\", cols) + \"\\n)\\n\" + \"VALUES\\n    \" + \"(\" + String.join(\",\", params) + \"\\n    )\";\n" +
                            "    }\n",
                    typeElement.getSimpleName(),
                    String.join("", codeLines),
                    tableName
            ) + String.format(
                    "\n" +
                            "    public static java.util.Map<Integer, Object> insertMapInputParameterV2(%s entity) {" +
                            "\n        java.util.Map<Integer, Object> map = new java.util.HashMap<>();" +
                            "\n        int startingPosition = 0;" +
                            "\n%s" +
                            "\n        return map;" +
                            "\n    }\n\n",
                    typeElement.getSimpleName(),
                    String.join("", putEntityFieldIntoMapCodeLines)
            );
        }

        public static String updateStatementBuilder(TypeElement typeElement, List<Element> classElements) {

            String result = CommonConstant.EMPTY_STRING;

            TableName tableNameAnnotation = typeElement.getAnnotation(TableName.class);

            if (tableNameAnnotation == null) {
                return result;
            }

            String tableName;
            String tableNameValue = tableNameAnnotation.value();

            if (StringUtils.isNotBlank(tableNameAnnotation.schema())) {
                String schemaName = tableNameAnnotation.schema() + ".";
                if (tableNameValue.contains(".")) {
                    final String[] tableNameValueArray = tableNameValue.split(DOT);
                    tableName = schemaName + tableNameValueArray[tableNameValueArray.length - 1];
                } else {
                    tableName = schemaName + tableNameValue;
                }
            } else {
                tableName = tableNameValue;
            }

            String updateStatement = "UPDATE\\n    " + tableName + "\\nSET\\n    ";
            ArrayList<String> columnList = new ArrayList<String>();
            String entityFieldGetCode = CommonConstant.EMPTY_STRING;
            ArrayList<String> entityFieldList = new ArrayList<String>();

            boolean idColumnIsNumber = false;

            String idColumnColumnName = CommonConstant.EMPTY_STRING;
            String idColumnFieldName = CommonConstant.EMPTY_STRING;

            for (Element classElement : classElements) {
                ColumnName columnNameAnnotation = classElement.getAnnotation(ColumnName.class);
                if (columnNameAnnotation == null || StringUtils.isBlank(columnNameAnnotation.name())) {
                    continue;
                }
                IdColumn idColumnAnnotation = classElement.getAnnotation(IdColumn.class);
                if (idColumnAnnotation != null) {
                    idColumnColumnName = columnNameAnnotation.name();
                    idColumnFieldName = classElement.getSimpleName().toString();
                    if (
                            classElement.asType().toString().equals("java.math.BigDecimal") ||
                                    classElement.asType().toString().equals("java.lang.Long")
                    ) {
                        idColumnIsNumber = true;
                    }
                    continue;
                }
                SecondaryIdColumn secondaryIdColumnAnnotation = classElement.getAnnotation(SecondaryIdColumn.class);
                if (secondaryIdColumnAnnotation != null && idColumnIsNumber) {
                    idColumnColumnName = columnNameAnnotation.name();
                    idColumnFieldName = classElement.getSimpleName().toString();
                    continue;
                }

                columnList.add(columnNameAnnotation.name() + " = ?");
                entityFieldList.add(
                        String.format(
                                "map.put(++startingPosition, entity.get%s())",
                                capitalize(classElement.getSimpleName().toString())
                        )
                );
            }

            if (StringUtils.isBlank(idColumnColumnName) || StringUtils.isBlank(idColumnFieldName)) {
                return result;
            }

            entityFieldList.add(
                    String.format(
                            "map.put(++startingPosition, entity.get%s())",
                            capitalize(idColumnFieldName)
                    )
            );

            updateStatement += String.join(",\\n    ", columnList) + "\\nWHERE\\n    " + idColumnColumnName + " = ?";
            // updateStatement += String.join(", ", columnList) + "\\nWHERE\\n    " + idColumnColumnName + " = ?";

            entityFieldGetCode += String.format(
                    "" +
                            "\n    public static java.util.Map<Integer, Object> updateMapInputParameter(%s entity) {" +
                            "\n        if (entity.get%s() == null) {" +
                            "\n            throw new RuntimeException(\"ID is null\");" +
                            "\n        }" +
                            "\n        java.util.Map<Integer, Object> map = new java.util.HashMap<>();" +
                            "\n        int startingPosition = 0;" +
                            "\n        %s;" +
                            "\n        return map;" +
                            "\n    }",
                    typeElement.getSimpleName(),
                    capitalize(idColumnFieldName),
                    String.join(";\n        ", entityFieldList)
            );
            updateStatement = "\n    public static String updateSql() {\n        " + "return \"" + updateStatement + "\";" + "\n    }";

            // System.out.println(entityFieldGetCode + "\n" + updateStatement);
            result += entityFieldGetCode + "\n" + updateStatement + "\n";
            return result;
        }

        public static String updateStatementBuilderV2(TypeElement typeElement, List<Element> classElements) {

            TableName tableNameAnnotation = typeElement.getAnnotation(TableName.class);

            if (tableNameAnnotation == null) {
                return "";
            }
            String tableName;
            String tableNameValue = tableNameAnnotation.value();

            if (StringUtils.isNotBlank(tableNameAnnotation.schema())) {
                String schemaName = tableNameAnnotation.schema() + ".";
                if (tableNameValue.contains(".")) {
                    final String[] tableNameValueArray = tableNameValue.split(DOT);
                    tableName = schemaName + tableNameValueArray[tableNameValueArray.length - 1];
                } else {
                    tableName = schemaName + tableNameValue;
                }
            } else {
                tableName = tableNameValue;
            }

            String insertStatement = "INSERT INTO\\n    " + tableName;
            String entityFieldGetCode = CommonConstant.EMPTY_STRING;
            ArrayList<String> codeLines = new ArrayList<>();
            ArrayList<String> putEntityFieldIntoMapCodeLines = new ArrayList<>();
            boolean idColumnIsNumber = false;

            String idColumnColumnName = CommonConstant.EMPTY_STRING;
            String idColumnFieldName = CommonConstant.EMPTY_STRING;

            for (Element classElement : classElements) {
                ColumnName columnNameAnnotation = classElement.getAnnotation(ColumnName.class);
                if (columnNameAnnotation == null || StringUtils.isBlank(columnNameAnnotation.name())) {
                    continue;
                }
                IdColumn idColumnAnnotation = classElement.getAnnotation(IdColumn.class);
                if (idColumnAnnotation != null) {
                    idColumnColumnName = columnNameAnnotation.name();
                    idColumnFieldName = classElement.getSimpleName().toString();
                    continue;
                }
                codeLines.add(String.format(
                        "        if (entity.get%s() != null) {\n            cols.add(\"\\n    %s = ?\");\n        }\n",
                        capitalize(classElement.getSimpleName().toString()),
                        columnNameAnnotation.name()
                ));
                putEntityFieldIntoMapCodeLines.add(String.format(
                        "        if (entity.get%1$s() != null) {\n            map.put(++startingPosition, entity.get%1$s());\n        }\n",
                        capitalize(classElement.getSimpleName().toString())
                ));
            }
            if (StringUtils.isNotBlank(idColumnFieldName) && StringUtils.isNotBlank(idColumnColumnName)) {
                putEntityFieldIntoMapCodeLines.add(String.format(
                        "        if (entity.get%1$s() != null) {\n            map.put(++startingPosition, entity.get%1$s());\n        }\n",
                        capitalize(idColumnFieldName)
                ));
                return String.format(
                        "" +
                                "    public static String updateSqlV2(%s entity) {\n" +
                                "        java.util.List<String> cols = new java.util.ArrayList<>();\n" +
                                "%s\n" +
                                "        return \"UPDATE\\n    \" + \"%s\" + \"\\nSET\" + String.join(\",\", cols) + \"\\nWHERE %s = ?\";\n" +
                                "    }",
                        typeElement.getSimpleName(),
                        String.join("", codeLines),
                        tableName,
                        idColumnColumnName
                ) + String.format(
                        "\n" +
                                "\n    public static java.util.Map<Integer, Object> updateMapInputParameterV2(%s entity) {" +
                                "\n        java.util.Map<Integer, Object> map = new java.util.HashMap<>();" +
                                "\n        int startingPosition = 0;" +
                                "\n%s" +
                                "\n        return map;" +
                                "\n    }\n",
                        typeElement.getSimpleName(),
                        String.join("", putEntityFieldIntoMapCodeLines)
                );
            }
            return "// no id field to generate\n";
        }

        public static String deleteStatementBuilder(TypeElement typeElement, List<Element> classElements) {

            String result = CommonConstant.EMPTY_STRING;

            TableName tableNameAnnotation = typeElement.getAnnotation(TableName.class);

            if (tableNameAnnotation == null) {
                return result;
            }

            String tableName;
            String tableNameValue = tableNameAnnotation.value();

            if (StringUtils.isNotBlank(tableNameAnnotation.schema())) {
                String schemaName = tableNameAnnotation.schema() + ".";
                if (tableNameValue.contains(".")) {
                    final String[] tableNameValueArray = tableNameValue.split(DOT);
                    tableName = schemaName + tableNameValueArray[tableNameValueArray.length - 1];
                } else {
                    tableName = schemaName + tableNameValue;
                }
            } else {
                tableName = tableNameValue;
            }

            String deleteStatement = "DELETE FROM\\n    " + tableName + "\\nWHERE\\n    ";
            ArrayList<String> columnList = new ArrayList<String>();
            String entityFieldGetCode = CommonConstant.EMPTY_STRING;
            ArrayList<String> entityFieldList = new ArrayList<String>();

            boolean idColumnIsNumber = false;

            String idColumnColumnName = CommonConstant.EMPTY_STRING;
            String idColumnFieldName = CommonConstant.EMPTY_STRING;

            for (Element classElement : classElements) {
                ColumnName columnNameAnnotation = classElement.getAnnotation(ColumnName.class);
                if (columnNameAnnotation == null || StringUtils.isBlank(columnNameAnnotation.name())) {
                    continue;
                }
                IdColumn idColumnAnnotation = classElement.getAnnotation(IdColumn.class);
                if (idColumnAnnotation != null) {
                    idColumnColumnName = columnNameAnnotation.name();
                    idColumnFieldName = classElement.getSimpleName().toString();
                    if (
                            classElement.asType().toString().equals("java.math.BigDecimal") ||
                                    classElement.asType().toString().equals("java.lang.Long")
                    ) {
                        idColumnIsNumber = true;
                    }
                    continue;
                }
                SecondaryIdColumn secondaryIdColumnAnnotation = classElement.getAnnotation(SecondaryIdColumn.class);
                if (secondaryIdColumnAnnotation != null && idColumnIsNumber) {
                    idColumnColumnName = columnNameAnnotation.name();
                    idColumnFieldName = classElement.getSimpleName().toString();
                }
            }

            if (StringUtils.isBlank(idColumnColumnName) || StringUtils.isBlank(idColumnFieldName)) {
                return result;
            }

            entityFieldList.add(
                    String.format(
                            "map.put(++startingPosition, entity.get%s())",
                            capitalize(idColumnFieldName)
                    )
            );

            deleteStatement += idColumnColumnName + " = ?";

            entityFieldGetCode += String.format(
                    "" +
                            "\n    public static java.util.Map<Integer, Object> deleteMapInputParameter(%s entity) {" +
                            "\n        if (entity.get%s() == null) {" +
                            "\n            throw new RuntimeException(\"ID is null\");" +
                            "\n        }" +
                            "\n        java.util.Map<Integer, Object> map = new java.util.HashMap<>();" +
                            "\n        int startingPosition = 0;" +
                            "\n        %s;" +
                            "\n        return map;" +
                            "\n    }",
                    typeElement.getSimpleName(),
                    capitalize(idColumnFieldName),
                    String.join(";\n        ", entityFieldList)
            );
            deleteStatement = "\n    public static String deleteSql() {\n        " + "return \"" + deleteStatement + "\";" + "\n    }";

            // System.out.println(entityFieldGetCode + "\n" + deleteStatement);
            result += entityFieldGetCode + "\n" + deleteStatement + "\n";
            return result;
        }

    }

}
