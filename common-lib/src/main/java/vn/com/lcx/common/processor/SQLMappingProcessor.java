package vn.com.lcx.common.processor;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.Clob;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.IdColumn;
import vn.com.lcx.common.annotation.SQLMapping;
import vn.com.lcx.common.annotation.TableName;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.constant.JavaSqlResultSetConstant;
import vn.com.lcx.common.utils.ExceptionUtils;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.MyStringUtils;
import vn.com.lcx.jpa.processor.utility.ProcessorClassInfo;
import vn.com.lcx.jpa.processor.utility.TypeHierarchyAnalyzer;

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
import java.io.IOException;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static vn.com.lcx.common.constant.JavaSqlResultSetConstant.DOT;
import static vn.com.lcx.common.utils.WordCaseUtils.capitalize;
import static vn.com.lcx.common.utils.WordCaseUtils.convertCamelToConstant;

@SupportedAnnotationTypes("vn.com.lcx.common.annotation.SQLMapping")
public class SQLMappingProcessor extends AbstractProcessor {

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
                    ProcessorClassInfo processorClassInfo = ProcessorClassInfo.init(
                            typeElement,
                            processingEnv.getTypeUtils(),
                            processingEnv.getElementUtils()
                    );
                    generateBuilderClass(processorClassInfo);
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

