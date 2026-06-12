package vn.io.lcx.processor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import vn.io.lcx.common.annotation.ReadOnly;
import vn.io.lcx.common.constant.CommonConstant;
import vn.io.lcx.common.utils.ExceptionUtils;
import vn.io.lcx.common.utils.FileUtils;
import vn.io.lcx.common.utils.MyStringUtils;
import vn.io.lcx.processor.utility.MethodInfo;
import vn.io.lcx.processor.utility.ProcessorClassInfo;
import vn.io.lcx.processor.utility.ReactiveCodeGenHelper;
import vn.io.lcx.processor.utility.TypeHierarchyAnalyzer;
import vn.io.lcx.reactive.annotation.Query;
import vn.io.lcx.reactive.annotation.RRepository;
import vn.io.lcx.reactive.repository.ReactiveRepository;

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
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("vn.io.lcx.reactive.annotation.RRepository")
public class ReactiveRepositoryProcessor extends AbstractProcessor {

    // Error messages
    private static final String ERROR_INVALID_PARAMETERS = "First parameter must be a `io.vertx.ext.web.RoutingContext` and the second one must be a `io.vertx.sqlclient.SqlConnection`";
    private static final String ERROR_UNSUPPORTED_METHOD = "Unsupported method. The generator of this type of method were removed, please define method with @vn.io.lcx.reactive.annotation.Query and provide a SQL statement";

