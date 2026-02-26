package vn.com.lcx.processor;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.ReadOnly;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.ExceptionUtils;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.MyStringUtils;
import vn.com.lcx.processor.utility.MethodInfo;
import vn.com.lcx.processor.utility.ProcessorClassInfo;
import vn.com.lcx.processor.utility.ReactiveCodeGenHelper;
import vn.com.lcx.processor.utility.TypeHierarchyAnalyzer;
import vn.com.lcx.reactive.annotation.Query;
import vn.com.lcx.reactive.annotation.RRepository;
import vn.com.lcx.reactive.repository.ReactiveRepository;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("vn.com.lcx.reactive.annotation.RRepository")
public class ReactiveRepositoryProcessor extends AbstractProcessor {

    // Error messages
    private static final String ERROR_INVALID_PARAMETERS = "First parameter must be a `io.vertx.ext.web.RoutingContext` and the second one must be a `io.vertx.sqlclient.SqlConnection`";
    private static final String ERROR_UNSUPPORTED_METHOD = "Unsupported method. The generator of this type of method were removed, please define method with @vn.com.lcx.reactive.annotation.Query and provide a SQL statement";

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

        String repositoryTemplate = loadTemplate("template/repository-template.txt");
        String methodTemplate = loadTemplate("template/method-template.txt");