    private void generateBuilderClass(ProcessorClassInfo processorClassInfo) throws IOException {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                vn.com.lcx.common.utils.DateTimeUtils.toUnixMil(vn.com.lcx.common.utils.DateTimeUtils.generateCurrentTimeDefault()) + ": " +
                        String.format(
                                "Generating code for model : %s",
                                processorClassInfo.getClazz().getQualifiedName()
                        )
        );
        var sqlMappingAnnotation = processorClassInfo.getClazz().getAnnotation(SQLMapping.class);
        final String sqlMappingTemplate = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/sql-mapping-template.txt"
        );
        final String methodTemplate = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/method-template.txt"
        );
        assert sqlMappingTemplate != null;
        assert methodTemplate != null;
        final var resultSetMappingCodeLines = new ArrayList<String>();
        final var vertxRowMappingCodeLines = new ArrayList<String>();
        final var idRowExtractCodeLines = new ArrayList<String>();
        final var mySqlIdRowExtractCodeLines = new ArrayList<String>();
        final var idRowType = new AtomicReference<String>();
        StringBuilder methodCodeBody = new StringBuilder();
        methodCodeBody.append("\n");
        // Optional<TableName> tableNameAnnotation = Optional.ofNullable(processorClassInfo.getClazz().getAnnotation(TableName.class));
        resultSetMappingCodeLines.add(
                String.format(
                        "%1$s instance = new %1$s();",
                        processorClassInfo.getClazz().getSimpleName()
                )
        );
        vertxRowMappingCodeLines.add(
                String.format(
                        "%1$s instance = new %1$s();",
                        processorClassInfo.getClazz().getSimpleName()
                )
        );
        processorClassInfo.getFields().stream()
                .filter(
                        element ->
                                !(element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC))
                ).forEach(
                        element -> {
                            final String fieldName = element.getSimpleName().toString();
                            final String fieldType = element.asType().toString();
                            final String setFieldMethodName;
                            if (isPrimitiveBoolean(element) && fieldName.toLowerCase().startsWith("is")) {
                                setFieldMethodName = "set" + fieldName.substring(2);
                            } else {
                                setFieldMethodName = "set" + capitalize(fieldName);
                            }
                            ColumnName columnNameAnnotation = element.getAnnotation(ColumnName.class);
                            // String databaseColumnName = Optional
                            //         .ofNullable(columnNameAnnotation)
                            //         .filter(a -> StringUtils.isNotBlank(a.name()))
                            //         .map(ColumnName::name)
                            //         .orElse(convertCamelToConstant(fieldName));
                            // String databaseColumnNameToBeGet = tableNameAnnotation
                            //         .map(tableName -> String.format("%s_%s", EntityUtils.getTableShortenedName(tableName.value()).toUpperCase(), databaseColumnName))
                            //         .orElse(databaseColumnName);
                            String databaseColumnNameToBeGet = Optional
                                    .ofNullable(columnNameAnnotation)
                                    .filter(a -> StringUtils.isNotBlank(a.name()))
                                    .map(ColumnName::name)
                                    .orElse(convertCamelToConstant(fieldName));
                            final String fieldTypeSimpleName;
                            final String resultSetFunctionWillBeUse;
                            final String vertxRowFunctionWillBeUse;
                            if (fieldType.matches(".*\\..*")) {
                                List<String> fieldTypeSplitDot = new ArrayList<>(Arrays.asList(fieldType.split(JavaSqlResultSetConstant.DOT)));
                                fieldTypeSimpleName = fieldTypeSplitDot.get(fieldTypeSplitDot.size() - 1);
                            } else {
                                fieldTypeSimpleName = fieldType;
                            }
                            resultSetFunctionWillBeUse = JavaSqlResultSetConstant.RESULT_SET_DATA_TYPE_MAP.get(fieldTypeSimpleName);
                            vertxRowFunctionWillBeUse = JavaSqlResultSetConstant.VERTX_SQL_CLIENT_ROW.get(fieldTypeSimpleName);
                            extractResultSetMappingCode(
                                    element,
                                    resultSetFunctionWillBeUse,
                                    resultSetMappingCodeLines,
                                    fieldType,
                                    databaseColumnNameToBeGet,
                                    setFieldMethodName,
                                    fieldTypeSimpleName
                            );
                            extractVertxRowMappingCode(
                                    element,
                                    vertxRowFunctionWillBeUse,
                                    vertxRowMappingCodeLines,
                                    fieldType,
                                    databaseColumnNameToBeGet,
                                    setFieldMethodName,
                                    fieldTypeSimpleName
                            );
                            if (Optional.ofNullable(element.getAnnotation(IdColumn.class)).isPresent()) {
                                idRowType.set(fieldType);
                                idRowExtractCode(
                                        element,
                                        vertxRowFunctionWillBeUse,
                                        idRowExtractCodeLines,
                                        fieldType,
                                        databaseColumnNameToBeGet,
                                        setFieldMethodName,
                                        fieldTypeSimpleName
                                );
                                mySqlIdRowExtractCode(
                                        element,
                                        vertxRowFunctionWillBeUse,
                                        mySqlIdRowExtractCodeLines,
                                        fieldType,
                                        databaseColumnNameToBeGet,
                                        setFieldMethodName,
                                        fieldTypeSimpleName
                                );
                            }
                        }
                );
        resultSetMappingCodeLines.add("return instance;");
        vertxRowMappingCodeLines.add("return instance;");
        final var insertStatementCodeLines = new ArrayList<String>();
        final var reactiveInsertStatementCodeLines = new ArrayList<String>();
        final var insertJdbcParameterCodeLines = new ArrayList<String>();
        final var insertVertClientParameterCodeLines = new ArrayList<String>();
        final var updateStatementCodeLines = new ArrayList<String>();
        final var reactiveUpdateStatementCodeLines = new ArrayList<String>();
        final var updateJdbcParameterCodeLines = new ArrayList<String>();
        final var updateVertClientParameterCodeLines = new ArrayList<String>();
        final var deleteStatementCodeLines = new ArrayList<String>();
        final var reactiveDeleteStatementCodeLines = new ArrayList<String>();
        final var deleteJdbcParameterCodeLines = new ArrayList<String>();
        final var deleteVertClientParameterCodeLines = new ArrayList<String>();
        final var idColumnNameCodeLines = new ArrayList<String>();
        buildStatement(
                insertStatementCodeLines,
                reactiveInsertStatementCodeLines,
                insertJdbcParameterCodeLines,
                insertVertClientParameterCodeLines,
                updateStatementCodeLines,
                reactiveUpdateStatementCodeLines,
                updateJdbcParameterCodeLines,
                updateVertClientParameterCodeLines,
                deleteStatementCodeLines,
                reactiveDeleteStatementCodeLines,
                deleteJdbcParameterCodeLines,
                deleteVertClientParameterCodeLines,
                idColumnNameCodeLines,
                processorClassInfo
        );
        methodCodeBody.append(
                methodTemplate
                        .replace("${return-type}", "static " + processorClassInfo.getClazz().getQualifiedName().toString())
                        .replace("${method-name}", "resultSetMapping")
                        .replace("${list-of-parameters}", "java.sql.ResultSet resultSet")
                        .replace("${method-body}", resultSetMappingCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static String")
                        .replace("${method-name}", "insertStatement")
                        .replace("${list-of-parameters}", processorClassInfo.getClazz().getQualifiedName() + " model")
                        .replace("${method-body}", insertStatementCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static String")
                        .replace("${method-name}", "updateStatement")
                        .replace("${list-of-parameters}", processorClassInfo.getClazz().getQualifiedName() + " model")
                        .replace("${method-body}", updateStatementCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static String")
                        .replace("${method-name}", "deleteStatement")
                        .replace("${list-of-parameters}", processorClassInfo.getClazz().getQualifiedName() + " model")
                        .replace("${method-body}", deleteStatementCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static String")
                        .replace("${method-name}", "reactiveInsertStatement")
                        .replace("${list-of-parameters}", processorClassInfo.getClazz().getQualifiedName() + " model, final String placeHolder")
                        .replace("${method-body}", reactiveInsertStatementCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static String")
                        .replace("${method-name}", "reactiveUpdateStatement")
                        .replace("${list-of-parameters}", processorClassInfo.getClazz().getQualifiedName() + " model, final String placeHolder")
                        .replace("${method-body}", reactiveUpdateStatementCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static String")
                        .replace("${method-name}", "reactiveDeleteStatement")
                        .replace("${list-of-parameters}", processorClassInfo.getClazz().getQualifiedName() + " model, final String placeHolder")
                        .replace("${method-body}", reactiveDeleteStatementCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static java.util.Map<Integer, Object>")
                        .replace("${method-name}", "insertJDBCParams")
                        .replace("${list-of-parameters}", processorClassInfo.getClazz().getQualifiedName() + " model")
                        .replace("${method-body}", insertJdbcParameterCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static java.util.Map<Integer, Object>")
                        .replace("${method-name}", "updateJDBCParams")
                        .replace("${list-of-parameters}", processorClassInfo.getClazz().getQualifiedName() + " model")
                        .replace("${method-body}", updateJdbcParameterCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static java.util.Map<Integer, Object>")
                        .replace("${method-name}", "deleteJDBCParams")
                        .replace("${list-of-parameters}", processorClassInfo.getClazz().getQualifiedName() + " model")
                        .replace("${method-body}", deleteJdbcParameterCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static " + processorClassInfo.getClazz().getQualifiedName())
                        .replace("${method-name}", "vertxRowMapping")
                        .replace("${list-of-parameters}", "io.vertx.sqlclient.Row row")
                        .replace("${method-body}", vertxRowMappingCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static io.vertx.sqlclient.Tuple")
                        .replace("${method-name}", "insertTupleParam")
                        .replace("${list-of-parameters}", processorClassInfo.getClazz().getQualifiedName() + " model")
                        .replace("${method-body}", insertVertClientParameterCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static io.vertx.sqlclient.Tuple")
                        .replace("${method-name}", "updateTupleParam")
                        .replace("${list-of-parameters}", processorClassInfo.getClazz().getQualifiedName() + " model")
                        .replace("${method-body}", updateVertClientParameterCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static io.vertx.sqlclient.Tuple")
                        .replace("${method-name}", "deleteTupleParam")
                        .replace("${list-of-parameters}", processorClassInfo.getClazz().getQualifiedName() + " model")
                        .replace("${method-body}", deleteVertClientParameterCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static String")
                        .replace("${method-name}", "idColumnName")
                        .replace("${list-of-parameters}", CommonConstant.EMPTY_STRING)
                        .replace("${method-body}", idColumnNameCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static " + Optional.ofNullable(idRowType.get()).orElse("void"))
                        .replace("${method-name}", "idRowExtract")
                        .replace("${list-of-parameters}", "io.vertx.sqlclient.Row row, " + processorClassInfo.getClazz().getQualifiedName() + " model")
                        .replace("${method-body}", idRowExtractCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n").append(
                methodTemplate
                        .replace("${return-type}", "static void")
                        .replace("${method-name}", "mySqlIdRowExtract")
                        .replace("${list-of-parameters}", "io.vertx.sqlclient.RowSet<io.vertx.sqlclient.Row> rowSet, " + processorClassInfo.getClazz().getQualifiedName() + " model")
                        .replace("${method-body}", mySqlIdRowExtractCodeLines
                                .stream()
                                .collect(
                                        Collectors.joining(
                                                "\n        ",
                                                CommonConstant.EMPTY_STRING,
                                                CommonConstant.EMPTY_STRING
                                        )
                                )
                        )
        ).append("\n");
        final var packageName = processingEnv
                .getElementUtils()
                .getPackageOf(processorClassInfo.getClazz())
                .getQualifiedName()
                .toString();
        final var className = processorClassInfo.getClazz().getSimpleName() + "Utils";
        final var code = sqlMappingTemplate
                .replace("${package-name}", packageName)
                .replace("${class-name}", className)
                .replace("${methods}", MyStringUtils.removeSuffixOfString(methodCodeBody.toString(), "\n"));
        String fullClassName = packageName + "." + className;
        JavaFileObject builderFile = this.processingEnv.getFiler().createSourceFile(fullClassName);
        try (Writer writer = builderFile.openWriter()) {
            writer.write(code);
        }
    }

    private void extractResultSetMappingCode(final Element element,
                                             final String resultSetFunctionWillBeUse,
                                             final ArrayList<String> resultSetMappingCodeLines,
                                             final String fieldType,
                                             final String databaseColumnNameToBeGet,
                                             final String setFieldMethodName,
                                             final String fieldTypeSimpleName) {
        if (TypeHierarchyAnalyzer.isEnumField(element, processingEnv.getTypeUtils())) {
            resultSetMappingCodeLines.add(
                    "try {"
            );
            resultSetMappingCodeLines.add(
                    String.format(
                            "    String value = resultSet.getString(\"%s\");",
                            databaseColumnNameToBeGet
                    )
            );
            resultSetMappingCodeLines.add(
                    String.format(
                            "    instance.%s(%s.valueOf(value));",
                            setFieldMethodName,
                            element.asType() + CommonConstant.EMPTY_STRING
                    )
            );
            resultSetMappingCodeLines.add(
                    "} catch (java.sql.SQLException e) {"
            );
            resultSetMappingCodeLines.add(
                    "    log.debug(e.getMessage(), e);"
            );
            resultSetMappingCodeLines.add(
                    "}"
            );
            return;
        }
        if (resultSetFunctionWillBeUse != null && !resultSetFunctionWillBeUse.isEmpty()) {
            resultSetMappingCodeLines.add(
                    "try {"
            );
            if (element.getAnnotation(Clob.class) != null) {
                resultSetMappingCodeLines.add(
                        String.format(
                                "    %1$s value = vn.com.lcx.common.database.handler.resultset.SqlClobReader.parseClobToString(resultSet.getClob(\"%2$s\"));",
                                fieldType,
                                databaseColumnNameToBeGet
                        )
                );
            } else {
                resultSetMappingCodeLines.add(
                        String.format(
                                "    %1$s value = resultSet.%2$s(\"%3$s\");",
                                fieldType,
                                resultSetFunctionWillBeUse,
                                databaseColumnNameToBeGet
                        )
                );
            }
            resultSetMappingCodeLines.add(
                    String.format(
                            "    instance.%s(value);",
                            setFieldMethodName
                    )
            );
            resultSetMappingCodeLines.add(
                    "} catch (java.sql.SQLException e) {"
            );
            resultSetMappingCodeLines.add(
                    "    log.debug(e.getMessage(), e);"
            );
            resultSetMappingCodeLines.add(
                    "}"
            );
        } else {
            if (!LocalDateTime.class.getSimpleName().equals(fieldTypeSimpleName) &&
                    !LocalDate.class.getSimpleName().equals(fieldTypeSimpleName) &&
                    !BigDecimal.class.getSimpleName().equals(fieldTypeSimpleName) &&
                    !BigInteger.class.getSimpleName().equals(fieldTypeSimpleName)) {
                resultSetMappingCodeLines.add(
                        String.format(
                                "// ################# Unknow type to generate code for field `%s` - `%s` #################",
                                element.getSimpleName().toString(),
                                element.asType()
                        )
                );
                return;
            }
            resultSetMappingCodeLines.add(
                    "try {"
            );
            if (LocalDate.class.getSimpleName().equals(fieldTypeSimpleName)) {
                resultSetMappingCodeLines.add(
                        String.format(
                                "    java.sql.Date time = resultSet.getDate(\"%s\");",
                                databaseColumnNameToBeGet
                        )
                );
                resultSetMappingCodeLines.add(
                        String.format(
                                "    instance.%s(time != null ? time.toLocalDate() : null);",
                                setFieldMethodName
                        )
                );
            }
            if (LocalDateTime.class.getSimpleName().equals(fieldTypeSimpleName)) {
                resultSetMappingCodeLines.add(
                        String.format(
                                "    java.sql.Timestamp time = resultSet.getTimestamp(\"%s\");",
                                databaseColumnNameToBeGet
                        )
                );
                resultSetMappingCodeLines.add(
                        String.format(
                                "    instance.%s(time != null ? time.toLocalDateTime() : null);",
                                setFieldMethodName
                        )
                );
            }
            if (BigDecimal.class.getSimpleName().equals(fieldTypeSimpleName)) {
                resultSetMappingCodeLines.add(
                        String.format(
                                "    String resultNumberInString = resultSet.getString(\"%s\");",
                                databaseColumnNameToBeGet
                        )
                );
                resultSetMappingCodeLines.add(
                        String.format(
                                "    instance.%s(resultNumberInString != null && !resultNumberInString.isEmpty() ? new java.math.BigDecimal(resultNumberInString) : java.math.BigDecimal.ZERO);",
                                setFieldMethodName
                        )
                );
            }
            if (BigInteger.class.getSimpleName().equals(fieldTypeSimpleName)) {
                resultSetMappingCodeLines.add(
                        String.format(
                                "    String resultNumberInString = resultSet.getString(\"%s\");",
                                databaseColumnNameToBeGet
                        )
                );
                resultSetMappingCodeLines.add(
                        String.format(
                                "    instance.%s(resultNumberInString != null && !resultNumberInString.isEmpty() ? new java.math.BigInteger(resultNumberInString) : java.math.BigInteger.ZERO);",
                                setFieldMethodName
                        )
                );
            }
            resultSetMappingCodeLines.add(
                    "} catch (java.sql.SQLException e) {"
            );
            resultSetMappingCodeLines.add(
                    "    log.debug(e.getMessage(), e);"
            );
            resultSetMappingCodeLines.add(
                    "}"
            );
        }
    }

    private void extractVertxRowMappingCode(final Element element,
                                            final String resultSetFunctionWillBeUse,
                                            final ArrayList<String> vertxRowMappingCodeLines,
                                            final String fieldType,
                                            final String databaseColumnNameToBeGet,
                                            final String setFieldMethodName,
                                            final String fieldTypeSimpleName) {
        if (TypeHierarchyAnalyzer.isEnumField(element, processingEnv.getTypeUtils())) {
            vertxRowMappingCodeLines.add(
                    "try {"
            );
            vertxRowMappingCodeLines.add(
                    String.format(
                            "    String value = row.getString(\"%s\");",
                            databaseColumnNameToBeGet
                    )
            );
            vertxRowMappingCodeLines.add(
                    String.format(
                            "    instance.%s(%s.valueOf(value));",
                            setFieldMethodName,
                            element.asType() + CommonConstant.EMPTY_STRING
                    )
            );
            vertxRowMappingCodeLines.add(
                    "} catch (java.lang.Throwable e) {"
            );
            vertxRowMappingCodeLines.add(
                    "    log.debug(e.getMessage(), e);"
            );
            vertxRowMappingCodeLines.add(
                    "}"
            );
            return;
        }
        if (resultSetFunctionWillBeUse != null && !resultSetFunctionWillBeUse.isEmpty()) {
            vertxRowMappingCodeLines.add(
                    "try {"
            );
            vertxRowMappingCodeLines.add(
                    String.format(
                            "    %1$s value = row.%2$s(\"%3$s\");",
                            fieldType,
                            resultSetFunctionWillBeUse,
                            databaseColumnNameToBeGet
                    )
            );
            vertxRowMappingCodeLines.add(
                    String.format(
                            "    instance.%s(value);",
                            setFieldMethodName
                    )
            );
            vertxRowMappingCodeLines.add(
                    "} catch (java.lang.Throwable e) {"
            );
            vertxRowMappingCodeLines.add(
                    "    log.debug(e.getMessage(), e);"
            );
            vertxRowMappingCodeLines.add(
                    "}"
            );
        } else {
            if (!BigInteger.class.getSimpleName().equals(fieldTypeSimpleName)) {
                vertxRowMappingCodeLines.add(
                        String.format(
                                "// ################# Unknow type to generate code for field `%s` - `%s` #################",
                                element.getSimpleName().toString(),
                                element.asType()
                        )
                );
                return;
            }
            vertxRowMappingCodeLines.add(
                    "try {"
            );
            vertxRowMappingCodeLines.add(
                    String.format(
                            "    io.vertx.sqlclient.data.Numeric numericValue = row.getNumeric(\"%s\");",
                            databaseColumnNameToBeGet
                    )
            );
            vertxRowMappingCodeLines.add("    if (numericValue != null) {");
            vertxRowMappingCodeLines.add("        java.math.BigInteger bigIntValue = numericValue.bigIntegerValue();");
            vertxRowMappingCodeLines.add(String.format("        instance.%s(bigIntValue);", setFieldMethodName));
            vertxRowMappingCodeLines.add("    }");
            vertxRowMappingCodeLines.add(
                    "} catch (java.lang.Throwable e) {"
            );
            vertxRowMappingCodeLines.add(
                    "    log.debug(e.getMessage(), e);"
            );
            vertxRowMappingCodeLines.add(
                    "}"
            );
        }
    }

    private void idRowExtractCode(final Element element,
                                  final String resultSetFunctionWillBeUse,
                                  final ArrayList<String> vertxRowMappingCodeLines,
                                  final String fieldType,
                                  final String databaseColumnNameToBeGet,
                                  final String setFieldMethodName,
                                  final String fieldTypeSimpleName) {
        final String defaultValueCode = JavaSqlResultSetConstant.DATA_TYPE_DEFAULT_VALUE_MAP.getOrDefault(fieldTypeSimpleName, CommonConstant.NULL_STRING);
        final String returnDefaultValueCode = "return " + defaultValueCode;
        if (resultSetFunctionWillBeUse != null && !resultSetFunctionWillBeUse.isEmpty()) {
            vertxRowMappingCodeLines.add(
                    "try {"
            );
            vertxRowMappingCodeLines.add(
                    String.format(
                            "    %1$s value = row.%2$s(\"%3$s\");",
                            fieldType,
                            resultSetFunctionWillBeUse,
                            databaseColumnNameToBeGet
                    )
            );
            vertxRowMappingCodeLines.add(
                    String.format("    model.%s(value);", setFieldMethodName)
            );
            vertxRowMappingCodeLines.add("    return value;");
            vertxRowMappingCodeLines.add(
                    "} catch (java.lang.Throwable ignored) {"
            );
            vertxRowMappingCodeLines.add(
                    "    " + returnDefaultValueCode + ";"
            );
            vertxRowMappingCodeLines.add(
                    "}"
            );
        } else {
            if (!BigInteger.class.getSimpleName().equals(fieldTypeSimpleName)) {
                vertxRowMappingCodeLines.add(
                        String.format(
                                "// ################# Unknow type to generate code for field `%s` - `%s` #################",
                                element.getSimpleName().toString(),
                                element.asType()
                        )
                );
                return;
            }
            vertxRowMappingCodeLines.add(
                    "try {"
            );
            vertxRowMappingCodeLines.add(
                    String.format(
                            "    io.vertx.sqlclient.data.Numeric numericValue = row.getNumeric(\"%s\");",
                            databaseColumnNameToBeGet
                    )
            );
            vertxRowMappingCodeLines.add("    if (numericValue != null) {");
            vertxRowMappingCodeLines.add("        java.math.BigInteger bigIntValue = numericValue.bigIntegerValue();");
            vertxRowMappingCodeLines.add(String.format("        model.%s(bigIntValue);", setFieldMethodName));
            vertxRowMappingCodeLines.add("        return bigIntValue;");
            vertxRowMappingCodeLines.add("    } else {");
            vertxRowMappingCodeLines.add("        " + returnDefaultValueCode + ";");
            vertxRowMappingCodeLines.add("    }");
            vertxRowMappingCodeLines.add(
                    "} catch (java.lang.Throwable ignored) {"
            );
            vertxRowMappingCodeLines.add(
                    "    " + returnDefaultValueCode + ";"
            );
            vertxRowMappingCodeLines.add(
                    "}"
            );
        }
    }

    private void mySqlIdRowExtractCode(final Element element,
                                       final String resultSetFunctionWillBeUse,
                                       final ArrayList<String> vertxRowMappingCodeLines,
                                       final String fieldType,
                                       final String databaseColumnNameToBeGet,
                                       final String setFieldMethodName,
                                       final String fieldTypeSimpleName) {
        if (JavaSqlResultSetConstant.NUMBER_DATA_TYPE_CLASS_NAME.contains(fieldTypeSimpleName)) {
            vertxRowMappingCodeLines.add("long lastInsertId = rowSet.property(io.vertx.mysqlclient.MySQLClient.LAST_INSERTED_ID);");
            if (fieldTypeSimpleName.equals("BigDecimal")) {
                vertxRowMappingCodeLines.add("final java.math.BigDecimal value = java.math.BigDecimal.valueOf(lastInsertId);");
            } else if (fieldTypeSimpleName.equals("BigInteger")) {
                vertxRowMappingCodeLines.add("final java.math.BigInteger value = java.math.BigInteger.valueOf(lastInsertId);");
            } else {
                vertxRowMappingCodeLines.add("final long value = lastInsertId;");
            }
            vertxRowMappingCodeLines.add(
                    String.format("model.%s(value);", setFieldMethodName)
            );
        } else {
            vertxRowMappingCodeLines.add("// ID column is not number");
        }
    }

    private void buildStatement(final ArrayList<String> insertStatementCodeLines,
                                final ArrayList<String> reactiveInsertStatementCodeLines,
                                final ArrayList<String> insertJdbcParameterCodeLines,
                                final ArrayList<String> insertVertClientParameterCodeLines,
                                final ArrayList<String> updateStatementCodeLines,
                                final ArrayList<String> reactiveUpdateStatementCodeLines,
                                final ArrayList<String> updateJdbcParameterCodeLines,
                                final ArrayList<String> updateVertClientParameterCodeLines,
                                final ArrayList<String> deleteStatementCodeLines,
                                final ArrayList<String> reactiveDeleteStatementCodeLines,
                                final ArrayList<String> deleteJdbcParameterCodeLines,
                                final ArrayList<String> deleteVertClientParameterCodeLines,
                                final ArrayList<String> idColumnNameCodeLines,
                                final ProcessorClassInfo processorClassInfo) {
        final String tableName = getTableName(processorClassInfo);
        if (StringUtils.isBlank(tableName)) {
            insertStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"A `vn.com.lcx.common.annotation.TableName` should be defined\");");
            reactiveInsertStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"A `vn.com.lcx.common.annotation.TableName` should be defined\");");
            insertJdbcParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"A `vn.com.lcx.common.annotation.TableName` should be defined\");");
            insertVertClientParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"A `vn.com.lcx.common.annotation.TableName` should be defined\");");
            updateStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"A `vn.com.lcx.common.annotation.TableName` should be defined\");");
            reactiveUpdateStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"A `vn.com.lcx.common.annotation.TableName` should be defined\");");
            updateJdbcParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"A `vn.com.lcx.common.annotation.TableName` should be defined\");");
            updateVertClientParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"A `vn.com.lcx.common.annotation.TableName` should be defined\");");
            deleteStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"A `vn.com.lcx.common.annotation.TableName` should be defined\");");
            reactiveDeleteStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"A `vn.com.lcx.common.annotation.TableName` should be defined\");");
            deleteJdbcParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"A `vn.com.lcx.common.annotation.TableName` should be defined\");");
            deleteVertClientParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"A `vn.com.lcx.common.annotation.TableName` should be defined\");");
            idColumnNameCodeLines.add("return null;");
            return;
        }
        var idElements = processorClassInfo.getFields().stream()
                .filter(
                        element ->
                                !(element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC))
                ).filter(
                        element ->
                                Optional.ofNullable(element.getAnnotation(IdColumn.class)).isPresent()
                ).collect(Collectors.toCollection(ArrayList::new));
        if (idElements.isEmpty()) {
            insertStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"An primary key should be defined\");");
            reactiveInsertStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"An primary key should be defined\");");
            insertJdbcParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"An primary key should be defined\");");
            insertVertClientParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"An primary key should be defined\");");
            updateStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"An primary key should be defined\");");
            reactiveUpdateStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"An primary key should be defined\");");
            updateJdbcParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"An primary key should be defined\");");
            updateVertClientParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"An primary key should be defined\");");
            deleteStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"An primary key should be defined\");");
            reactiveDeleteStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"An primary key should be defined\");");
            deleteJdbcParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"An primary key should be defined\");");
            deleteVertClientParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"An primary key should be defined\");");
            idColumnNameCodeLines.add("return null;");
            return;
        }
        if (idElements.size() > 1) {
            insertStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"More than one id column were defined\");");
            reactiveInsertStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"More than one id column were defined\");");
            insertJdbcParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"More than one id column were defined\");");
            insertVertClientParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"More than one id column were defined\");");
            updateStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"More than one id column were defined\");");
            reactiveUpdateStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"More than one id column were defined\");");
            updateJdbcParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"More than one id column were defined\");");
            updateVertClientParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"More than one id column were defined\");");
            deleteStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"More than one id column were defined\");");
            reactiveDeleteStatementCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"More than one id column were defined\");");
            deleteJdbcParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"More than one id column were defined\");");
            deleteVertClientParameterCodeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"More than one id column were defined\");");
            idColumnNameCodeLines.add("return null;");
            return;
        }
        final var idElement = idElements.get(0);
        final String idFieldName = idElement.getSimpleName().toString();
        ColumnName idColumnNameAnnotation = idElement.getAnnotation(ColumnName.class);
        String idDatabaseColumnNameToBeGet = Optional
                .ofNullable(idColumnNameAnnotation)
                .filter(a -> StringUtils.isNotBlank(a.name()))
                .map(ColumnName::name)
                .orElse(convertCamelToConstant(idFieldName));
        idColumnNameCodeLines.add(
                String.format(
                        "return \"%s\";",
                        idDatabaseColumnNameToBeGet
                )
        );
        insertStatementCodeLines.add("java.util.List<String> cols = new java.util.ArrayList<>();");
        reactiveInsertStatementCodeLines.add("java.util.List<String> cols = new java.util.ArrayList<>();");
        updateStatementCodeLines.add(String.format(String.format("if (model.get%s() == null) {", capitalize(idElement.getSimpleName().toString()))));
        updateStatementCodeLines.add("    throw new RuntimeException(\"Primary key is null\");");
        updateStatementCodeLines.add("}");
        updateStatementCodeLines.add("java.util.List<String> cols = new java.util.ArrayList<>();");
        reactiveUpdateStatementCodeLines.add(String.format(String.format("if (model.get%s() == null) {", capitalize(idElement.getSimpleName().toString()))));
        reactiveUpdateStatementCodeLines.add("    throw new RuntimeException(\"Primary key is null\");");
        reactiveUpdateStatementCodeLines.add("}");
        reactiveUpdateStatementCodeLines.add("java.util.List<String> cols = new java.util.ArrayList<>();");
        reactiveUpdateStatementCodeLines.add("int count = 0;");
        deleteStatementCodeLines.add(String.format(String.format("if (model.get%s() == null) {", capitalize(idElement.getSimpleName().toString()))));
        deleteStatementCodeLines.add("    throw new RuntimeException(\"Primary key is null\");");
        deleteStatementCodeLines.add("}");
        reactiveDeleteStatementCodeLines.add(String.format(String.format("if (model.get%s() == null) {", capitalize(idElement.getSimpleName().toString()))));
        reactiveDeleteStatementCodeLines.add("    throw new RuntimeException(\"Primary key is null\");");
        reactiveDeleteStatementCodeLines.add("}");
        insertJdbcParameterCodeLines.add("java.util.Map<Integer, Object> map = new java.util.HashMap<>();");
        insertJdbcParameterCodeLines.add("int startingPosition = 0;");
        updateJdbcParameterCodeLines.add("java.util.Map<Integer, Object> map = new java.util.HashMap<>();");
        updateJdbcParameterCodeLines.add("int startingPosition = 0;");
        deleteJdbcParameterCodeLines.add("java.util.Map<Integer, Object> map = new java.util.HashMap<>();");
        deleteJdbcParameterCodeLines.add("int startingPosition = 0;");
        insertVertClientParameterCodeLines.add("java.util.ArrayList<Object> params = new java.util.ArrayList<>();");
        updateVertClientParameterCodeLines.add("java.util.ArrayList<Object> params = new java.util.ArrayList<>();");
        deleteVertClientParameterCodeLines.add("java.util.ArrayList<Object> params = new java.util.ArrayList<>();");
        processorClassInfo.getFields().stream()
                .filter(element ->
                        !(element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC))
                ).forEach(element -> {
                    final String fieldName = element.getSimpleName().toString();
                    // final String fieldType = element.asType().toString();
                    ColumnName columnNameAnnotation = element.getAnnotation(ColumnName.class);
                    String databaseColumnNameToBeGet = Optional
                            .ofNullable(columnNameAnnotation)
                            .filter(a -> StringUtils.isNotBlank(a.name()))
                            .map(ColumnName::name)
                            .orElse(convertCamelToConstant(fieldName));
                    final boolean insertable = Optional
                            .ofNullable(columnNameAnnotation)
                            .map(ColumnName::insertable)
                            .orElse(true);
                    final boolean updatable = Optional
                            .ofNullable(columnNameAnnotation)
                            .map(ColumnName::updatable)
                            .orElse(true);
                    final boolean nullable = Optional
                            .ofNullable(columnNameAnnotation)
                            .map(ColumnName::nullable)
                            .orElse(true);
                    if (insertable) {
                        insertStatementCodeLines.add(String.format("if (model.get%s() != null) {", capitalize(fieldName)));
                        insertStatementCodeLines.add(String.format("    cols.add(\"%s\");", databaseColumnNameToBeGet));
                        insertStatementCodeLines.add("}");
                        reactiveInsertStatementCodeLines.add(String.format("if (model.get%s() != null) {", capitalize(fieldName)));
                        reactiveInsertStatementCodeLines.add(String.format("    cols.add(\"%s\");", databaseColumnNameToBeGet));
                        reactiveInsertStatementCodeLines.add("}");
                        insertJdbcParameterCodeLines.add(String.format("if (model.get%s() != null) {", capitalize(fieldName)));
                        insertJdbcParameterCodeLines.add(String.format("    map.put(++startingPosition, model.get%s()%s);", capitalize(fieldName), (TypeHierarchyAnalyzer.isEnumField(element, processingEnv.getTypeUtils()) ? ".name()" : CommonConstant.EMPTY_STRING)));
                        insertVertClientParameterCodeLines.add(String.format("if (model.get%s() != null) {", capitalize(fieldName)));
                        insertVertClientParameterCodeLines.add(String.format("    params.add(model.get%s()%s);", capitalize(fieldName), (TypeHierarchyAnalyzer.isEnumField(element, processingEnv.getTypeUtils()) ? ".name()" : CommonConstant.EMPTY_STRING)));
                        if (!nullable) {
                            insertJdbcParameterCodeLines.add("} else {");
                            insertJdbcParameterCodeLines.add("    throw new java.lang.NullPointerException(\"" + fieldName + " is marked as not nullable\");");
                            insertJdbcParameterCodeLines.add("}");
                            insertVertClientParameterCodeLines.add("} else {");
                            insertVertClientParameterCodeLines.add("    throw new java.lang.NullPointerException(\"" + fieldName + " is marked as not nullable\");");
                            insertVertClientParameterCodeLines.add("}");
                        } else {
                            insertJdbcParameterCodeLines.add("}");
                            insertVertClientParameterCodeLines.add("}");
                        }
                    }
                    if (!fieldName.equals(idFieldName) && updatable) {
                        updateStatementCodeLines.add(String.format("if (model.get%s() != null) {", capitalize(fieldName)));
                        updateStatementCodeLines.add(String.format("    cols.add(\"%s = ?\");", databaseColumnNameToBeGet));
                        updateStatementCodeLines.add("}");
                        reactiveUpdateStatementCodeLines.add(String.format("if (model.get%s() != null) {", capitalize(fieldName)));
                        reactiveUpdateStatementCodeLines.add("    if (placeHolder.equals(\"?\")) {");
                        reactiveUpdateStatementCodeLines.add(String.format("        cols.add(\"%s = ?\");", databaseColumnNameToBeGet));
                        reactiveUpdateStatementCodeLines.add("    } else {");
                        reactiveUpdateStatementCodeLines.add(String.format("        cols.add(\"%s = \" + placeHolder + (++count));", databaseColumnNameToBeGet));
                        reactiveUpdateStatementCodeLines.add("    }");
                        reactiveUpdateStatementCodeLines.add("}");
                        updateJdbcParameterCodeLines.add(String.format("if (model.get%s() != null) {", capitalize(fieldName)));
                        updateJdbcParameterCodeLines.add(String.format("    map.put(++startingPosition, model.get%s()%s);", capitalize(fieldName), (TypeHierarchyAnalyzer.isEnumField(element, processingEnv.getTypeUtils()) ? ".name()" : CommonConstant.EMPTY_STRING)));
                        updateVertClientParameterCodeLines.add(String.format("if (model.get%s() != null) {", capitalize(fieldName)));
                        updateVertClientParameterCodeLines.add(String.format("    params.add(model.get%s()%s);", capitalize(fieldName), (TypeHierarchyAnalyzer.isEnumField(element, processingEnv.getTypeUtils()) ? ".name()" : CommonConstant.EMPTY_STRING)));
                        if (!nullable) {
                            updateJdbcParameterCodeLines.add("} else {");
                            updateJdbcParameterCodeLines.add("    throw new java.lang.NullPointerException(\"" + fieldName + " is marked as not nullable\");");
                            updateJdbcParameterCodeLines.add("}");
                            updateVertClientParameterCodeLines.add("} else {");
                            updateVertClientParameterCodeLines.add("    throw new java.lang.NullPointerException(\"" + fieldName + " is marked as not nullable\");");
                            updateVertClientParameterCodeLines.add("}");
                        } else {
                            updateJdbcParameterCodeLines.add("}");
                            updateVertClientParameterCodeLines.add("}");
                        }
                        // updateStatementCodeLines.addAll(codes);
                    }
                });
        insertStatementCodeLines.add(String.format("return \"INSERT INTO %s\" +", tableName));
        insertStatementCodeLines.add("        cols.stream().collect(java.util.stream.Collectors.joining(\", \", \" (\", \") \")) +");
        insertStatementCodeLines.add("        \"VALUES\" +");
        insertStatementCodeLines.add("        cols.stream().map(it -> \"?\").collect(java.util.stream.Collectors.joining(\", \", \" (\", \") \"));");
        reactiveInsertStatementCodeLines.add("if (placeHolder.equals(\"?\")) {");
        reactiveInsertStatementCodeLines.add(String.format("    return \"INSERT INTO %s\" +", tableName));
        reactiveInsertStatementCodeLines.add("            cols.stream().collect(java.util.stream.Collectors.joining(\", \", \" (\", \") \")) +");
        reactiveInsertStatementCodeLines.add("            \"VALUES\" +");
        reactiveInsertStatementCodeLines.add("            cols.stream().map(it -> \"?\").collect(java.util.stream.Collectors.joining(\", \", \" (\", \") \"));");
        reactiveInsertStatementCodeLines.add("} else {");
        reactiveInsertStatementCodeLines.add(String.format("    return \"INSERT INTO %s\" +", tableName));
        reactiveInsertStatementCodeLines.add("            cols.stream().collect(java.util.stream.Collectors.joining(\", \", \" (\", \") \")) +");
        reactiveInsertStatementCodeLines.add("            (placeHolder.equals(\"@p\") ? \"OUTPUT INSERTED." + idDatabaseColumnNameToBeGet + " VALUES\" : \"VALUES\") +");
        reactiveInsertStatementCodeLines.add("            java.util.stream.IntStream.range(0, cols.size()).mapToObj(i -> placeHolder + (i + 1)).collect(java.util.stream.Collectors.joining(\", \", \" (\", \") \"));");
        reactiveInsertStatementCodeLines.add("}");
        updateStatementCodeLines.add(String.format("return \"UPDATE %s SET \" + String.join(\",\", cols) + \" WHERE %s = ?\";", tableName, idDatabaseColumnNameToBeGet));
        reactiveUpdateStatementCodeLines.add("if (placeHolder.equals(\"?\")) {");
        reactiveUpdateStatementCodeLines.add(String.format("    return \"UPDATE %s SET \" + String.join(\",\", cols) + \" WHERE %s = ?\";", tableName, idDatabaseColumnNameToBeGet));
        reactiveUpdateStatementCodeLines.add("} else {");
        reactiveUpdateStatementCodeLines.add(String.format("    return \"UPDATE %s SET \" + String.join(\",\", cols) + \" WHERE %s = \" + placeHolder + (++count);", tableName, idDatabaseColumnNameToBeGet));
        reactiveUpdateStatementCodeLines.add("}");
        deleteStatementCodeLines.add(String.format("return \"DELETE FROM %s WHERE %s = ?\";", tableName, idDatabaseColumnNameToBeGet));
        reactiveDeleteStatementCodeLines.add("if (placeHolder.equals(\"?\")) {");
        reactiveDeleteStatementCodeLines.add(String.format("    return \"DELETE FROM %s WHERE %s = ?\";", tableName, idDatabaseColumnNameToBeGet));
        reactiveDeleteStatementCodeLines.add("} else {");
        reactiveDeleteStatementCodeLines.add(String.format("    return \"DELETE FROM %s WHERE %s = \" + placeHolder + \"1\";", tableName, idDatabaseColumnNameToBeGet));
        reactiveDeleteStatementCodeLines.add("}");
        deleteJdbcParameterCodeLines.add(String.format("if (model.get%s() != null) {", capitalize(idFieldName)));
        deleteJdbcParameterCodeLines.add(String.format("    map.put(++startingPosition, model.get%s());", capitalize(idFieldName)));
        deleteJdbcParameterCodeLines.add("}");
        deleteVertClientParameterCodeLines.add(String.format("if (model.get%s() != null) {", capitalize(idFieldName)));
        deleteVertClientParameterCodeLines.add(String.format("    params.add(model.get%s());", capitalize(idFieldName)));
        deleteVertClientParameterCodeLines.add("}");
        updateVertClientParameterCodeLines.add(String.format("if (model.get%s() != null) {", capitalize(idFieldName)));
        updateVertClientParameterCodeLines.add(String.format("    params.add(model.get%s());", capitalize(idFieldName)));
        updateVertClientParameterCodeLines.add("}");
        insertJdbcParameterCodeLines.add("return map;");
        updateJdbcParameterCodeLines.add("return map;");
        deleteJdbcParameterCodeLines.add("return map;");
        insertVertClientParameterCodeLines.add("return io.vertx.sqlclient.Tuple.from(params.toArray(Object[]::new));");
        updateVertClientParameterCodeLines.add("return io.vertx.sqlclient.Tuple.from(params.toArray(Object[]::new));");
        deleteVertClientParameterCodeLines.add("return io.vertx.sqlclient.Tuple.from(params.toArray(Object[]::new));");
    }

    private String getTableName(ProcessorClassInfo processorClassInfo) {
        final String tableName;
        TableName tableNameAnnotation = processorClassInfo.getClazz().getAnnotation(TableName.class);
        if (tableNameAnnotation == null) {
            return CommonConstant.EMPTY_STRING;
        }
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
        return tableName;
    }

}