    // Expected parameter types
    private static final String ROUTING_CONTEXT_TYPE = "io.vertx.ext.web.RoutingContext";
    private static final String SQL_CONNECTION_TYPE = "io.vertx.sqlclient.SqlConnection";

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(RRepository.class)) {
            if (annotatedElement.getKind() != ElementKind.INTERFACE) {
                error(annotatedElement, "@RRepository can only be used on interfaces");
                continue;
            }
            if (annotatedElement instanceof TypeElement typeElement) {
                try {
                    ProcessorClassInfo processorClassInfo = ProcessorClassInfo.init(
                            typeElement,
                            processingEnv.getTypeUtils(),
                            processingEnv.getElementUtils()
                    );
                    generateCode(processorClassInfo);
                } catch (Exception e) {
                    this.processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            ExceptionUtils.getStackTrace(e)
                    );
                }
            }
        }
        return true;
    }

    public void generateCode(ProcessorClassInfo processorClassInfo) throws IOException {
        logProcessing(processorClassInfo);

        List<TypeMirror> genericClasses = TypeHierarchyAnalyzer.getGenericTypeOfExtendingInterface(
                processingEnv.getElementUtils(),
                processingEnv.getTypeUtils(),
                processorClassInfo.getClazz(),
                ReactiveRepository.class.getName()
        );
        if (genericClasses.isEmpty()) {
            error(processorClassInfo.getClazz(), "@RRepository interface must extend vn.io.lcx.reactive.repository.ReactiveRepository<T>");
            return;
        }

        String repositoryTemplate = loadTemplate("template/repository-template.txt");
        String methodTemplate = loadTemplate("template/method-template.txt");

        final TypeMirror entityTypeMirror = genericClasses.get(0);
        final var entityTypeElement = TypeHierarchyAnalyzer.getTypeElementFromClassName(
                processingEnv.getElementUtils(),
                entityTypeMirror.toString()
        );
        if (entityTypeElement == null) {
            error(processorClassInfo.getClazz(), "Cannot resolve ReactiveRepository entity type: " + entityTypeMirror);
            return;
        }

        if (!validateRepository(processorClassInfo, entityTypeMirror)) {
            return;
        }

        StringBuilder methodCodeBody = new StringBuilder("\n");

        processorClassInfo.getMethods().forEach((methodInfo, executableElement) -> {
            String methodCode = generateMethodCode(methodInfo, executableElement, entityTypeMirror, entityTypeElement, methodTemplate);
            if (methodCode != null) {
                methodCodeBody.append(methodCode).append("\n");
            }
        });

        writeGeneratedClass(processorClassInfo, repositoryTemplate, methodCodeBody.toString());
    }

    private void logProcessing(ProcessorClassInfo processorClassInfo) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                vn.io.lcx.common.utils.DateTimeUtils.toUnixMillis(
                        vn.io.lcx.common.utils.DateTimeUtils.generateCurrentTimeDefault()
                ) + ": " + String.format(
                        "Generating code for reactive repository : %s",
                        processorClassInfo.getClazz().getQualifiedName()
                )
        );
    }

    private String loadTemplate(String templatePath) {
        String template = FileUtils.readResourceFileAsText(this.getClass().getClassLoader(), templatePath);
        assert StringUtils.isNotBlank(template);
        return template;
    }

    private String generateMethodCode(MethodInfo methodInfo, ExecutableElement executableElement,
                                      TypeMirror entityTypeMirror, TypeElement entityTypeElement,
                                      String methodTemplate) {
        // Skip default interface methods - they have implementation in ReactiveRepository interface
        String methodName = methodInfo.getMethodName();
        if (methodName.equals("find") || methodName.equals("findOne") || methodName.equals("findFirst")) {
            return null;
        }

        final String actualReturnType = resolveReturnType(methodInfo, entityTypeMirror);
        final var codeLines = new ArrayList<String>();

        final ReactiveReturnTypeModel returnTypeModel = buildReturnTypeModel(executableElement, entityTypeMirror);
        final VariableElement contextVariable = methodInfo.getInputParameters().get(0);
        final VariableElement sqlConnectionVariable = methodInfo.getInputParameters().get(1);
        final List<VariableElement> actualParameters = extractActualParameters(methodInfo);

        codeLines.add(String.format("String databaseName = %s.databaseMetadata().productName();",
                sqlConnectionVariable.getSimpleName()));

        generateMethodBody(methodInfo, executableElement, entityTypeMirror, entityTypeElement,
                codeLines, returnTypeModel, contextVariable, sqlConnectionVariable, actualParameters);

        return buildMethodFromTemplate(methodTemplate, actualReturnType, methodInfo, codeLines, entityTypeMirror);
    }

    private String resolveReturnType(MethodInfo methodInfo, TypeMirror entityTypeMirror) {
        String outputType = methodInfo.getOutputParameter().toString();
        if (outputType.equals("T")) {
            return entityTypeMirror.toString();
        } else if (outputType.contains("<T>")) {
            return outputType.replace("<T>", "<" + entityTypeMirror.toString() + ">");
        }
        return outputType;
    }

    private String extractFutureOutputType(String actualReturnType) {
        return MyStringUtils.removeSuffixOfString(
                MyStringUtils.removePrefixOfString(actualReturnType, "io.vertx.core.Future<"),
                ">"
        );
    }

    private List<VariableElement> extractActualParameters(MethodInfo methodInfo) {
        List<VariableElement> actualParameters = new ArrayList<>();
        for (int i = 2; i < methodInfo.getInputParameters().size(); i++) {
            actualParameters.add(methodInfo.getInputParameters().get(i));
        }
        return actualParameters;
    }

    private void generateMethodBody(MethodInfo methodInfo, ExecutableElement executableElement,
                                    TypeMirror entityTypeMirror, TypeElement entityTypeElement,
                                    List<String> codeLines, ReactiveReturnTypeModel returnTypeModel,
                                    VariableElement contextVariable, VariableElement sqlConnectionVariable,
                                    List<VariableElement> actualParameters) {

        codeLines.add(String.format("io.vertx.core.Future<%s> future;", returnTypeModel.futureOutputType));

        boolean isReadOnly = entityTypeElement.getAnnotation(ReadOnly.class) != null;

        switch (methodInfo.getMethodName()) {
            case "save":
                if (isReadOnly) {
                    codeLines.add("return io.vertx.core.Future.succeededFuture(null);");
                } else {
                    buildSaveMethodCodeBody(codeLines, contextVariable, sqlConnectionVariable, entityTypeMirror);
                }
                break;
            case "update":
                if (isReadOnly) {
                    codeLines.add("return io.vertx.core.Future.succeededFuture(null);");
                } else {
                    buildUpdateMethodCodeBody(codeLines, contextVariable, sqlConnectionVariable, entityTypeMirror);
                }
                break;
            case "delete":
                if (isReadOnly) {
                    codeLines.add("return io.vertx.core.Future.succeededFuture(null);");
                } else {
                    buildDeleteMethodCodeBody(codeLines, contextVariable, sqlConnectionVariable, entityTypeMirror);
                }
                break;
            case "saveAll":
                if (isReadOnly) {
                    codeLines.add("return io.vertx.core.Future.succeededFuture(java.util.Collections.emptyList());");
                } else {
                    buildBatchSaveMethodCodeBody(codeLines, contextVariable, sqlConnectionVariable, entityTypeMirror);
                }
                break;
            case "updateAll":
                if (isReadOnly) {
                    codeLines.add("return io.vertx.core.Future.succeededFuture(0);");
                } else {
                    buildBatchUpdateMethodCodeBody(codeLines, contextVariable, sqlConnectionVariable, entityTypeMirror);
                }
                break;
            case "deleteAll":
                if (isReadOnly) {
                    codeLines.add("return io.vertx.core.Future.succeededFuture(0);");
                } else {
                    buildBatchDeleteMethodCodeBody(codeLines, contextVariable, sqlConnectionVariable, entityTypeMirror);
                }
                break;
            case "find":
            case "findOne":
            case "findFirst":
                // These should be skipped earlier in generateMethodCode - this is a fallback
                break;
            default:
                if (executableElement.getAnnotation(Query.class) != null) {
                    buildQueryMethodCodeBody(executableElement, codeLines, contextVariable, sqlConnectionVariable,
                            actualParameters, returnTypeModel);
                } else {
                    codeLines.clear();
                    codeLines.add(String.format("throw new vn.io.lcx.jpa.exception.CodeGenError(\"%s\");", ERROR_UNSUPPORTED_METHOD));
                }
                break;
        }
    }

    private String buildMethodFromTemplate(String methodTemplate, String actualReturnType,
                                           MethodInfo methodInfo, List<String> codeLines, TypeMirror entityTypeMirror) {
        return methodTemplate
                .replace("${return-type}", actualReturnType)
                .replace("${method-name}", methodInfo.getMethodName())
                .replace("${list-of-parameters}", formatParameterList(methodInfo, entityTypeMirror))
                .replace("${method-body}", formatCodeBody(codeLines));
    }

    private String formatParameterList(MethodInfo methodInfo, TypeMirror entityTypeMirror) {
        return methodInfo.getInputParameters().stream()
                .map(variableElement -> {
                    String paramType = variableElement.asType().toString();
                    if (paramType.equals("T")) {
                        return entityTypeMirror.toString() + " model";
                    } else if (paramType.equals("java.util.List<T>")) {
                        return "java.util.List<" + entityTypeMirror.toString() + "> entities";
                    } else {
                        return String.format("%s %s", paramType, variableElement.getSimpleName());
                    }
                })
                .collect(Collectors.joining(", "));
    }

    private String formatCodeBody(List<String> codeLines) {
        return codeLines.stream().collect(Collectors.joining(
                "\n        ",
                CommonConstant.EMPTY_STRING,
                CommonConstant.EMPTY_STRING
        ));
    }

    private void writeGeneratedClass(ProcessorClassInfo processorClassInfo, String repositoryTemplate, String methodsCode) throws IOException {
        final var packageName = processingEnv.getElementUtils()
                .getPackageOf(processorClassInfo.getClazz())
                .getQualifiedName()
                .toString();
        final var className = processorClassInfo.getClazz().getSimpleName() + "Impl";
        final var code = repositoryTemplate
                .replace("${package-name}", packageName)
                .replace("${proxy-class-name}", className)
                .replace("${interface-class-name}", processorClassInfo.getClazz().getSimpleName())
                .replace("${methods}", MyStringUtils.removeSuffixOfString(methodsCode, "\n"));

        String fullClassName = packageName + "." + className;
        JavaFileObject builderFile = this.processingEnv.getFiler().createSourceFile(fullClassName);
        try (Writer writer = builderFile.openWriter()) {
            writer.write(code);
        }
    }

    // ========== CRUD Method Code Generation ==========

    private void buildSaveMethodCodeBody(List<String> codeLines, VariableElement contextVariable,
                                         VariableElement sqlConnectionVariable, TypeMirror entityTypeMirror) {
        String contextVar = contextVariable.getSimpleName().toString();
        String sqlConnVar = sqlConnectionVariable.getSimpleName().toString();
        String entityType = entityTypeMirror.toString();

        List<String> generatedCode = ReactiveCodeGenHelper.generateDatabaseSpecificCrudCode(
                sqlConnVar, contextVar, entityType, ReactiveCodeGenHelper.CrudOperationType.INSERT);
        codeLines.addAll(generatedCode);
    }

    private void buildUpdateMethodCodeBody(List<String> codeLines, VariableElement contextVariable,
                                           VariableElement sqlConnectionVariable, TypeMirror entityTypeMirror) {
        String contextVar = contextVariable.getSimpleName().toString();
        String sqlConnVar = sqlConnectionVariable.getSimpleName().toString();
        String entityType = entityTypeMirror.toString();

        List<String> generatedCode = ReactiveCodeGenHelper.generateDatabaseSpecificCrudCode(
                sqlConnVar, contextVar, entityType, ReactiveCodeGenHelper.CrudOperationType.UPDATE);
        codeLines.addAll(generatedCode);
    }

    private void buildDeleteMethodCodeBody(List<String> codeLines, VariableElement contextVariable,
                                           VariableElement sqlConnectionVariable, TypeMirror entityTypeMirror) {
        String contextVar = contextVariable.getSimpleName().toString();
        String sqlConnVar = sqlConnectionVariable.getSimpleName().toString();
        String entityType = entityTypeMirror.toString();

        List<String> generatedCode = ReactiveCodeGenHelper.generateDatabaseSpecificCrudCode(
                sqlConnVar, contextVar, entityType, ReactiveCodeGenHelper.CrudOperationType.DELETE);
        codeLines.addAll(generatedCode);
    }

    // ========== Batch CRUD Method Code Generation ==========

    private void buildBatchSaveMethodCodeBody(List<String> codeLines, VariableElement contextVariable,
                                              VariableElement sqlConnectionVariable, TypeMirror entityTypeMirror) {
        String contextVar = contextVariable.getSimpleName().toString();
        String sqlConnVar = sqlConnectionVariable.getSimpleName().toString();
        String entityType = entityTypeMirror.toString();

        List<String> generatedCode = ReactiveCodeGenHelper.generateBatchCrudCode(
                sqlConnVar, contextVar, entityType, ReactiveCodeGenHelper.CrudOperationType.BATCH_INSERT);
        codeLines.addAll(generatedCode);
    }

    private void buildBatchUpdateMethodCodeBody(List<String> codeLines, VariableElement contextVariable,
                                                VariableElement sqlConnectionVariable, TypeMirror entityTypeMirror) {
        String contextVar = contextVariable.getSimpleName().toString();
        String sqlConnVar = sqlConnectionVariable.getSimpleName().toString();
        String entityType = entityTypeMirror.toString();

        List<String> generatedCode = ReactiveCodeGenHelper.generateBatchCrudCode(
                sqlConnVar, contextVar, entityType, ReactiveCodeGenHelper.CrudOperationType.BATCH_UPDATE);
        codeLines.addAll(generatedCode);
    }

    private void buildBatchDeleteMethodCodeBody(List<String> codeLines, VariableElement contextVariable,
                                                VariableElement sqlConnectionVariable, TypeMirror entityTypeMirror) {
        String contextVar = contextVariable.getSimpleName().toString();
        String sqlConnVar = sqlConnectionVariable.getSimpleName().toString();
        String entityType = entityTypeMirror.toString();

        List<String> generatedCode = ReactiveCodeGenHelper.generateBatchCrudCode(
                sqlConnVar, contextVar, entityType, ReactiveCodeGenHelper.CrudOperationType.BATCH_DELETE);
        codeLines.addAll(generatedCode);
    }

    // ========== Query Method Code Generation ==========

    private void buildQueryMethodCodeBody(ExecutableElement executableElement, List<String> codeLines,
                                          VariableElement contextVariable, VariableElement sqlConnectionVariable,
                                          List<VariableElement> actualParameters,
                                          ReactiveReturnTypeModel returnTypeModel) {
        String contextVar = contextVariable.getSimpleName().toString();
        String sqlConnVar = sqlConnectionVariable.getSimpleName().toString();

        ReactiveCodeGenHelper.addStartingTimeCode(codeLines);
        codeLines.add("java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);");
        ReactiveCodeGenHelper.addPlaceholderResolution(codeLines);

        if (lastParameterIsPageable(actualParameters)) {
            String pageableParam = actualParameters.get(actualParameters.size() - 1).getSimpleName().toString();
            codeLines.add(String.format("if (%s.getEntityClass() == null) {", pageableParam));
            codeLines.add(String.format("    %s.setEntityClass(${{class}}.class);", pageableParam));
            codeLines.add("}");
        }

        final var statement = executableElement.getAnnotation(Query.class);
        final var queryPlan = buildQueryPlan(statement.value(), actualParameters, executableElement);

        addSqlAndTupleCode(codeLines, queryPlan, "sql", "tuple");
        if (lastParameterIsPageable(actualParameters)) {
            String pageableParam = actualParameters.get(actualParameters.size() - 1).getSimpleName().toString();
            codeLines.add(String.format("sql.append(%s.toSql());", pageableParam));
        }

        addQueryExecution(codeLines, sqlConnVar, contextVar, queryPlan, "sql", "tuple");
        addResultMapping(codeLines, contextVar, returnTypeModel, queryPlan, actualParameters, sqlConnVar, statement);

        codeLines.add("        });");
    }

    private void addSqlAndTupleCode(List<String> codeLines, QueryPlan queryPlan, String sqlVar, String tupleVar) {
        codeLines.add(String.format("StringBuilder %s = new StringBuilder();", sqlVar));
        codeLines.add(String.format("io.vertx.sqlclient.Tuple %s = io.vertx.sqlclient.Tuple.tuple();", tupleVar));
        for (QueryPart part : queryPlan.parts) {
            if (part.text != null) {
                if (!part.text.isEmpty()) {
                    codeLines.add(String.format("%s.append(\"%s\");", sqlVar, escapeJava(part.text)));
                }
                continue;
            }
            String paramName = part.binding.parameterName;
            if (part.binding.collectionExpansion) {
                addExpandedSqlPlaceholders(codeLines, sqlVar, paramName, part.binding.array);
            } else {
                codeLines.add(String.format("%s.append(placeholder.equals(\"?\") ? \"?\" : placeholder + count.incrementAndGet());", sqlVar));
            }
        }
        for (QueryBinding binding : queryPlan.bindings) {
            if (binding.collectionExpansion) {
                addExpandedTupleValues(codeLines, tupleVar, binding.parameterName);
            } else {
                codeLines.add(String.format("%s.addValue(%s);", tupleVar, binding.parameterName));
            }
        }
    }

    private void addExpandedSqlPlaceholders(List<String> codeLines, String sqlVar, String paramName, boolean array) {
        String sizeExpr = array ? paramName + ".length" : paramName + ".size()";
        if (array) {
            codeLines.add(String.format("if (%s == null || %s.length == 0) {", paramName, paramName));
        } else {
            codeLines.add(String.format("if (%s == null || %s.isEmpty()) {", paramName, paramName));
        }
        codeLines.add(String.format("    throw new java.lang.NullPointerException(\"%s\");", paramName));
        codeLines.add("}");
        codeLines.add(String.format("for (int i = 0; i < %s; i++) {", sizeExpr));
        codeLines.add("    if (i > 0) {");
        codeLines.add(String.format("        %s.append(\", \");", sqlVar));
        codeLines.add("    }");
        codeLines.add(String.format("    %s.append(placeholder.equals(\"?\") ? \"?\" : placeholder + count.incrementAndGet());", sqlVar));
        codeLines.add("}");
    }

    private void addExpandedTupleValues(List<String> codeLines, String tupleVar, String paramName) {
        codeLines.add(String.format("for (Object item : %s) {", paramName));
        codeLines.add(String.format("    %s.addValue(item);", tupleVar));
        codeLines.add("}");
    }

    private void addQueryExecution(List<String> codeLines, String sqlConnVar, String contextVar,
                                   QueryPlan queryPlan, String sqlVar, String tupleVar) {
        codeLines.add(String.format(
                "return vn.io.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%s, %s).preparedQuery(%s.toString())",
                sqlConnVar, contextVar, sqlVar));
        if (queryPlan.bindings.isEmpty()) {
            codeLines.add("        .execute()");
        } else {
            codeLines.add(String.format("        .execute(%s)", tupleVar));
        }
        codeLines.add("        .map(rowSet -> {");
    }

    private void addResultMapping(List<String> codeLines, String contextVar, ReactiveReturnTypeModel returnTypeModel,
                                  QueryPlan queryPlan, List<VariableElement> actualParameters,
                                  String sqlConnVar, Query statement) {
        switch (returnTypeModel.kind) {
            case LIST_ENTITY:
            case PAGE_ENTITY:
                addEntityListResultMapping(codeLines, contextVar, returnTypeModel.elementType);
                if (returnTypeModel.kind == ReturnKind.PAGE_ENTITY) {
                    addPageableCountQuery(codeLines, sqlConnVar, contextVar, queryPlan, actualParameters, statement);
                }
                break;
            case LIST_SCALAR:
                addScalarListResultMapping(codeLines, contextVar, returnTypeModel);
                break;
            case SCALAR:
            case OPTIONAL_SCALAR:
                addScalarResultMapping(codeLines, contextVar, returnTypeModel, queryPlan);
                break;
            case OBJECT_ARRAY:
                addObjectArrayResultMapping(codeLines, contextVar);
                break;
            case OPTIONAL_ENTITY:
            case ENTITY:
            default:
                addEntityResultMapping(codeLines, contextVar, returnTypeModel.elementType, returnTypeModel.kind == ReturnKind.OPTIONAL_ENTITY);
                break;
        }
    }

    private void addEntityListResultMapping(List<String> codeLines, String contextVar, String elementType) {
        codeLines.replaceAll(s -> s.replace("${{class}}", elementType));
        codeLines.add(String.format("            final java.util.List<%s> result = new java.util.ArrayList<>();", elementType));
        codeLines.add("            for (io.vertx.sqlclient.Row row : rowSet) {");
        codeLines.add(String.format("                result.add(%sUtils.vertxRowMapping(row));", elementType));
        codeLines.add("            }");
        ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 12);
        codeLines.add("            return result;");
    }

    private void addScalarListResultMapping(List<String> codeLines, String contextVar, ReactiveReturnTypeModel returnTypeModel) {
        codeLines.add(String.format("            final java.util.List<%s> result = new java.util.ArrayList<>();", returnTypeModel.elementType));
        codeLines.add("            for (io.vertx.sqlclient.Row row : rowSet) {");
        codeLines.add(String.format("                result.add(%s);", scalarReadExpression(returnTypeModel.elementType, "row")));
        codeLines.add("            }");
        ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 12);
        codeLines.add("            return result;");
    }

    private void addPageableCountQuery(List<String> codeLines, String sqlConnVar, String contextVar,
                                       QueryPlan queryPlan, List<VariableElement> actualParameters, Query statement) {
        String pageableParam = actualParameters.get(actualParameters.size() - 1).getSimpleName().toString();
        String countQuery = StringUtils.isNotBlank(statement.countQuery())
                ? statement.countQuery()
                : buildAutoCountQuery(queryPlan.rawSql);
        QueryPlan countPlan = buildQueryPlan(countQuery, actualParameters, null);

        codeLines.add("        }).compose(rs -> {");
        codeLines.add("            count.set(0);");
        addSqlAndTupleCode(codeLines, countPlan, "countSql", "countTuple");
        codeLines.add(String.format(
                "            return vn.io.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%s, %s).preparedQuery(countSql.toString())",
                sqlConnVar, contextVar));
        if (countPlan.bindings.isEmpty()) {
            codeLines.add("                    .execute()");
        } else {
            codeLines.add("                    .execute(countTuple)");
        }

        codeLines.add("                    .map(rowSet -> {");
        codeLines.add("                        long countRs = 0L;");
        codeLines.add("                        for (io.vertx.sqlclient.Row row : rowSet) {");
        codeLines.add("                            java.lang.Object countValue = row.getValue(0);");
        codeLines.add("                            if (countValue instanceof java.lang.Number number) {");
        codeLines.add("                                countRs = number.longValue();");
        codeLines.add("                            }");
        codeLines.add("                            break;");
        codeLines.add("                        }");
        codeLines.add(String.format(
                "                        return vn.io.lcx.common.database.pageable.Page.create(rs, countRs, %s.getPageNumber(), %s.getPageSize());",
                pageableParam, pageableParam));
        codeLines.add("                    });");
    }

    private void addScalarResultMapping(List<String> codeLines, String contextVar,
                                        ReactiveReturnTypeModel returnTypeModel, QueryPlan queryPlan) {
        if (isModifyingQuery(queryPlan.rawSql) && isNumericScalar(returnTypeModel.elementType)) {
            ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 12);
            codeLines.add(returnTypeModel.elementType.equals("java.lang.Long")
                    ? "            return (long) rowSet.rowCount();"
                    : "            return rowSet.rowCount();");
            return;
        }
        if (returnTypeModel.kind == ReturnKind.SCALAR && isNumericScalar(returnTypeModel.elementType)) {
            String resultType = returnTypeModel.elementType.equals("java.lang.Long") ? "long" : "int";
            String getMethod = returnTypeModel.elementType.equals("java.lang.Long") ? "getLong" : "getInteger";
            codeLines.add(String.format("            %s result = 0;", resultType));
            codeLines.add("            for (io.vertx.sqlclient.Row row : rowSet) {");
            codeLines.add(String.format("                result += row.%s(0);", getMethod));
            codeLines.add("            }");
            ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 12);
            codeLines.add("            return result;");
            return;
        }

        codeLines.add("            if (rowSet.size() == 0) {");
        ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 16);
        codeLines.add(returnTypeModel.kind == ReturnKind.OPTIONAL_SCALAR
                ? "                return java.util.Optional.empty();"
                : "                return null;");
        codeLines.add("            }");
        codeLines.add("            for (io.vertx.sqlclient.Row row : rowSet) {");
        ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 16);
        if (returnTypeModel.kind == ReturnKind.OPTIONAL_SCALAR) {
            codeLines.add(String.format("                return java.util.Optional.ofNullable(%s);",
                    scalarReadExpression(returnTypeModel.elementType, "row")));
        } else {
            codeLines.add(String.format("                return %s;", scalarReadExpression(returnTypeModel.elementType, "row")));
        }
        codeLines.add("            }");
        codeLines.add(returnTypeModel.kind == ReturnKind.OPTIONAL_SCALAR
                ? "            return java.util.Optional.empty();"
                : "            return null;");
    }

    private boolean isModifyingQuery(String statement) {
        String normalized = removeLeadingSqlComments(statement).stripLeading().toLowerCase();
        return normalized.startsWith("update") || normalized.startsWith("insert") || normalized.startsWith("delete");
    }

    private void addObjectArrayResultMapping(List<String> codeLines, String contextVar) {
        codeLines.add("            java.util.List<java.lang.Object> objects = new java.util.ArrayList<>();");
        codeLines.add("            for (io.vertx.sqlclient.Row row : rowSet) {");
        codeLines.add("                for (int i = 0; i < row.size(); i++) {");
        codeLines.add("                    objects.add(row.getValue(i));");
        codeLines.add("                }");
        codeLines.add("            }");
        ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 12);
        codeLines.add("            return objects.toArray(java.lang.Object[]::new);");
    }

    private void addEntityResultMapping(List<String> codeLines, String contextVar, String genericType, boolean isOptional) {
        codeLines.replaceAll(s -> s.replace("${{class}}", genericType));

        codeLines.add("            if (rowSet.size() == 0) {");
        ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 16);
        codeLines.add(isOptional ? "                return java.util.Optional.empty();" : "                return null;");
        codeLines.add("            }");

        codeLines.add("            if (rowSet.size() > 1) {");
        ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 16);
        codeLines.add("                throw new vn.io.lcx.reactive.exception.NonUniqueQueryResult();");
        codeLines.add("            }");

        codeLines.add(String.format("            final java.util.List<%s> result = new java.util.ArrayList<>();", genericType));
        codeLines.add("            for (io.vertx.sqlclient.Row row : rowSet) {");
        codeLines.add(String.format("                result.add(%sUtils.vertxRowMapping(row));", genericType));
        codeLines.add("            }");
        ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 12);
        codeLines.add(isOptional
                ? "            return java.util.Optional.of(result.get(0));"
                : "            return result.isEmpty() ? null : result.get(0);");
    }

    private boolean validateRepository(ProcessorClassInfo processorClassInfo, TypeMirror entityTypeMirror) {
        boolean valid = true;
        for (var entry : processorClassInfo.getMethods().entrySet()) {
            MethodInfo methodInfo = entry.getKey();
            ExecutableElement method = entry.getValue();
            if (shouldSkipValidation(methodInfo, method)) {
                continue;
            }
            valid &= validateMethod(methodInfo, method, entityTypeMirror);
        }
        return valid;
    }

    private boolean shouldSkipValidation(MethodInfo methodInfo, ExecutableElement method) {
        Set<Modifier> modifiers = method.getModifiers();
        if (modifiers.contains(Modifier.DEFAULT) || modifiers.contains(Modifier.STATIC)) {
            return true;
        }
        return methodInfo.getMethodName().equals("find")
                || methodInfo.getMethodName().equals("findOne")
                || methodInfo.getMethodName().equals("findFirst");
    }

    private boolean validateMethod(MethodInfo methodInfo, ExecutableElement method, TypeMirror entityTypeMirror) {
        boolean valid = true;
        List<? extends VariableElement> params = method.getParameters();
        if (!isFuture(method.getReturnType())) {
            error(method, "@RRepository methods must return io.vertx.core.Future<X>");
            valid = false;
        }
        if (params.size() < 2) {
            error(method, ERROR_INVALID_PARAMETERS);
            valid = false;
        } else {
            if (!isAssignableTo(params.get(0).asType(), ROUTING_CONTEXT_TYPE)
                    || !isAssignableTo(params.get(1).asType(), SQL_CONNECTION_TYPE)) {
                error(method, ERROR_INVALID_PARAMETERS);
                valid = false;
            }
        }

        List<VariableElement> actualParameters = extractActualParameters(methodInfo);
        for (int i = 0; i < actualParameters.size() - 1; i++) {
            if (isPageable(actualParameters.get(i))) {
                error(method, "Pageable parameter must be the final repository method parameter");
                valid = false;
            }
        }

        ReactiveReturnTypeModel returnTypeModel = buildReturnTypeModel(method, entityTypeMirror);
        if (returnTypeModel.kind == ReturnKind.UNSUPPORTED) {
            error(method, returnTypeModel.error);
            valid = false;
        }
        if (returnTypeModel.kind == ReturnKind.OPTIONAL_LIST) {
            error(method, "Future<Optional<List<T>>> is not supported");
            valid = false;
        }
        if (returnTypeModel.kind == ReturnKind.PAGE_ENTITY && !lastParameterIsPageable(actualParameters)) {
            error(method, "Future<Page<T>> query methods must declare Pageable as the final parameter");
            valid = false;
        }
        if ((returnTypeModel.kind == ReturnKind.LIST_ENTITY || returnTypeModel.kind == ReturnKind.LIST_SCALAR)
                && lastParameterIsPageable(actualParameters)) {
            warning(method, "Using Pageable with Future<List<T>> is deprecated. Return Future<Page<T>> instead.");
        }
        if (returnTypeModel.kind == ReturnKind.OBJECT_ARRAY) {
            warning(method, "Future<Object[]> query mapping is deprecated and will be removed. Use a DTO, entity, or scalar return type.");
        }

        if (!isCrudMethod(methodInfo.getMethodName()) && method.getAnnotation(Query.class) == null) {
            error(method, ERROR_UNSUPPORTED_METHOD);
            valid = false;
        }
        Query query = method.getAnnotation(Query.class);
        if (query != null) {
            valid &= validateQuery(query.value(), actualParameters, method);
            if (returnTypeModel.kind == ReturnKind.PAGE_ENTITY) {
                if (StringUtils.isBlank(query.countQuery()) && isComplexCountQuery(query.value())) {
                    error(method, "@Query method returning Page<T> uses complex SQL and must define countQuery()");
                    valid = false;
                } else if (StringUtils.isNotBlank(query.countQuery())) {
                    valid &= validateQuery(query.countQuery(), actualParameters, method);
                }
            }
        }
        return valid;
    }

    private boolean validateQuery(String query, List<VariableElement> actualParameters, Element element) {
        QueryPlan plan = buildQueryPlan(query, actualParameters, element);
        return plan.valid;
    }

    private ReactiveReturnTypeModel buildReturnTypeModel(ExecutableElement method, TypeMirror entityTypeMirror) {
        TypeMirror returnType = method.getReturnType();
        if (!(returnType instanceof DeclaredType futureType) || !isFuture(returnType)) {
            return ReactiveReturnTypeModel.unsupported("Return type must be io.vertx.core.Future<X>");
        }
        if (futureType.getTypeArguments().isEmpty()) {
            return ReactiveReturnTypeModel.unsupported("Future return type must declare an output type");
        }
        TypeMirror innerType = futureType.getTypeArguments().get(0);
        String futureOutputType = resolveTypeName(innerType, entityTypeMirror);
        return buildInnerReturnTypeModel(innerType, futureOutputType, entityTypeMirror);
    }

    private ReactiveReturnTypeModel buildInnerReturnTypeModel(TypeMirror innerType, String futureOutputType,
                                                              TypeMirror entityTypeMirror) {
        if (innerType instanceof ArrayType arrayType) {
            if (arrayType.getComponentType().toString().equals("java.lang.Object")) {
                return new ReactiveReturnTypeModel(ReturnKind.OBJECT_ARRAY, futureOutputType, futureOutputType);
            }
            return ReactiveReturnTypeModel.unsupported("Array query return types are not supported except Object[]");
        }
        if (innerType instanceof DeclaredType declaredType) {
            String rawType = declaredType.asElement().asType().toString();
            if (rawType.startsWith("java.util.List") && !declaredType.getTypeArguments().isEmpty()) {
                TypeMirror elementType = declaredType.getTypeArguments().get(0);
                String elementTypeName = resolveTypeName(elementType, entityTypeMirror);
                ReturnKind kind = isScalarType(elementTypeName) ? ReturnKind.LIST_SCALAR : ReturnKind.LIST_ENTITY;
                return new ReactiveReturnTypeModel(kind, futureOutputType, elementTypeName);
            }
            if (rawType.startsWith("vn.io.lcx.common.database.pageable.Page") && !declaredType.getTypeArguments().isEmpty()) {
                TypeMirror elementType = declaredType.getTypeArguments().get(0);
                String elementTypeName = resolveTypeName(elementType, entityTypeMirror);
                if (isScalarType(elementTypeName)) {
                    return ReactiveReturnTypeModel.unsupported("Future<Page<T>> supports entity mapping only");
                }
                return new ReactiveReturnTypeModel(ReturnKind.PAGE_ENTITY, futureOutputType, elementTypeName);
            }
            if (rawType.startsWith("java.util.Optional") && !declaredType.getTypeArguments().isEmpty()) {
                TypeMirror elementType = declaredType.getTypeArguments().get(0);
                String elementTypeName = resolveTypeName(elementType, entityTypeMirror);
                if (elementType instanceof DeclaredType optionalInner
                        && optionalInner.asElement().asType().toString().startsWith("java.util.List")) {
                    return new ReactiveReturnTypeModel(ReturnKind.OPTIONAL_LIST, futureOutputType, elementTypeName);
                }
                ReturnKind kind = isScalarType(elementTypeName) ? ReturnKind.OPTIONAL_SCALAR : ReturnKind.OPTIONAL_ENTITY;
                return new ReactiveReturnTypeModel(kind, futureOutputType, elementTypeName);
            }
        }
        String outputType = resolveTypeName(innerType, entityTypeMirror);
        if (isScalarType(outputType)) {
            return new ReactiveReturnTypeModel(ReturnKind.SCALAR, futureOutputType, outputType);
        }
        if (innerType.getKind().isPrimitive()) {
            return ReactiveReturnTypeModel.unsupported("Primitive query return types are not supported inside Future; use wrapper types");
        }
        if (outputType.startsWith("java.")) {
            return ReactiveReturnTypeModel.unsupported("Unsupported query return type: " + outputType);
        }
        return new ReactiveReturnTypeModel(ReturnKind.ENTITY, futureOutputType, outputType);
    }

    private QueryPlan buildQueryPlan(String statementValue, List<VariableElement> actualParameters, Element diagnosticElement) {
        String rawSql = removeTrailingSemicolon(statementValue);
        QueryPlan plan = new QueryPlan(rawSql);
        StringBuilder text = new StringBuilder();
        PlaceholderStyle style = PlaceholderStyle.NONE;
        int implicitIndex = 0;
        boolean inString = false;

        for (int i = 0; i < rawSql.length(); i++) {
            char current = rawSql.charAt(i);
            if (current == '\'') {
                text.append(current);
                if (inString && i + 1 < rawSql.length() && rawSql.charAt(i + 1) == '\'') {
                    text.append(rawSql.charAt(++i));
                } else {
                    inString = !inString;
                }
                continue;
            }
            if (current != '?' || inString) {
                text.append(current);
                continue;
            }

            int placeholderEnd = i + 1;
            while (placeholderEnd < rawSql.length() && Character.isDigit(rawSql.charAt(placeholderEnd))) {
                placeholderEnd++;
            }
            boolean explicit = placeholderEnd > i + 1;
            PlaceholderStyle currentStyle = explicit ? PlaceholderStyle.EXPLICIT : PlaceholderStyle.IMPLICIT;
            if (style == PlaceholderStyle.NONE) {
                style = currentStyle;
            } else if (style != currentStyle) {
                plan.error("Do not mix implicit ? and indexed ?1 query placeholders", diagnosticElement);
            }

            int parameterIndex;
            if (explicit) {
                parameterIndex = Integer.parseInt(rawSql.substring(i + 1, placeholderEnd)) - 1;
                if (parameterIndex < 0) {
                    plan.error("Query placeholder indexes are 1-based", diagnosticElement);
                }
            } else {
                parameterIndex = implicitIndex++;
            }
            if (parameterIndex >= actualQueryParameterCount(actualParameters)) {
                plan.error("Query placeholder index exceeds repository method parameter count", diagnosticElement);
                parameterIndex = Math.max(0, actualQueryParameterCount(actualParameters) - 1);
            }

            if (text.length() > 0) {
                plan.parts.add(QueryPart.text(text.toString()));
                text.setLength(0);
            }

            VariableElement parameter = actualParameters.isEmpty() ? null : actualParameters.get(parameterIndex);
            if (parameter != null) {
                boolean collection = isCollectionOrArray(parameter);
                boolean inExpansion = isInPlaceholder(rawSql, i);
                if (collection && !inExpansion) {
                    plan.error("Collection query parameters are only supported inside IN (?) or IN (?1)", diagnosticElement);
                }
                if (!collection && inExpansion) {
                    plan.error("IN collection expansion requires a collection or object array parameter", diagnosticElement);
                }
                if (isPrimitiveArray(parameter)) {
                    plan.error("Primitive arrays are not supported for query binding; use List<Wrapper> instead", diagnosticElement);
                }
                QueryBinding binding = new QueryBinding(
                        parameterIndex,
                        parameter.getSimpleName().toString(),
                        collection && inExpansion,
                        parameter.asType().getKind() == TypeKind.ARRAY
                );
                plan.parts.add(QueryPart.placeholder(binding));
                plan.bindings.add(binding);
            }
            i = placeholderEnd - 1;
        }
        if (text.length() > 0) {
            plan.parts.add(QueryPart.text(text.toString()));
        }
        validateAllQueryParametersUsed(plan, actualParameters, diagnosticElement);
        return plan;
    }

    private void validateAllQueryParametersUsed(QueryPlan plan, List<VariableElement> actualParameters, Element diagnosticElement) {
        boolean[] used = new boolean[actualQueryParameterCount(actualParameters)];
        for (QueryBinding binding : plan.bindings) {
            if (binding.parameterIndex >= 0 && binding.parameterIndex < used.length) {
                used[binding.parameterIndex] = true;
            }
        }
        for (int i = 0; i < used.length; i++) {
            if (!used[i]) {
                plan.error("Query parameter '" + actualParameters.get(i).getSimpleName() + "' is not referenced by any placeholder", diagnosticElement);
            }
        }
    }

    private int actualQueryParameterCount(List<VariableElement> actualParameters) {
        if (lastParameterIsPageable(actualParameters)) {
            return actualParameters.size() - 1;
        }
        return actualParameters.size();
    }

    private boolean isInPlaceholder(String sql, int placeholderIndex) {
        int cursor = placeholderIndex - 1;
        while (cursor >= 0 && Character.isWhitespace(sql.charAt(cursor))) {
            cursor--;
        }
        if (cursor >= 0 && sql.charAt(cursor) == '(') {
            cursor--;
        }
        while (cursor >= 0 && Character.isWhitespace(sql.charAt(cursor))) {
            cursor--;
        }
        int end = cursor + 1;
        while (cursor >= 0 && Character.isLetter(sql.charAt(cursor))) {
            cursor--;
        }
        String previousWord = sql.substring(cursor + 1, end);
        return previousWord.equalsIgnoreCase("IN");
    }

    private String buildAutoCountQuery(String sql) {
        String cleaned = removeTrailingSemicolon(sql);
        int fromIndex = findTopLevelKeyword(cleaned, "from", 0);
        if (fromIndex < 0) {
            return "SELECT COUNT(1)";
        }
        int orderByIndex = findTopLevelKeyword(cleaned, "order by", fromIndex);
        String fromClause = orderByIndex >= 0 ? cleaned.substring(fromIndex, orderByIndex) : cleaned.substring(fromIndex);
        return "SELECT COUNT(1) " + fromClause.strip();
    }

    private boolean isComplexCountQuery(String sql) {
        String cleaned = stripQuotedSql(sql).toLowerCase();
        return cleaned.contains(" with ")
                || cleaned.stripLeading().startsWith("with ")
                || cleaned.contains(" union ")
                || cleaned.contains(" group by ")
                || cleaned.contains(" having ")
                || cleaned.contains(" distinct ");
    }

    private int findTopLevelKeyword(String sql, String keyword, int start) {
        String lower = stripQuotedSql(sql).toLowerCase();
        int index = lower.indexOf(keyword.toLowerCase(), start);
        while (index >= 0) {
            boolean leftOk = index == 0 || !Character.isLetterOrDigit(lower.charAt(index - 1));
            int rightIndex = index + keyword.length();
            boolean rightOk = rightIndex >= lower.length() || !Character.isLetterOrDigit(lower.charAt(rightIndex));
            if (leftOk && rightOk) {
                return index;
            }
            index = lower.indexOf(keyword.toLowerCase(), index + 1);
        }
        return -1;
    }

    private String removeTrailingSemicolon(String sql) {
        int end = sql.length() - 1;
        while (end >= 0 && Character.isWhitespace(sql.charAt(end))) {
            end--;
        }
        if (end >= 0 && sql.charAt(end) == ';') {
            return sql.substring(0, end) + sql.substring(end + 1);
        }
        return sql;
    }

    private String stripQuotedSql(String sql) {
        StringBuilder builder = new StringBuilder(sql.length());
        boolean inString = false;
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            if (current == '\'') {
                builder.append(' ');
                if (inString && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    builder.append(' ');
                    i++;
                } else {
                    inString = !inString;
                }
            } else {
                builder.append(inString ? ' ' : current);
            }
        }
        return builder.toString();
    }

    private String removeLeadingSqlComments(String sql) {
        String result = sql;
        while (result.stripLeading().startsWith("--")) {
            int lineEnd = result.indexOf('\n');
            if (lineEnd < 0) {
                return CommonConstant.EMPTY_STRING;
            }
            result = result.substring(lineEnd + 1);
        }
        return result;
    }

    private String scalarReadExpression(String typeName, String rowVar) {
        return switch (typeName) {
            case "java.lang.String" -> rowVar + ".getString(0)";
            case "java.lang.Integer" -> rowVar + ".getInteger(0)";
            case "java.lang.Long" -> rowVar + ".getLong(0)";
            case "java.lang.Boolean" -> rowVar + ".getBoolean(0)";
            case "java.math.BigDecimal" -> "(java.math.BigDecimal) " + rowVar + ".getValue(0)";
            case "java.math.BigInteger" -> "(" + rowVar + ".getValue(0) == null ? null : new java.math.BigInteger(" + rowVar + ".getValue(0).toString()))";
            default -> rowVar + ".getValue(0)";
        };
    }

    private boolean isNumericScalar(String typeName) {
        return typeName.equals("java.lang.Integer") || typeName.equals("java.lang.Long");
    }

    private boolean isScalarType(String typeName) {
        return typeName.equals("java.lang.String")
                || typeName.equals("java.lang.Integer")
                || typeName.equals("java.lang.Long")
                || typeName.equals("java.lang.Boolean")
                || typeName.equals("java.math.BigDecimal")
                || typeName.equals("java.math.BigInteger");
    }

    private boolean isCrudMethod(String methodName) {
        return methodName.equals("save")
                || methodName.equals("update")
                || methodName.equals("delete")
                || methodName.equals("saveAll")
                || methodName.equals("updateAll")
                || methodName.equals("deleteAll");
    }

    private boolean isFuture(TypeMirror typeMirror) {
        return isDeclaredAssignableTo(typeMirror, "io.vertx.core.Future");
    }

    private boolean isAssignableTo(TypeMirror source, String targetClassName) {
        return isDeclaredAssignableTo(source, targetClassName);
    }

    private boolean isDeclaredAssignableTo(TypeMirror source, String targetClassName) {
        TypeElement targetElement = TypeHierarchyAnalyzer.getTypeElementFromClassName(
                processingEnv.getElementUtils(),
                targetClassName
        );
        if (targetElement == null) {
            return source.toString().equals(targetClassName);
        }
        Types types = processingEnv.getTypeUtils();
        return types.isAssignable(types.erasure(source), types.erasure(targetElement.asType()));
    }

    private boolean isCollectionOrArray(VariableElement parameter) {
        TypeMirror type = parameter.asType();
        return type.getKind() == TypeKind.ARRAY
                || isDeclaredAssignableTo(type, "java.util.Collection");
    }

    private boolean isPrimitiveArray(VariableElement parameter) {
        TypeMirror type = parameter.asType();
        return type instanceof ArrayType arrayType && arrayType.getComponentType().getKind().isPrimitive();
    }

    private String resolveTypeName(TypeMirror typeMirror, TypeMirror entityTypeMirror) {
        String value = typeMirror.toString();
        if (value.equals("T")) {
            return entityTypeMirror.toString();
        }
        return value.replace("<T>", "<" + entityTypeMirror + ">");
    }

    private String escapeJava(String value) {
        return StringEscapeUtils.escapeJava(value);
    }

    private void error(Element element, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void warning(Element element, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message, element);
    }

    private enum ReturnKind {
        ENTITY,
        OPTIONAL_ENTITY,
        LIST_ENTITY,
        PAGE_ENTITY,
        SCALAR,
        OPTIONAL_SCALAR,
        LIST_SCALAR,
        OBJECT_ARRAY,
        OPTIONAL_LIST,
        UNSUPPORTED
    }

    private enum PlaceholderStyle {
        NONE,
        IMPLICIT,
        EXPLICIT
    }

    private static final class ReactiveReturnTypeModel {
        private final ReturnKind kind;
        private final String futureOutputType;
        private final String elementType;
        private final String error;

        private ReactiveReturnTypeModel(ReturnKind kind, String futureOutputType, String elementType) {
            this(kind, futureOutputType, elementType, null);
        }

        private ReactiveReturnTypeModel(ReturnKind kind, String futureOutputType, String elementType, String error) {
            this.kind = kind;
            this.futureOutputType = futureOutputType;
            this.elementType = elementType;
            this.error = error;
        }

        private static ReactiveReturnTypeModel unsupported(String error) {
            return new ReactiveReturnTypeModel(ReturnKind.UNSUPPORTED, "java.lang.Object", "java.lang.Object", error);
        }
    }

    private final class QueryPlan {
        private final String rawSql;
        private final List<QueryPart> parts = new ArrayList<>();
        private final List<QueryBinding> bindings = new ArrayList<>();
        private boolean valid = true;

        private QueryPlan(String rawSql) {
            this.rawSql = rawSql;
        }

        private void error(String message, Element element) {
            valid = false;
            if (element != null) {
                ReactiveRepositoryProcessor.this.error(element, message);
            }
        }
    }

    private static final class QueryPart {
        private final String text;
        private final QueryBinding binding;

        private QueryPart(String text, QueryBinding binding) {
            this.text = text;
            this.binding = binding;
        }

        private static QueryPart text(String text) {
            return new QueryPart(text, null);
        }

        private static QueryPart placeholder(QueryBinding binding) {
            return new QueryPart(null, binding);
        }
    }

    private static final class QueryBinding {
        private final int parameterIndex;
        private final String parameterName;
        private final boolean collectionExpansion;
        private final boolean array;

        private QueryBinding(int parameterIndex, String parameterName, boolean collectionExpansion, boolean array) {
            this.parameterIndex = parameterIndex;
            this.parameterName = parameterName;
            this.collectionExpansion = collectionExpansion;
            this.array = array;
        }
    }

    // ========== Utility Methods ==========

    public boolean lastParameterIsPageable(List<VariableElement> actualParameters) {
        return !actualParameters.isEmpty() && isPageable(actualParameters.get(actualParameters.size() - 1));
    }

    private boolean isPageable(VariableElement variableElement) {
        return processingEnv.getTypeUtils().isAssignable(
                variableElement.asType(),
                TypeHierarchyAnalyzer.getTypeElementFromClassName(
                        processingEnv.getElementUtils(),
                        "vn.io.lcx.common.database.pageable.Pageable"
                ).asType()
        );
    }
}