        final TypeMirror entityTypeMirror = genericClasses.get(0);
        final var entityTypeElement = TypeHierarchyAnalyzer.getTypeElementFromClassName(
                processingEnv.getElementUtils(),
                entityTypeMirror.toString()
        );

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
                vn.com.lcx.common.utils.DateTimeUtils.toUnixMillis(
                        vn.com.lcx.common.utils.DateTimeUtils.generateCurrentTimeDefault()
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

        if (!validateParameters(methodInfo, codeLines)) {
            return buildMethodFromTemplate(methodTemplate, actualReturnType, methodInfo, codeLines, entityTypeMirror);
        }

        final String futureOutputType = extractFutureOutputType(actualReturnType);
        final boolean isReturningList = futureOutputType.contains("java.util.List");
        final VariableElement contextVariable = methodInfo.getInputParameters().get(0);
        final VariableElement sqlConnectionVariable = methodInfo.getInputParameters().get(1);
        final List<VariableElement> actualParameters = extractActualParameters(methodInfo);

        codeLines.add(String.format("String databaseName = %s.databaseMetadata().productName();",
                sqlConnectionVariable.getSimpleName()));

        generateMethodBody(methodInfo, executableElement, entityTypeMirror, entityTypeElement,
                codeLines, futureOutputType, isReturningList, contextVariable, sqlConnectionVariable, actualParameters);

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

    private boolean validateParameters(MethodInfo methodInfo, List<String> codeLines) {
        var params = methodInfo.getInputParameters();
        if (params.isEmpty() || params.size() < 2) {
            codeLines.add(String.format("throw new vn.com.lcx.jpa.exception.CodeGenError(\"%s\");", ERROR_INVALID_PARAMETERS));
            return false;
        }
        if (!params.get(0).asType().toString().equals(ROUTING_CONTEXT_TYPE) ||
                !params.get(1).asType().toString().equals(SQL_CONNECTION_TYPE)) {
            codeLines.add(String.format("throw new vn.com.lcx.jpa.exception.CodeGenError(\"%s\");", ERROR_INVALID_PARAMETERS));
            return false;
        }
        return true;
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
                                    List<String> codeLines, String futureOutputType, boolean isReturningList,
                                    VariableElement contextVariable, VariableElement sqlConnectionVariable,
                                    List<VariableElement> actualParameters) {

        codeLines.add(String.format("io.vertx.core.Future<%s> future;", futureOutputType));

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
                if (Optional.ofNullable(executableElement.getAnnotation(Query.class)).isPresent()) {
                    buildQueryMethodCodeBody(executableElement, codeLines, contextVariable, sqlConnectionVariable,
                            actualParameters, isReturningList, futureOutputType);
                } else {
                    codeLines.clear();
                    codeLines.add(String.format("throw new vn.com.lcx.jpa.exception.CodeGenError(\"%s\");", ERROR_UNSUPPORTED_METHOD));
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
                                          List<VariableElement> actualParameters, boolean isReturningList,
                                          String futureOutputType) {
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

        addParameterValidation(codeLines, actualParameters);

        final var statement = executableElement.getAnnotation(Query.class);
        final var finalStatementArray = processQueryStatement(statement.value(), actualParameters);
        final String queryStatement = String.join(" ", finalStatementArray).replace("\n", "\\n\" +\n                        \"");

        addQueryExecution(codeLines, sqlConnVar, contextVar, queryStatement, actualParameters);
        addResultMapping(codeLines, contextVar, isReturningList, futureOutputType, finalStatementArray,
                actualParameters, sqlConnVar);

        codeLines.add("        });");
    }

    private void addParameterValidation(List<String> codeLines, List<VariableElement> actualParameters) {
        for (VariableElement param : actualParameters) {
            if (param.asType().toString().contains("java.util.List")) {
                String paramName = param.getSimpleName().toString();
                codeLines.add(String.format("if (%s == null || %s.isEmpty()) {", paramName, paramName));
                codeLines.add(String.format("    throw new java.lang.NullPointerException(\"%s\");", paramName));
                codeLines.add("}");
            }
        }
    }

    private List<String> processQueryStatement(String statementValue, List<VariableElement> actualParameters) {
        final var statementArr = statementValue.replace(";", CommonConstant.EMPTY_STRING).split(" ");
        var index = 0;
        final var finalStatementArray = new ArrayList<String>();

        for (int i = 0; i < statementArr.length; i++) {
            final var word = statementArr[i];
            if (word.startsWith("?")) {
                if (i > 0 && statementArr[i - 1].equalsIgnoreCase("IN")) {
                    finalStatementArray.add(word.replace("?", String.format(
                            "(\" + %s.stream().map(it -> placeholder.equals(\"?\") ? \"?\" : placeholder + " +
                                    "count.incrementAndGet()).collect(java.util.stream.Collectors.joining(\", \")) + \")",
                            actualParameters.get(index).getSimpleName()
                    )));
                } else {
                    finalStatementArray.add(word.replace("?", "\" + placeholder + (placeholder.equals(\"?\") ? \"\" : count.incrementAndGet()) + \""));
                }
                index++;
            } else {
                finalStatementArray.add(word);
            }
        }
        return finalStatementArray;
    }

    private void addQueryExecution(List<String> codeLines, String sqlConnVar, String contextVar,
                                   String queryStatement, List<VariableElement> actualParameters) {
        if (lastParameterIsPageable(actualParameters)) {
            String pageableParam = actualParameters.get(actualParameters.size() - 1).getSimpleName().toString();
            codeLines.add(String.format(
                    "return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%s, %s).preparedQuery(\"%s\" + %s)",
                    sqlConnVar, contextVar, queryStatement, pageableParam + ".toSql()"));
        } else {
            codeLines.add(String.format(
                    "return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%s, %s).preparedQuery(\"%s\")",
                    sqlConnVar, contextVar, queryStatement));
        }

        if (actualParameters.isEmpty() || (lastParameterIsPageable(actualParameters) && actualParameters.size() == 1)) {
            codeLines.add("        .execute()");
        } else {
            String paramList = actualParameters.stream()
                    .filter(it -> !isPageable(it))
                    .map(VariableElement::getSimpleName)
                    .collect(Collectors.joining(", "));
            codeLines.add(String.format("        .execute(io.vertx.sqlclient.Tuple.of(%s))", paramList));
        }

        codeLines.add("        .map(rowSet -> {");
    }

    private void addResultMapping(List<String> codeLines, String contextVar, boolean isReturningList,
                                  String futureOutputType, List<String> finalStatementArray,
                                  List<VariableElement> actualParameters, String sqlConnVar) {
        if (isReturningList || lastParameterIsPageable(actualParameters)) {
            addListResultMapping(codeLines, contextVar, isReturningList, futureOutputType,
                    finalStatementArray, actualParameters, sqlConnVar);
        } else {
            addSingleResultMapping(codeLines, contextVar, futureOutputType, finalStatementArray);
        }
    }

    private void addListResultMapping(List<String> codeLines, String contextVar, boolean isReturningList,
                                      String futureOutputType, List<String> finalStatementArray,
                                      List<VariableElement> actualParameters, String sqlConnVar) {
        String genericTypeOfList;
        if (isReturningList) {
            genericTypeOfList = MyStringUtils.removeSuffixOfString(
                    MyStringUtils.removePrefixOfString(futureOutputType, "java.util.List<"), ">");
        } else {
            genericTypeOfList = MyStringUtils.removeSuffixOfString(
                    MyStringUtils.removePrefixOfString(futureOutputType, "vn.com.lcx.common.database.pageable.Page<"), ">");
        }

        codeLines.replaceAll(s -> s.replace("${{class}}", genericTypeOfList));
        codeLines.add(String.format("            final java.util.List<%s> result = new java.util.ArrayList<>();", genericTypeOfList));
        codeLines.add("            for (io.vertx.sqlclient.Row row : rowSet) {");
        codeLines.add(String.format("                result.add(%sUtils.vertxRowMapping(row));", genericTypeOfList));
        codeLines.add("            }");
        ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 12);
        codeLines.add("            return result;");

        if (lastParameterIsPageable(actualParameters) && !isReturningList) {
            addPageableCountQuery(codeLines, sqlConnVar, contextVar, finalStatementArray, actualParameters);
        }
    }

    private void addPageableCountQuery(List<String> codeLines, String sqlConnVar, String contextVar,
                                       List<String> finalStatementArray, List<VariableElement> actualParameters) {
        final var countStatementArray = new ArrayList<String>();
        final var subListFromKeyword = ReactiveCodeGenHelper.subListFromKeyword(finalStatementArray, "from");
        if (!subListFromKeyword.isEmpty()) {
            subListFromKeyword.set(0, "FROM");
        }
        countStatementArray.add("SELECT COUNT(1)");
        countStatementArray.addAll(subListFromKeyword);
        final String countStatement = String.join(" ", countStatementArray).replace("\n", "\\n\" +\n                        \"");

        String pageableParam = actualParameters.get(actualParameters.size() - 1).getSimpleName().toString();

        codeLines.add("        }).compose(rs -> {");
        codeLines.add(String.format(
                "            return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%s, %s).preparedQuery(\"%s\")",
                sqlConnVar, contextVar, countStatement));

        if (actualParameters.isEmpty() || (lastParameterIsPageable(actualParameters) && actualParameters.size() == 1)) {
            codeLines.add("                    .execute()");
        } else {
            String paramList = actualParameters.stream()
                    .filter(it -> !isPageable(it))
                    .map(VariableElement::getSimpleName)
                    .collect(Collectors.joining(", "));
            codeLines.add(String.format("                    .execute(io.vertx.sqlclient.Tuple.of(%s))", paramList));
        }

        codeLines.add("                    .map(rowSet -> {");
        codeLines.add("                        long countRs = 0L;");
        codeLines.add("                        for (io.vertx.sqlclient.Row row : rowSet) {");
        codeLines.add("                            countRs = countRs + row.getLong(0);");
        codeLines.add("                            break;");
        codeLines.add("                        }");
        codeLines.add(String.format(
                "                        return vn.com.lcx.common.database.pageable.Page.create(rs, countRs, %s.getPageNumber(), %s.getPageSize());",
                pageableParam, pageableParam));
        codeLines.add("                    });");
    }

    private void addSingleResultMapping(List<String> codeLines, String contextVar,
                                        String futureOutputType, List<String> finalStatementArray) {
        final boolean isOptional = futureOutputType.startsWith("java.util.Optional");
        final String genericType = isOptional
                ? MyStringUtils.removeSuffixOfString(MyStringUtils.removePrefixOfString(futureOutputType, "java.util.Optional<"), ">")
                : futureOutputType;

        switch (genericType) {
            case "java.lang.Long":
                addNumericResultMapping(codeLines, contextVar, finalStatementArray, "Long", "(long) rowSet.rowCount()");
                break;
            case "java.lang.Integer":
                addNumericResultMapping(codeLines, contextVar, finalStatementArray, "Integer", "rowSet.rowCount()");
                break;
            case "java.lang.Object[]":
                addObjectArrayResultMapping(codeLines, contextVar);
                break;
            default:
                addEntityResultMapping(codeLines, contextVar, genericType, isOptional);
                break;
        }
    }

    private void addNumericResultMapping(List<String> codeLines, String contextVar,
                                         List<String> finalStatementArray, String type, String rowCountExpr) {
        if (isModifyingQuery(finalStatementArray)) {
            ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 12);
            codeLines.add(String.format("            return %s;", rowCountExpr));
        } else {
            String resultType = type.equals("Long") ? "long" : "int";
            String getMethod = type.equals("Long") ? "getLong" : "getInteger";
            codeLines.add(String.format("            %s result = 0;", resultType));
            codeLines.add("            for (io.vertx.sqlclient.Row row : rowSet) {");
            codeLines.add(String.format("                result += row.%s(0);", getMethod));
            codeLines.add("            }");
            ReactiveCodeGenHelper.addDurationLogging(codeLines, contextVar, 12);
            codeLines.add("            return result;");
        }
    }

    private boolean isModifyingQuery(List<String> statementArray) {
        if (statementArray.isEmpty()) return false;
        String firstWord = statementArray.get(0).toLowerCase();
        return firstWord.startsWith("update") || firstWord.startsWith("insert") || firstWord.startsWith("delete");
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
        codeLines.add("                throw new vn.com.lcx.reactive.exception.NonUniqueQueryResult();");
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

    // ========== Utility Methods ==========

    public boolean lastParameterIsPageable(List<VariableElement> actualParameters) {
        return !actualParameters.isEmpty() && isPageable(actualParameters.get(actualParameters.size() - 1));
    }

    private boolean isPageable(VariableElement variableElement) {
        return processingEnv.getTypeUtils().isAssignable(
                variableElement.asType(),
                TypeHierarchyAnalyzer.getTypeElementFromClassName(
                        processingEnv.getElementUtils(),
                        "vn.com.lcx.common.database.pageable.Pageable"
                ).asType()
        );
    }
}
