package vn.com.lcx.processor;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.ExceptionUtils;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.MyStringUtils;
import vn.com.lcx.processor.utility.MethodInfo;
import vn.com.lcx.processor.utility.ProcessorClassInfo;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SupportedAnnotationTypes("vn.com.lcx.reactive.annotation.RRepository")
public class ReactiveRepositoryProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // roundEnv.getRootElements()
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(RRepository.class)) {
            if (annotatedElement instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                try {
                    ProcessorClassInfo processorClassInfo = ProcessorClassInfo.init(
                            typeElement,
                            processingEnv.getTypeUtils(),
                            processingEnv.getElementUtils()
                    );
                    generateCode(processorClassInfo);
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

    public void generateCode(ProcessorClassInfo processorClassInfo) throws IOException {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                vn.com.lcx.common.utils.DateTimeUtils.toUnixMil(vn.com.lcx.common.utils.DateTimeUtils.generateCurrentTimeDefault()) + ": " +
                        String.format(
                                "Generating code for reactive repository : %s",
                                processorClassInfo.getClazz().getQualifiedName()
                        )
        );
        // get generic type of interface
        List<TypeMirror> genericClasses =
                TypeHierarchyAnalyzer.getGenericTypeOfExtendingInterface(
                        processingEnv.getElementUtils(),
                        processingEnv.getTypeUtils(),
                        processorClassInfo.getClazz(),
                        ReactiveRepository.class.getName()
                );
        String repositoryTemplate = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/repository-template.txt"
        );
        String methodTemplate = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/method-template.txt"
        );
        assert StringUtils.isNotBlank(repositoryTemplate);
        assert StringUtils.isNotBlank(methodTemplate);
        final TypeMirror entityTypeMirror = genericClasses.get(0);
        StringBuilder methodCodeBody = new StringBuilder();
        methodCodeBody.append("\n");
        processorClassInfo.getMethods().forEach(
                (methodInfo, executableElement) -> {
                    final String actualReturnType;
                    if (methodInfo.getOutputParameter().toString().equals("T")) {
                        actualReturnType = entityTypeMirror.toString();
                    } else if (methodInfo.getOutputParameter().toString().contains("<T>")) {
                        actualReturnType = methodInfo.getOutputParameter().toString().replace("<T>", "<" + entityTypeMirror.toString() + ">");
                    } else {
                        actualReturnType = methodInfo.getOutputParameter().toString();
                    }
                    final var codeLines = new ArrayList<String>();
                    if (methodInfo.getInputParameters().isEmpty() || methodInfo.getInputParameters().size() < 2) {
                        codeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"First parameter must be a `io.vertx.ext.web.RoutingContext` and the second one must be a `io.vertx.sqlclient.SqlConnection`\");");
                        return;
                    }
                    if (!methodInfo.getInputParameters().get(0).asType().toString().equals("io.vertx.ext.web.RoutingContext") || !methodInfo.getInputParameters().get(1).asType().toString().equals("io.vertx.sqlclient.SqlConnection")) {
                        codeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"First parameter must be a `io.vertx.ext.web.RoutingContext` and the second one must be a `io.vertx.sqlclient.SqlConnection`\");");
                    } else {
                        final String futureOutputType = MyStringUtils
                                .removeSuffixOfString(
                                        MyStringUtils.removePrefixOfString(
                                                actualReturnType,
                                                "io.vertx.core.Future<"
                                        ),
                                        ">"
                                );
                        codeLines.add(
                                String.format(
                                        "io.vertx.core.Future<%s> future;",
                                        futureOutputType
                                )
                        );
                        final boolean isReturningList = futureOutputType.contains("java.util.List");
                        final VariableElement contextVariable = methodInfo.getInputParameters().get(0);
                        final VariableElement sqlConnectionVariable = methodInfo.getInputParameters().get(1);
                        final List<VariableElement> actualParameters = new ArrayList<>();
                        for (int i = 2; i < methodInfo.getInputParameters().size(); i++) {
                            actualParameters.add(methodInfo.getInputParameters().get(i));
                        }
                        codeLines.add(
                                String.format(
                                        "String databaseName = %s.databaseMetadata().productName();",
                                        sqlConnectionVariable.getSimpleName().toString()
                                )
                        );
                        switch (methodInfo.getMethodName()) {
                            case "save":
                                buildSaveModelMethodCodeBody(
                                        codeLines,
                                        contextVariable,
                                        sqlConnectionVariable,
                                        entityTypeMirror
                                );
                                break;
                            case "update":
                                buildUpdateModelMethodCodeBody(
                                        codeLines,
                                        contextVariable,
                                        sqlConnectionVariable,
                                        entityTypeMirror
                                );
                                break;
                            case "delete":
                                buildDeleteModelMethodCodeBody(
                                        codeLines,
                                        contextVariable,
                                        sqlConnectionVariable,
                                        entityTypeMirror);
                                break;
                            default:
                                if (Optional.ofNullable(executableElement.getAnnotation(Query.class)).isPresent()) {
                                    buildQueryMethodCodeBody(
                                            executableElement,
                                            codeLines,
                                            contextVariable,
                                            sqlConnectionVariable,
                                            actualParameters,
                                            isReturningList,
                                            futureOutputType
                                    );
                                } else {
                                    buildCustomQueryMethodCodeBody(
                                            methodInfo,
                                            executableElement,
                                            codeLines,
                                            contextVariable,
                                            sqlConnectionVariable,
                                            entityTypeMirror,
                                            actualParameters,
                                            isReturningList,
                                            futureOutputType
                                    );
                                }
                                break;
                        }
                    }
                    methodCodeBody.append(
                            methodTemplate
                                    .replace(
                                            "${return-type}",
                                            actualReturnType
                                    )
                                    .replace(
                                            "${method-name}",
                                            methodInfo.getMethodName()
                                    )
                                    .replace(
                                            "${list-of-parameters}",
                                            methodInfo.getInputParameters()
                                                    .stream()
                                                    .map(
                                                            variableElement -> {
                                                                if (!variableElement.asType().toString().equals("T")) {
                                                                    return String.format(
                                                                            "%s %s",
                                                                            variableElement.asType(),
                                                                            variableElement.getSimpleName()
                                                                    );
                                                                } else {
                                                                    return entityTypeMirror.toString() + " model";
                                                                }
                                                            }
                                                    )
                                                    .collect(Collectors.joining(", "))
                                    )
                                    .replace(
                                            "${method-body}",
                                            codeLines
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
                }
        );
        final var packageName = processingEnv
                .getElementUtils()
                .getPackageOf(processorClassInfo.getClazz())
                .getQualifiedName()
                .toString();
        final var className = processorClassInfo.getClazz().getSimpleName() + "Impl";
        final var code = repositoryTemplate
                .replace("${package-name}", packageName)
                .replace("${proxy-class-name}", className)
                .replace("${interface-class-name}", processorClassInfo.getClazz().getSimpleName())
                .replace("${methods}", MyStringUtils.removeSuffixOfString(methodCodeBody.toString(), "\n"));
        String fullClassName = packageName + "." + className;
        // processingEnv.getMessager().printMessage(
        //         Diagnostic.Kind.NOTE,
        //         vn.com.lcx.common.utils.DateTimeUtils.toUnixMil(vn.com.lcx.common.utils.DateTimeUtils.generateCurrentTimeDefault()) + ": \n" +
        //                 code
        // );
        JavaFileObject builderFile = this.processingEnv.getFiler().createSourceFile(fullClassName);
        try (Writer writer = builderFile.openWriter()) {
            writer.write(code);
        }
    }

    private void buildSaveModelMethodCodeBody(ArrayList<String> codeLines,
                                              VariableElement contextVariable,
                                              VariableElement sqlConnectionVariable,
                                              TypeMirror entityTypeMirror) {
        codeLines.add(
                "final double startingTime = (double) java.lang.System.currentTimeMillis();"
        );
        codeLines.add(
                "if (databaseName.equals(\"PostgreSQL\")) {"
        );
        codeLines.add(
                String.format("    return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveInsertStatement(model, \"$\") + \" returning \" + %3$sUtils.idColumnName())",
                        sqlConnectionVariable.getSimpleName().toString(),
                        contextVariable.getSimpleName().toString(),
                        entityTypeMirror.toString())
        );
        codeLines.add(
                String.format("            .execute(%sUtils.insertTupleParam(model))", entityTypeMirror)
        );
        codeLines.add("            .map(rowSet -> {");
        codeLines.add("                for (io.vertx.sqlclient.Row row : rowSet) {");
        codeLines.add("                    " + entityTypeMirror + "Utils.idRowExtract(row, model);");
        codeLines.add("                }");
        codeLines.add("                final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;");
        codeLines.add("                vn.com.lcx.common.utils.LogUtils.writeLog(" + contextVariable.getSimpleName() + ", vn.com.lcx.common.utils.LogUtils.Level.INFO, \"Executed SQL in {} ms\", duration);");
        codeLines.add("                return model;");
        codeLines.add("            });");
        codeLines.add(
                "} else if (databaseName.equals(\"MySQL\") || databaseName.equals(\"MariaDB\")) {"
        );
        codeLines.add(
                String.format("    return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveInsertStatement(model, \"?\"))",
                        sqlConnectionVariable.getSimpleName().toString(),
                        contextVariable.getSimpleName().toString(),
                        entityTypeMirror)
        );
        codeLines.add(
                String.format("            .execute(%sUtils.insertTupleParam(model))", entityTypeMirror)
        );
        codeLines.add("            .map(rowSet -> {");
        codeLines.add("                " + entityTypeMirror + "Utils.mySqlIdRowExtract(rowSet, model);");
        codeLines.add("                final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;");
        codeLines.add("                vn.com.lcx.common.utils.LogUtils.writeLog(" + contextVariable.getSimpleName() + ", vn.com.lcx.common.utils.LogUtils.Level.INFO, \"Executed SQL in {} ms\", duration);");
        codeLines.add("                return model;");
        codeLines.add("            });");
        codeLines.add(
                "} else if (databaseName.equals(\"Microsoft SQL Server\")) {"
        );
        codeLines.add(
                String.format("    return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveInsertStatement(model, \"@p\"))",
                        sqlConnectionVariable.getSimpleName().toString(),
                        contextVariable.getSimpleName().toString(),
                        entityTypeMirror)
        );
        codeLines.add(
                String.format("            .execute(%sUtils.insertTupleParam(model))", entityTypeMirror)
        );
        codeLines.add("            .map(rowSet -> {");
        codeLines.add("                " + entityTypeMirror + "Utils.idRowExtract(rowSet.iterator().next(), model);");
        codeLines.add("                final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;");
        codeLines.add("                vn.com.lcx.common.utils.LogUtils.writeLog(" + contextVariable.getSimpleName() + ", vn.com.lcx.common.utils.LogUtils.Level.INFO, \"Executed SQL in {} ms\", duration);");
        codeLines.add("                return model;");
        codeLines.add("            });");
        codeLines.add(
                "} else if (databaseName.equals(\"Oracle\")) {"
        );
        codeLines.add(
                String.format("    return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveInsertStatement(model, \"?\"), new io.vertx.oracleclient.OraclePrepareOptions().setAutoGeneratedKeysIndexes(new io.vertx.core.json.JsonArray().add(%3$sUtils.idColumnName())))",
                        sqlConnectionVariable.getSimpleName().toString(),
                        contextVariable.getSimpleName().toString(),
                        entityTypeMirror)
        );
        codeLines.add(
                String.format("            .execute(%sUtils.insertTupleParam(model))", entityTypeMirror)
        );
        codeLines.add("            .map(rowSet -> {");
        codeLines.add("                io.vertx.sqlclient.Row row = rowSet.property(io.vertx.oracleclient.OracleClient.GENERATED_KEYS);");
        codeLines.add("                " + entityTypeMirror + "Utils.idRowExtract(row, model);");
        codeLines.add("                final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;");
        codeLines.add("                vn.com.lcx.common.utils.LogUtils.writeLog(" + contextVariable.getSimpleName() + ", vn.com.lcx.common.utils.LogUtils.Level.INFO, \"Executed SQL in {} ms\", duration);");
        codeLines.add("                return model;");
        codeLines.add("            });");
        codeLines.add(
                "} else {"
        );
        codeLines.add(
                "    throw new vn.com.lcx.jpa.exception.CodeGenError(\"Unsupported database type\");"
        );
        codeLines.add(
                "}"
        );
    }

    private void buildUpdateModelMethodCodeBody(ArrayList<String> codeLines,
                                                VariableElement contextVariable,
                                                VariableElement sqlConnectionVariable,
                                                TypeMirror entityTypeMirror) {
        codeLines.add(
                "final double startingTime = (double) java.lang.System.currentTimeMillis();"
        );
        codeLines.add(
                "if (databaseName.equals(\"PostgreSQL\")) {"
        );
        codeLines.add(
                String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveUpdateStatement(model, \"$\"))",
                        sqlConnectionVariable.getSimpleName().toString(),
                        contextVariable.getSimpleName().toString(),
                        entityTypeMirror)
        );
        codeLines.add(
                String.format("            .execute(%sUtils.updateTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityTypeMirror)
        );
        codeLines.add(
                "} else if (databaseName.equals(\"MySQL\") || databaseName.equals(\"MariaDB\")) {"
        );
        codeLines.add(
                String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveUpdateStatement(model, \"?\"))",
                        sqlConnectionVariable.getSimpleName().toString(),
                        contextVariable.getSimpleName().toString(),
                        entityTypeMirror)
        );
        codeLines.add(
                String.format("            .execute(%sUtils.updateTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityTypeMirror)
        );
        codeLines.add(
                "} else if (databaseName.equals(\"Microsoft SQL Server\")) {"
        );
        codeLines.add(
                String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveUpdateStatement(model, \"@p\"))",
                        sqlConnectionVariable.getSimpleName().toString(),
                        contextVariable.getSimpleName().toString(),
                        entityTypeMirror)
        );
        codeLines.add(
                String.format("            .execute(%sUtils.updateTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityTypeMirror)
        );
        codeLines.add(
                "} else if (databaseName.equals(\"Oracle\")) {"
        );
        codeLines.add(
                String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveUpdateStatement(model, \"?\"))",
                        sqlConnectionVariable.getSimpleName().toString(),
                        contextVariable.getSimpleName().toString(),
                        entityTypeMirror)
        );
        codeLines.add(
                String.format("            .execute(%sUtils.updateTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityTypeMirror)
        );
        codeLines.add(
                "} else {"
        );
        codeLines.add(
                "    throw new vn.com.lcx.jpa.exception.CodeGenError(\"Unsupported database type\");"
        );
        codeLines.add(
                "}"
        );
        codeLines.add(
                "return future.map(it -> {"
        );
        codeLines.add(
                "    final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;"
        );
        codeLines.add(
                "    vn.com.lcx.common.utils.LogUtils.writeLog(" + contextVariable.getSimpleName() + ", vn.com.lcx.common.utils.LogUtils.Level.INFO, \"Executed SQL in {} ms\", duration);"
        );
        codeLines.add(
                "    return it;"
        );
        codeLines.add(
                "});"
        );
    }

    private void buildDeleteModelMethodCodeBody(ArrayList<String> codeLines,
                                                VariableElement contextVariable,
                                                VariableElement sqlConnectionVariable,
                                                TypeMirror entityTypeMirror) {
        codeLines.add(
                "final double startingTime = (double) java.lang.System.currentTimeMillis();"
        );
        codeLines.add(
                "if (databaseName.equals(\"PostgreSQL\")) {"
        );
        codeLines.add(
                String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveDeleteStatement(model, \"$\") + \" returning \" + %3$sUtils.idColumnName())",
                        sqlConnectionVariable.getSimpleName().toString(),
                        contextVariable.getSimpleName().toString(),
                        entityTypeMirror)
        );
        codeLines.add(
                String.format("            .execute(%sUtils.deleteTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityTypeMirror)
        );
        codeLines.add(
                "} else if (databaseName.equals(\"MySQL\") || databaseName.equals(\"MariaDB\")) {"
        );
        codeLines.add(
                String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveDeleteStatement(model, \"?\"))",
                        sqlConnectionVariable.getSimpleName().toString(),
                        contextVariable.getSimpleName().toString(),
                        entityTypeMirror)
        );
        codeLines.add(
                String.format("            .execute(%sUtils.deleteTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityTypeMirror)
        );
        codeLines.add(
                "} else if (databaseName.equals(\"Microsoft SQL Server\")) {"
        );
        codeLines.add(
                String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveDeleteStatement(model, \"@p\"))",
                        sqlConnectionVariable.getSimpleName().toString(),
                        contextVariable.getSimpleName().toString(),
                        entityTypeMirror)
        );
        codeLines.add(
                String.format("            .execute(%sUtils.deleteTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityTypeMirror)
        );
        codeLines.add(
                "} else if (databaseName.equals(\"Oracle\")) {"
        );
        codeLines.add(
                String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveDeleteStatement(model, \"?\"))",
                        sqlConnectionVariable.getSimpleName().toString(),
                        contextVariable.getSimpleName().toString(),
                        entityTypeMirror)
        );
        codeLines.add(
                String.format("            .execute(%sUtils.deleteTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityTypeMirror)
        );
        codeLines.add(
                "} else {"
        );
        codeLines.add(
                "    throw new vn.com.lcx.jpa.exception.CodeGenError(\"Unsupported database type\");"
        );
        codeLines.add(
                "}"
        );
        codeLines.add(
                "return future.map(it -> {"
        );
        codeLines.add(
                "    final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;"
        );
        codeLines.add(
                "    vn.com.lcx.common.utils.LogUtils.writeLog(" + contextVariable.getSimpleName() + ", vn.com.lcx.common.utils.LogUtils.Level.INFO, \"Executed SQL in {} ms\", duration);"
        );
        codeLines.add(
                "    return it;"
        );
        codeLines.add(
                "});"
        );
    }

    private void buildQueryMethodCodeBody(ExecutableElement executableElement,
                                          ArrayList<String> codeLines,
                                          VariableElement contextVariable,
                                          VariableElement sqlConnectionVariable,
                                          List<VariableElement> actualParameters,
                                          boolean isReturningList,
                                          String futureOutputType) {
        // final String queryStatement = executableElement.getAnnotation(Query.class).value().replace("\n", "\\n");
        codeLines.add("final double startingTime = (double) java.lang.System.currentTimeMillis();");
        codeLines.add("java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);");
        codeLines.add("String placeholder;");
        codeLines.add("if (databaseName.equals(\"PostgreSQL\")) {");
        codeLines.add("    placeholder = \"$\";");
        codeLines.add("} else if (databaseName.equals(\"MySQL\") || databaseName.equals(\"MariaDB\")) {");
        codeLines.add("    placeholder = \"?\";");
        codeLines.add("} else if (databaseName.equals(\"Microsoft SQL Server\")) {");
        codeLines.add("    placeholder = \"@p\";");
        codeLines.add("} else if (databaseName.equals(\"Oracle\")) {");
        codeLines.add("    placeholder = \"?\";");
        codeLines.add("} else {");
        codeLines.add("    throw new vn.com.lcx.jpa.exception.CodeGenError(\"Unsupported database type\");");
        codeLines.add("}");
        for (VariableElement actualParameter : actualParameters) {
            if (actualParameter.asType().toString().contains("java.util.List")) {
                codeLines.add("if (" + actualParameter.getSimpleName() + " == null || " + actualParameter.getSimpleName() + ".isEmpty()) {");
                codeLines.add("    throw new java.lang.NullPointerException(\"" + actualParameter.getSimpleName() + "\");");
                codeLines.add("}");
            }
        }
        final var statement = executableElement.getAnnotation(Query.class);
        final var statementArr = statement.value().split(" ");
        var index = 0;
        final var finalStatementArray = new ArrayList<String>();
        for (int i = 0; i < statementArr.length; i++) {
            final var word = statementArr[i];
            if (word.startsWith("?")) {
                if (statementArr[i - 1].equalsIgnoreCase("IN")) {
                    finalStatementArray.add(
                            word.replace(
                                    "?",
                                    String.format(
                                            "(\" + %s.stream().map(it -> placeholder.equals(\"?\") ? \"?\" : placeholder + " +
                                                    "count.incrementAndGet()).collect(java.util.stream.Collectors.joining(\", \")) + \")",
                                            actualParameters.get(index).getSimpleName()
                                    )
                            )
                    );
                } else {
                    finalStatementArray.add(
                            word.replace("?", "\" + placeholder + (placeholder.equals(\"?\") ? \"\" : count.incrementAndGet()) + \"")
                    );
                }
                index = index + 1;
            } else {
                finalStatementArray.add(word);
            }
        }
        final String queryStatement = String.join(" ", finalStatementArray).replace("\n", "\\n\" +\n                        \"");
        if (lastParameterIsPageable(actualParameters)) {
            codeLines.add(
                    String.format("return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(\"%3$s\" + %4$s)",
                            sqlConnectionVariable.getSimpleName().toString(),
                            contextVariable.getSimpleName().toString(),
                            queryStatement,
                            actualParameters.get(actualParameters.size() - 1).getSimpleName() + ".toSql()")
            );
        } else {
            codeLines.add(
                    String.format("return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(\"%3$s\")",
                            sqlConnectionVariable.getSimpleName().toString(),
                            contextVariable.getSimpleName().toString(),
                            queryStatement)
            );
        }
        if (actualParameters.isEmpty()) {
            codeLines.add(
                    "        .execute()"
            );
        } else {
            if (lastParameterIsPageable(actualParameters) && actualParameters.size() == 1) {
                codeLines.add(
                        "        .execute()"
                );
            } else {
                codeLines.add(
                        String.format(
                                "        .execute(io.vertx.sqlclient.Tuple.of(%s))",
                                actualParameters.stream().filter(it -> !isPageable(it)).map(
                                        VariableElement::getSimpleName
                                ).collect(Collectors.joining(", "))
                        )
                );
            }
        }
        codeLines.add(
                "        .map(rowSet -> {"
        );
        if (isReturningList || lastParameterIsPageable(actualParameters)) {
            String genericTypeOfList;
            if (isReturningList) {
                genericTypeOfList = MyStringUtils
                        .removeSuffixOfString(
                                MyStringUtils.removePrefixOfString(futureOutputType, "java.util.List<"),
                                ">"
                        );
            } else {
                genericTypeOfList = MyStringUtils
                        .removeSuffixOfString(
                                MyStringUtils.removePrefixOfString(futureOutputType, "vn.com.lcx.common.database.pageable.Page<"),
                                ">"
                        );
            }
            codeLines.add(
                    "            final java.util.List<" + genericTypeOfList + "> result = new java.util.ArrayList<>();"
            );
            codeLines.add(
                    "            for (io.vertx.sqlclient.Row row : rowSet) {"
            );
            codeLines.add(
                    "                result.add(" + genericTypeOfList + "Utils.vertxRowMapping(row));"
            );
            codeLines.add(
                    "            }"
            );
            codeLines.add(
                    "            final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;"
            );
            codeLines.add(
                    "            vn.com.lcx.common.utils.LogUtils.writeLog(" + contextVariable.getSimpleName() + ", vn.com.lcx.common.utils.LogUtils.Level.INFO, \"Executed SQL in {} ms\", duration);"
            );
            codeLines.add(
                    "            return result;"
            );
            if (lastParameterIsPageable(actualParameters) && !isReturningList) {
                final var countStatementArray = new ArrayList<String>();
                countStatementArray.add("SELECT COUNT(1)");
                countStatementArray.addAll(subListFromKeyword(
                        finalStatementArray,
                        "from"
                ));
                final String countStatement = String.join(" ", countStatementArray).replace("\n", "\\n\" +\n                        \"");
                codeLines.add(
                        "        }).compose(rs -> {"
                );
                codeLines.add(
                        String.format("            return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(\"%3$s\")",
                                sqlConnectionVariable.getSimpleName().toString(),
                                contextVariable.getSimpleName().toString(),
                                countStatement)
                );
                if (actualParameters.isEmpty() || (lastParameterIsPageable(actualParameters) && actualParameters.size() == 1)) {
                    codeLines.add(
                            "                    .execute()"
                    );
                } else {
                    codeLines.add(
                            String.format(
                                    "                    .execute(io.vertx.sqlclient.Tuple.of(%s))",
                                    actualParameters.stream().filter(it -> !isPageable(it)).map(
                                            VariableElement::getSimpleName
                                    ).collect(Collectors.joining(", "))
                            )
                    );
                }
                codeLines.add(
                        "                    .map(rowSet -> {"
                );
                codeLines.add(
                        "                        long countRs = 0L;"
                );
                codeLines.add(
                        "                        for (io.vertx.sqlclient.Row row : rowSet) {"
                );
                codeLines.add(
                        "                            countRs = countRs + row.getLong(0);"
                );
                codeLines.add(
                        "                            break;"
                );
                codeLines.add(
                        "                        }"
                );
                codeLines.add(
                        "                        return vn.com.lcx.common.database.pageable.Page.create(rs, countRs, " +
                                actualParameters.get(actualParameters.size() - 1).getSimpleName() + ".getPageNumber(), " +
                                actualParameters.get(actualParameters.size() - 1).getSimpleName() + ".getPageSize());"
                );
                codeLines.add(
                        "                    });"
                );
            }
        } else {
            final boolean isOptional = futureOutputType.startsWith("java.util.Optional");
            final String genericType;
            if (isOptional) {
                genericType = MyStringUtils
                        .removeSuffixOfString(
                                MyStringUtils.removePrefixOfString(futureOutputType, "java.util.Optional<"),
                                ">"
                        );
            } else {
                genericType = futureOutputType;
            }
            switch (genericType) {
                case "java.lang.Long":
                    if (finalStatementArray.get(0).toLowerCase().startsWith("update") ||
                            finalStatementArray.get(0).toLowerCase().startsWith("delete")) {
                        codeLines.add(
                                "            final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;"
                        );
                        codeLines.add(
                                "            vn.com.lcx.common.utils.LogUtils.writeLog(" + contextVariable.getSimpleName() + ", vn.com.lcx.common.utils.LogUtils.Level.INFO, \"Executed SQL in {} ms\", duration);"
                        );
                        codeLines.add(
                                "            return (long) rowSet.rowCount();"
                        );
                    } else {
                        codeLines.add(
                                "            long result = 0;"
                        );
                        codeLines.add(
                                "            for (io.vertx.sqlclient.Row row : rowSet) {"
                        );
                        codeLines.add(
                                "                result += row.getLong(0);"
                        );
                        codeLines.add(
                                "            }"
                        );
                        codeLines.add(
                                "            final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;"
                        );
                        codeLines.add(
                                "            vn.com.lcx.common.utils.LogUtils.writeLog(" + contextVariable.getSimpleName() + ", vn.com.lcx.common.utils.LogUtils.Level.INFO, \"Executed SQL in {} ms\", duration);"
                        );
                        codeLines.add(
                                "            return result;"
                        );
                    }
                    break;
                case "java.lang.Integer":
                    if (finalStatementArray.get(0).toLowerCase().startsWith("update") ||
                            finalStatementArray.get(0).toLowerCase().startsWith("delete")) {
                        codeLines.add(
                                "            return rowSet.rowCount();"
                        );
                    } else {
                        codeLines.add(
                                "            int result = 0;"
                        );
                        codeLines.add(
                                "            for (io.vertx.sqlclient.Row row : rowSet) {"
                        );
                        codeLines.add(
                                "                result += row.getInteger(0);"
                        );
                        codeLines.add(
                                "            }"
                        );
                        codeLines.add(
                                "            final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;"
                        );
                        codeLines.add(
                                "            vn.com.lcx.common.utils.LogUtils.writeLog(" + contextVariable.getSimpleName() + ", vn.com.lcx.common.utils.LogUtils.Level.INFO, \"Executed SQL in {} ms\", duration);"
                        );
                        codeLines.add(
                                "            return result;"
                        );
                    }
                    break;
                case "java.lang.Object[]":
                    codeLines.add(
                            "            java.util.List<java.lang.Object> objects = new java.util.ArrayList<>();"
                    );
                    codeLines.add(
                            "            for (io.vertx.sqlclient.Row row : rowSet) {"
                    );
                    codeLines.add(
                            "                for (int i = 0; i < row.size(); i++) {"
                    );
                    codeLines.add(
                            "                    objects.add(row.getValue(i));"
                    );
                    codeLines.add(
                            "                }"
                    );
                    codeLines.add(
                            "            }"
                    );
                    codeLines.add(
                            "            final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;"
                    );
                    codeLines.add(
                            "            vn.com.lcx.common.utils.LogUtils.writeLog(" + contextVariable.getSimpleName() + ", vn.com.lcx.common.utils.LogUtils.Level.INFO, \"Executed SQL in {} ms\", duration);"
                    );
                    codeLines.add(
                            "            return objects.toArray(java.lang.Object[]::new);"
                    );
                    break;
                default:
                    codeLines.add(
                            "            if (rowSet.size() == 0) {"
                    );
                    if (isOptional) {
                        codeLines.add(
                                "                return java.util.Optional.empty();"
                        );
                    } else {
                        codeLines.add(
                                "                return null;"
                        );
                    }
                    codeLines.add(
                            "            }"
                    );
                    codeLines.add(
                            "            if (rowSet.size() > 1) {"
                    );
                    codeLines.add(
                            "                throw new vn.com.lcx.reactive.exception.NonUniqueQueryResult();"
                    );
                    codeLines.add(
                            "            }"
                    );
                    codeLines.add(
                            "            final java.util.List<" + genericType + "> result = new java.util.ArrayList<>();"
                    );
                    codeLines.add(
                            "            for (io.vertx.sqlclient.Row row : rowSet) {"
                    );
                    codeLines.add(
                            "                result.add(" + genericType + "Utils.vertxRowMapping(row));"
                    );
                    codeLines.add(
                            "            }"
                    );
                    codeLines.add(
                            "            final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;"
                    );
                    codeLines.add(
                            "            vn.com.lcx.common.utils.LogUtils.writeLog(" + contextVariable.getSimpleName() + ", vn.com.lcx.common.utils.LogUtils.Level.INFO, \"Executed SQL in {} ms\", duration);"
                    );
                    if (isOptional) {
                        codeLines.add(
                                "            return java.util.Optional.of(result.get(0));"
                        );
                    } else {
                        codeLines.add(
                                "            return result.isEmpty() ? null : result.get(0);"
                        );
                    }
                    break;
            }
        }
        codeLines.add(
                "        });"
        );
    }

    @SuppressWarnings("ALL")
    private void buildCustomQueryMethodCodeBody(MethodInfo methodInfo,
                                                ExecutableElement executableElement,
                                                ArrayList<String> codeLines,
                                                VariableElement contextVariable,
                                                VariableElement sqlConnectionVariable,
                                                TypeMirror entityTypeMirror,
                                                List<VariableElement> actualParameters,
                                                boolean isReturningList,
                                                String futureOutputType) {
        codeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"Unsupported method\");");
    }

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

    /**
     * Returns a sublist starting from the first element
     * that contains the keyword (case-insensitive) until the end of the list.
     *
     * @param keywords the list of keywords to search in
     * @param keyword  the keyword to search for (case-insensitive)
     * @return a sublist of keywords from the found index to the end,
     * or an empty list if no match is found
     */
    public static List<String> subListFromKeyword(List<String> keywords, String keyword) {
        if (keywords == null || keyword == null) {
            return Collections.emptyList();
        }

        int index = IntStream.range(0, keywords.size())
                .filter(i -> keywords.get(i).toLowerCase().contains(keyword.toLowerCase()))
                .findFirst()
                .orElse(-1);

        return index != -1
                ? keywords.subList(index, keywords.size())
                : Collections.emptyList();
    }

}
