package vn.io.lcx.processor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import vn.io.lcx.common.utils.ExceptionUtils;
import vn.io.lcx.common.utils.FileUtils;
import vn.io.lcx.common.utils.MyStringUtils;
import vn.io.lcx.processor.utility.MethodInfo;
import vn.io.lcx.processor.utility.ProcessorClassInfo;
import vn.io.lcx.processor.utility.TypeHierarchyAnalyzer;
import vn.io.lcx.reactive.annotation.HRModifying;
import vn.io.lcx.reactive.annotation.HRParam;
import vn.io.lcx.reactive.annotation.HRQuery;
import vn.io.lcx.reactive.annotation.HRRepository;
import vn.io.lcx.reactive.annotation.HRResultSetMapping;
import vn.io.lcx.reactive.repository.HReactiveRepository;

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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("vn.io.lcx.reactive.annotation.HRRepository")
public class HRRepositoryProcessor extends AbstractProcessor {

    private static final String STAGE_SESSION_TYPE = "org.hibernate.reactive.stage.Stage.Session";
    private static final String FUTURE_TYPE = "io.vertx.core.Future";
    private static final String PAGE_TYPE = "vn.io.lcx.common.database.pageable.Page";
    private static final String PAGEABLE_TYPE = "vn.io.lcx.common.database.pageable.Pageable";
    private static final String CRITERIA_HANDLER_TYPE = "vn.io.lcx.jpa.respository.CriteriaHandler";

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(HRRepository.class)) {
            if (annotatedElement.getKind() != ElementKind.INTERFACE) {
                error(annotatedElement, "@HRRepository can only be used on interfaces");
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
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            ExceptionUtils.getStackTrace(e),
                            annotatedElement
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
                HReactiveRepository.class.getName()
        );
        if (genericClasses.isEmpty()) {
            error(processorClassInfo.getClazz(),
                    "@HRRepository interface must extend vn.io.lcx.reactive.repository.HReactiveRepository<T>");
            return;
        }

        TypeMirror entityTypeMirror = genericClasses.get(0);
        if (TypeHierarchyAnalyzer.getTypeElementFromClassName(
                processingEnv.getElementUtils(),
                entityTypeMirror.toString()
        ) == null) {
            error(processorClassInfo.getClazz(), "Cannot resolve HRRepository entity type: " + entityTypeMirror);
            return;
        }

        String repositoryTemplate = loadTemplate("template/repository-template.txt");
        String methodTemplate = loadTemplate("template/method-template.txt");

        if (!validateRepository(processorClassInfo, entityTypeMirror)) {
            return;
        }

        StringBuilder methodCodeBody = new StringBuilder("\n");
        processorClassInfo.getMethods().forEach((methodInfo, executableElement) -> {
            String methodCode = generateMethodCode(methodInfo, executableElement, entityTypeMirror, methodTemplate);
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
                        "Generating code for HRRepository : %s",
                        processorClassInfo.getClazz().getQualifiedName()
                )
        );
    }

    private String loadTemplate(String templatePath) {
        String template = FileUtils.readResourceFileAsText(this.getClass().getClassLoader(), templatePath);
        if (StringUtils.isBlank(template)) {
            throw new IllegalStateException("Failed to load template file: " + templatePath);
        }
        return template;
    }

    private String generateMethodCode(MethodInfo methodInfo, ExecutableElement executableElement,
                                      TypeMirror entityTypeMirror, String methodTemplate) {
        if (shouldSkipMethod(executableElement)) {
            return null;
        }

        String actualReturnType = resolveTypeName(methodInfo.getOutputParameter(), entityTypeMirror);
        List<String> codeLines = new ArrayList<>();
        List<? extends VariableElement> parameters = methodInfo.getInputParameters();

        if (executableElement.getAnnotation(HRQuery.class) != null) {
            HRReturnTypeModel returnTypeModel = buildReturnTypeModel(executableElement, entityTypeMirror);
            buildQueryMethodCodeBody(executableElement, codeLines, parameters, returnTypeModel);
        } else {
            buildBaseMethodCodeBody(methodInfo, codeLines, parameters, entityTypeMirror);
        }

        if (codeLines.isEmpty()) {
            return null;
        }
        return methodTemplate
                .replace("${return-type}", actualReturnType)
                .replace("${method-name}", methodInfo.getMethodName())
                .replace("${list-of-parameters}", formatParameterList(methodInfo, entityTypeMirror))
                .replace("${method-body}", formatCodeBody(codeLines));
    }

    private boolean shouldSkipMethod(ExecutableElement method) {
        Set<Modifier> modifiers = method.getModifiers();
        return modifiers.contains(Modifier.DEFAULT) || modifiers.contains(Modifier.STATIC);
    }

    private String formatParameterList(MethodInfo methodInfo, TypeMirror entityTypeMirror) {
        return methodInfo.getInputParameters().stream()
                .map(variableElement -> resolveTypeName(variableElement.asType(), entityTypeMirror)
                        + " " + variableElement.getSimpleName())
                .collect(Collectors.joining(", "));
    }

    private String formatCodeBody(List<String> codeLines) {
        return codeLines.stream().collect(Collectors.joining("\n        "));
    }

    private void writeGeneratedClass(ProcessorClassInfo processorClassInfo,
                                     String repositoryTemplate,
                                     String methodsCode) throws IOException {
        String packageName = processingEnv.getElementUtils()
                .getPackageOf(processorClassInfo.getClazz())
                .getQualifiedName()
                .toString();
        String className = processorClassInfo.getClazz().getSimpleName() + "Impl";
        String code = repositoryTemplate
                .replace("${package-name}", packageName)
                .replace("${proxy-class-name}", className)
                .replace("${interface-class-name}", processorClassInfo.getClazz().getSimpleName())
                .replace("${methods}", MyStringUtils.removeSuffixOfString(methodsCode, "\n"));

        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(packageName + "." + className);
        try (Writer writer = builderFile.openWriter()) {
            writer.write(code);
        }
    }

    private void buildBaseMethodCodeBody(MethodInfo methodInfo, List<String> codeLines,
                                         List<? extends VariableElement> parameters,
                                         TypeMirror entityTypeMirror) {
        switch (methodInfo.getMethodName()) {
            case "save":
                buildSaveMethod(codeLines, parameters, entityTypeMirror);
                break;
            case "delete":
                buildDeleteMethod(codeLines, parameters, entityTypeMirror);
                break;
            case "find":
                if (parameters.size() == 3 && isPageable(parameters.get(2))) {
                    buildFindPageMethod(codeLines, parameters, entityTypeMirror);
                } else {
                    buildFindListMethod(codeLines, parameters, entityTypeMirror);
                }
                break;
            case "findOne":
                buildFindOneMethod(codeLines, parameters, entityTypeMirror);
                break;
            default:
                codeLines.add("throw new vn.io.lcx.jpa.exception.CodeGenError(\"Custom HR repository methods must use @vn.io.lcx.reactive.annotation.HRQuery\");");
                break;
        }
    }

    private void buildSaveMethod(List<String> codeLines,
                                 List<? extends VariableElement> parameters,
                                 TypeMirror entityType) {
        String session = parameters.get(0).getSimpleName().toString();
        String entity = parameters.get(1).getSimpleName().toString();
        if (resolveTypeName(parameters.get(1).asType(), entityType).equals("java.util.List<" + entityType + ">")) {
            codeLines.add("io.vertx.core.Future<java.util.List<" + entityType
                    + ">> chain = io.vertx.core.Future.succeededFuture(new java.util.ArrayList<>());");
            codeLines.add("java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);");
            codeLines.add("for (" + entityType + " item : " + entity + ") {");
            codeLines.add("    chain = chain.compose(list ->");
            codeLines.add("            io.vertx.core.Future.fromCompletionStage(" + session + ".merge(item))");
            codeLines.add("                    .compose(rs ->");
            codeLines.add("                            count.incrementAndGet() % 50 == 0 ?");
            codeLines.add("                                    io.vertx.core.Future.fromCompletionStage(");
            codeLines.add("                                            " + session + ".flush()");
            codeLines.add("                                                    .thenAccept(v -> " + session + ".clear())");
            codeLines.add("                                    ).map(rs) : io.vertx.core.Future.succeededFuture(rs)");
            codeLines.add("                    ).map(rs ->");
            codeLines.add("                            {");
            codeLines.add("                                list.add(rs);");
            codeLines.add("                                return list;");
            codeLines.add("                            }");
            codeLines.add("                    )");
            codeLines.add("    );");
            codeLines.add("}");
            codeLines.add("return chain;");
        } else {
            codeLines.add("return io.vertx.core.Future.fromCompletionStage(");
            codeLines.add("        (java.util.concurrent.CompletionStage<" + entityType + ">) " + session + ".merge("
                    + entity + ")");
            codeLines.add(");");
        }
    }

    private void buildDeleteMethod(List<String> codeLines,
                                   List<? extends VariableElement> parameters,
                                   TypeMirror entityType) {
        String session = parameters.get(0).getSimpleName().toString();
        String entity = parameters.get(1).getSimpleName().toString();
        if (resolveTypeName(parameters.get(1).asType(), entityType).equals("java.util.List<" + entityType + ">")) {
            codeLines.add("io.vertx.core.Future<Void> chain = io.vertx.core.Future.succeededFuture();");
            codeLines.add("java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);");
            codeLines.add("for (" + entityType + " item : " + entity + ") {");
            codeLines.add("    chain = chain.compose(v ->");
            codeLines.add("            io.vertx.core.Future.fromCompletionStage(" + session + ".remove(item))");
            codeLines.add("                    .compose(rs ->");
            codeLines.add("                            count.incrementAndGet() % 50 == 0 ?");
            codeLines.add("                                    io.vertx.core.Future.fromCompletionStage(");
            codeLines.add("                                            " + session + ".flush()");
            codeLines.add("                                                    .thenAccept(ignored -> " + session + ".clear())");
            codeLines.add("                                    ).map(ignored -> null) : io.vertx.core.Future.succeededFuture()");
            codeLines.add("                    )");
            codeLines.add("    );");
            codeLines.add("}");
            codeLines.add("return chain;");
        } else {
            codeLines.add("return io.vertx.core.Future.fromCompletionStage(");
            codeLines.add("        (java.util.concurrent.CompletionStage<Void>) " + session + ".remove(" + entity + ")");
            codeLines.add(");");
        }
    }

    private void buildFindListMethod(List<String> codeLines,
                                     List<? extends VariableElement> parameters,
                                     TypeMirror entityType) {
        String session = parameters.get(0).getSimpleName().toString();
        String handler = parameters.get(1).getSimpleName().toString();

        codeLines.add("final var criteriaBuilder = " + session + ".getCriteriaBuilder();");
        codeLines.add("final var criteriaQuery = criteriaBuilder.createQuery(" + entityType + ".class);");
        codeLines.add("final var root = criteriaQuery.from(" + entityType + ".class);");
        codeLines.add("criteriaQuery.select(root);");
        codeLines.add("if (" + handler + " != null) {");
        codeLines.add("    final var predicate = " + handler + ".toPredicate(criteriaBuilder, criteriaQuery, root);");
        codeLines.add("    criteriaQuery.where(predicate);");
        codeLines.add("}");
        codeLines.add("return io.vertx.core.Future.fromCompletionStage(");
        codeLines.add("        (java.util.concurrent.CompletionStage<java.util.List<" + entityType + ">>) " + session
                + ".createQuery(criteriaQuery).getResultList()");
        codeLines.add(");");
    }

    private void buildFindPageMethod(List<String> codeLines,
                                     List<? extends VariableElement> parameters,
                                     TypeMirror entityType) {
        String session = parameters.get(0).getSimpleName().toString();
        String handler = parameters.get(1).getSimpleName().toString();
        String pageable = parameters.get(2).getSimpleName().toString();

        codeLines.add("final var criteriaBuilder = " + session + ".getCriteriaBuilder();");
        codeLines.add("final var criteriaQuery = criteriaBuilder.createQuery(" + entityType + ".class);");
        codeLines.add("final var root = criteriaQuery.from(" + entityType + ".class);");
        codeLines.add("java.util.List<jakarta.persistence.criteria.Order> orders = new java.util.ArrayList<>();");
        codeLines.add("if (" + pageable + ".getFieldNameAndDirectionMap() != null) {");
        codeLines.add("    " + pageable + ".getFieldNameAndDirectionMap().forEach((field, direction) -> {");
        codeLines.add("        if (vn.io.lcx.common.database.pageable.Direction.DESC == direction) {");
        codeLines.add("            orders.add(criteriaBuilder.desc(root.get(field)));");
        codeLines.add("        } else {");
        codeLines.add("            orders.add(criteriaBuilder.asc(root.get(field)));");
        codeLines.add("        }");
        codeLines.add("    });");
        codeLines.add("}");
        codeLines.add("criteriaQuery.select(root).orderBy(orders);");
        codeLines.add("if (" + handler + " != null) {");
        codeLines.add("    final var predicate = " + handler + ".toPredicate(criteriaBuilder, criteriaQuery, root);");
        codeLines.add("    criteriaQuery.where(predicate);");
        codeLines.add("}");
        codeLines.add("java.util.List<" + entityType + "> queryResult = new java.util.ArrayList<>();");
        codeLines.add("return io.vertx.core.Future.fromCompletionStage(");
        codeLines.add("        (java.util.concurrent.CompletionStage<java.util.List<" + entityType + ">>) " + session
                + ".createQuery(criteriaQuery)");
        codeLines.add("                .setFirstResult(" + pageable + ".getOffset())");
        codeLines.add("                .setMaxResults(" + pageable + ".getPageSize())");
        codeLines.add("                .getResultList()");
        codeLines.add(").map(rs ->");
        codeLines.add("        {");
        codeLines.add("            queryResult.addAll(rs);");
        codeLines.add("            return vn.io.lcx.common.constant.CommonConstant.VOID;");
        codeLines.add("        }");
        codeLines.add(").compose(v ->");
        codeLines.add("        {");
        codeLines.add("            final var countQuery = criteriaBuilder.createQuery(Long.class);");
        codeLines.add("            final var countRoot = countQuery.from(" + entityType + ".class);");
        codeLines.add("            countQuery.select(criteriaBuilder.count(countRoot));");
        codeLines.add("            if (" + handler + " != null) {");
        codeLines.add("                jakarta.persistence.criteria.Predicate countPredicate = " + handler
                + ".toPredicate(criteriaBuilder, countQuery, countRoot);");
        codeLines.add("                countQuery.where(countPredicate);");
        codeLines.add("            }");
        codeLines.add("            return io.vertx.core.Future.fromCompletionStage(");
        codeLines.add("                    (java.util.concurrent.CompletionStage<Long>) " + session
                + ".createQuery(countQuery)");
        codeLines.add("                            .getSingleResult()");
        codeLines.add("            );");
        codeLines.add("        }");
        codeLines.add(").map(totalItems ->");
        codeLines.add("        vn.io.lcx.common.database.pageable.Page.<" + entityType + ">create(");
        codeLines.add("                queryResult,");
        codeLines.add("                totalItems,");
        codeLines.add("                " + pageable + ".getPageNumber(),");
        codeLines.add("                " + pageable + ".getPageSize()");
        codeLines.add("        )");
        codeLines.add(");");
    }

    private void buildFindOneMethod(List<String> codeLines,
                                    List<? extends VariableElement> parameters,
                                    TypeMirror entityType) {
        String session = parameters.get(0).getSimpleName().toString();
        String handler = parameters.get(1).getSimpleName().toString();

        codeLines.add("final var criteriaBuilder = " + session + ".getCriteriaBuilder();");
        codeLines.add("final var criteriaQuery = criteriaBuilder.createQuery(" + entityType + ".class);");
        codeLines.add("final var root = criteriaQuery.from(" + entityType + ".class);");
        codeLines.add("criteriaQuery.select(root);");
        codeLines.add("if (" + handler + " != null) {");
        codeLines.add("    final var predicate = " + handler + ".toPredicate(criteriaBuilder, criteriaQuery, root);");
        codeLines.add("    criteriaQuery.where(predicate);");
        codeLines.add("}");
        codeLines.add("return io.vertx.core.Future.fromCompletionStage(");
        codeLines.add("        (java.util.concurrent.CompletionStage<" + entityType + ">) " + session
                + ".createQuery(criteriaQuery).getSingleResultOrNull()");
        codeLines.add(").map(java.util.Optional::ofNullable);");
    }

    private void buildQueryMethodCodeBody(ExecutableElement executableElement,
                                          List<String> codeLines,
                                          List<? extends VariableElement> parameters,
                                          HRReturnTypeModel returnTypeModel) {
        String session = parameters.get(0).getSimpleName().toString();
        List<VariableElement> actualParameters = extractActualParameters(parameters);
        HRQuery queryAnnotation = executableElement.getAnnotation(HRQuery.class);
        boolean isModifying = executableElement.getAnnotation(HRModifying.class) != null;
        HRResultSetMapping resultSetMapping = executableElement.getAnnotation(HRResultSetMapping.class);
        QueryPlan queryPlan = buildQueryPlan(queryAnnotation.value(), actualParameters, null);

        addQueryCreation(codeLines, session, queryAnnotation, resultSetMapping, returnTypeModel, isModifying, queryPlan.rawQuery);
        addQueryBindings(codeLines, "query", queryPlan, "");

        if (isModifying) {
            if (returnTypeModel.kind == ReturnKind.MODIFIED_COUNT) {
                codeLines.add("return io.vertx.core.Future.fromCompletionStage((java.util.concurrent.CompletionStage<Integer>) query.executeUpdate());");
            } else {
                codeLines.add("return io.vertx.core.Future.fromCompletionStage((java.util.concurrent.CompletionStage<Integer>) query.executeUpdate()).map(v -> null);");
            }
            return;
        }

        switch (returnTypeModel.kind) {
            case PAGE:
                addPageQueryResult(codeLines, session, queryAnnotation, actualParameters, returnTypeModel);
                break;
            case LIST:
                codeLines.add("return io.vertx.core.Future.fromCompletionStage((java.util.concurrent.CompletionStage<java.util.List<"
                        + returnTypeModel.elementType + ">>) query.getResultList());");
                break;
            case OPTIONAL:
                codeLines.add("return io.vertx.core.Future.fromCompletionStage((java.util.concurrent.CompletionStage<"
                        + returnTypeModel.elementType + ">) query.getSingleResultOrNull()).map(java.util.Optional::ofNullable);");
                break;
            case ENTITY:
            default:
                codeLines.add("return io.vertx.core.Future.fromCompletionStage((java.util.concurrent.CompletionStage<"
                        + returnTypeModel.elementType + ">) query.getSingleResult());");
                break;
        }
    }

    private void addQueryCreation(List<String> codeLines,
                                  String session,
                                  HRQuery queryAnnotation,
                                  HRResultSetMapping resultSetMapping,
                                  HRReturnTypeModel returnTypeModel,
                                  boolean isModifying,
                                  String queryValue) {
        String escapedQuery = escapeJava(queryValue);
        if (resultSetMapping != null) {
            codeLines.add("final var query = " + session + ".createNativeQuery(\"" + escapedQuery
                    + "\", new org.hibernate.reactive.common.ResultSetMapping<" + returnTypeModel.elementType + ">() {");
            codeLines.add("    @Override");
            codeLines.add("    public String getName() {");
            codeLines.add("        return \"" + escapeJava(resultSetMapping.name()) + "\";");
            codeLines.add("    }");
            codeLines.add("");
            codeLines.add("    @Override");
            codeLines.add("    public Class<" + returnTypeModel.elementType + "> getResultType() {");
            codeLines.add("        return " + returnTypeModel.elementType + ".class;");
            codeLines.add("    }");
            codeLines.add("});");
            return;
        }

        if (isModifying) {
            if (queryAnnotation.isNative()) {
                codeLines.add("final var query = " + session + ".createNativeQuery(\"" + escapedQuery + "\");");
            } else {
                codeLines.add("final var query = " + session + ".createMutationQuery(\"" + escapedQuery + "\");");
            }
            return;
        }

        if (queryAnnotation.isNative()) {
            codeLines.add("final var query = " + session + ".createNativeQuery(\"" + escapedQuery + "\", "
                    + returnTypeModel.elementType + ".class);");
        } else {
            codeLines.add("final var query = " + session + ".createQuery(\"" + escapedQuery + "\", "
                    + returnTypeModel.elementType + ".class);");
        }
    }

    private void addPageQueryResult(List<String> codeLines,
                                    String session,
                                    HRQuery queryAnnotation,
                                    List<VariableElement> actualParameters,
                                    HRReturnTypeModel returnTypeModel) {
        String pageableParam = actualParameters.get(actualParameters.size() - 1).getSimpleName().toString();
        String countQuery = StringUtils.isNotBlank(queryAnnotation.countQuery())
                ? removeTrailingSemicolon(queryAnnotation.countQuery())
                : buildAutoCountQuery(queryAnnotation.value());
        QueryPlan countPlan = buildQueryPlan(countQuery, actualParameters, null);
        String countCreateCall = queryAnnotation.isNative()
                ? session + ".createNativeQuery(\"" + escapeJava(countPlan.rawQuery) + "\", Long.class)"
                : session + ".createQuery(\"" + escapeJava(countPlan.rawQuery) + "\", Long.class)";

        codeLines.add("java.util.List<" + returnTypeModel.elementType + "> queryResult = new java.util.ArrayList<>();");
        codeLines.add("return io.vertx.core.Future.fromCompletionStage(");
        codeLines.add("        (java.util.concurrent.CompletionStage<java.util.List<" + returnTypeModel.elementType + ">>) query");
        codeLines.add("                .setFirstResult(" + pageableParam + ".getOffset())");
        codeLines.add("                .setMaxResults(" + pageableParam + ".getPageSize())");
        codeLines.add("                .getResultList()");
        codeLines.add(").map(rs -> {");
        codeLines.add("    queryResult.addAll(rs);");
        codeLines.add("    return vn.io.lcx.common.constant.CommonConstant.VOID;");
        codeLines.add("})");
        codeLines.add(".compose(v -> {");
        codeLines.add("    final var countQuery = " + countCreateCall + ";");
        addQueryBindings(codeLines, "countQuery", countPlan, "    ");
        codeLines.add("    return io.vertx.core.Future.fromCompletionStage(");
        codeLines.add("            (java.util.concurrent.CompletionStage<Long>) countQuery.getSingleResult()");
        codeLines.add("    );");
        codeLines.add("})");
        codeLines.add(".map(totalItems ->");
        codeLines.add("    vn.io.lcx.common.database.pageable.Page.<" + returnTypeModel.elementType + ">create(");
        codeLines.add("            queryResult,");
        codeLines.add("            totalItems,");
        codeLines.add("            " + pageableParam + ".getPageNumber(),");
        codeLines.add("            " + pageableParam + ".getPageSize()");
        codeLines.add("    )");
        codeLines.add(");");
    }

    private void addQueryBindings(List<String> codeLines, String queryVariable, QueryPlan queryPlan, String prefix) {
        Set<String> emitted = new HashSet<>();
        for (QueryBinding binding : queryPlan.bindings) {
            String key = binding.named
                    ? "named:" + binding.queryParameterName
                    : "position:" + (binding.parameterIndex + 1);
            if (!emitted.add(key)) {
                continue;
            }
            if (binding.named) {
                codeLines.add(prefix + queryVariable + ".setParameter(\""
                        + escapeJava(binding.queryParameterName) + "\", " + binding.parameterName + ");");
            } else {
                codeLines.add(prefix + queryVariable + ".setParameter("
                        + (binding.parameterIndex + 1) + ", " + binding.parameterName + ");");
            }
        }
    }

    private boolean validateRepository(ProcessorClassInfo processorClassInfo, TypeMirror entityTypeMirror) {
        boolean valid = true;
        for (var entry : processorClassInfo.getMethods().entrySet()) {
            MethodInfo methodInfo = entry.getKey();
            ExecutableElement method = entry.getValue();
            if (shouldSkipMethod(method)) {
                continue;
            }
            valid &= validateMethod(methodInfo, method, entityTypeMirror);
        }
        return valid;
    }

    private boolean validateMethod(MethodInfo methodInfo,
                                   ExecutableElement method,
                                   TypeMirror entityTypeMirror) {
        boolean valid = true;
        valid &= rejectWrongAnnotations(method);

        List<? extends VariableElement> parameters = method.getParameters();
        if (!isFuture(method.getReturnType())) {
            error(method, "@HRRepository methods must return " + FUTURE_TYPE + "<X>");
            valid = false;
        }
        if (parameters.isEmpty()) {
            error(method, "First parameter must be " + STAGE_SESSION_TYPE);
            valid = false;
        } else if (!isStageSession(parameters.get(0))) {
            error(method, "First parameter must be " + STAGE_SESSION_TYPE);
            valid = false;
        }

        List<VariableElement> actualParameters = extractActualParameters(parameters);
        for (int i = 0; i < actualParameters.size() - 1; i++) {
            if (isPageable(actualParameters.get(i))) {
                error(method, "Pageable parameter must be the final repository method parameter");
                valid = false;
            }
        }

        HRQuery query = method.getAnnotation(HRQuery.class);
        if (query == null) {
            if (method.getAnnotation(HRModifying.class) != null) {
                error(method, "@HRModifying methods must also declare @HRQuery");
                valid = false;
            }
            if (method.getAnnotation(HRResultSetMapping.class) != null) {
                error(method, "@HRResultSetMapping methods must also declare @HRQuery");
                valid = false;
            }
            if (isBaseCrudMethod(methodInfo.getMethodName())) {
                valid &= validateBaseCrudMethod(methodInfo, method, entityTypeMirror);
            } else {
                error(method, "Custom HR repository methods must use @vn.io.lcx.reactive.annotation.HRQuery");
                valid = false;
            }
            return valid;
        }

        HRReturnTypeModel returnTypeModel = buildReturnTypeModel(method, entityTypeMirror);
        if (returnTypeModel.kind == ReturnKind.UNSUPPORTED) {
            error(method, returnTypeModel.error);
            valid = false;
        }
        if (returnTypeModel.kind == ReturnKind.PAGE && !lastParameterIsPageable(actualParameters)) {
            error(method, "Future<Page<T>> HR query methods must declare Pageable as the final parameter");
            valid = false;
        }
        if (lastParameterIsPageable(actualParameters) && returnTypeModel.kind != ReturnKind.PAGE) {
            error(method, "Pageable parameters are only supported for Future<Page<T>> HR query methods");
            valid = false;
        }

        boolean isModifying = method.getAnnotation(HRModifying.class) != null;
        if (isModifying && returnTypeModel.kind != ReturnKind.MODIFIED_COUNT && returnTypeModel.kind != ReturnKind.VOID) {
            error(method, "@HRModifying methods must return Future<Integer> or Future<Void>");
            valid = false;
        }
        if (!isModifying && (returnTypeModel.kind == ReturnKind.MODIFIED_COUNT || returnTypeModel.kind == ReturnKind.VOID)) {
            error(method, "Future<Integer> and Future<Void> are only supported for @HRModifying methods");
            valid = false;
        }

        HRResultSetMapping resultSetMapping = method.getAnnotation(HRResultSetMapping.class);
        if (resultSetMapping != null) {
            if (!query.isNative()) {
                error(method, "@HRResultSetMapping requires @HRQuery(isNative = true)");
                valid = false;
            }
            if (isModifying) {
                error(method, "@HRResultSetMapping is not supported on @HRModifying methods");
                valid = false;
            }
            if (returnTypeModel.kind == ReturnKind.PAGE) {
                error(method, "@HRResultSetMapping is not supported for Future<Page<T>> methods");
                valid = false;
            }
        }

        valid &= validateQuery(query.value(), actualParameters, method);
        if (returnTypeModel.kind == ReturnKind.PAGE) {
            if (StringUtils.isBlank(query.countQuery()) && isComplexCountQuery(query.value())) {
                error(method, "@HRQuery method returning Page<T> uses a complex query and must define countQuery()");
                valid = false;
            } else if (StringUtils.isNotBlank(query.countQuery())) {
                valid &= validateQuery(query.countQuery(), actualParameters, method);
            }
        }
        return valid;
    }

    private boolean rejectWrongAnnotations(ExecutableElement method) {
        boolean valid = true;
        if (method.getAnnotation(vn.io.lcx.jpa.annotation.Query.class) != null) {
            error(method, "Use @vn.io.lcx.reactive.annotation.HRQuery for @HRRepository methods instead of @vn.io.lcx.jpa.annotation.Query");
            valid = false;
        }
        if (method.getAnnotation(vn.io.lcx.reactive.annotation.Query.class) != null) {
            error(method, "Use @vn.io.lcx.reactive.annotation.HRQuery for @HRRepository methods instead of @vn.io.lcx.reactive.annotation.Query");
            valid = false;
        }
        if (method.getAnnotation(vn.io.lcx.jpa.annotation.Modifying.class) != null) {
            error(method, "Use @vn.io.lcx.reactive.annotation.HRModifying for @HRRepository methods instead of @vn.io.lcx.jpa.annotation.Modifying");
            valid = false;
        }
        if (method.getAnnotation(vn.io.lcx.jpa.annotation.ResultSetMapping.class) != null) {
            error(method, "Use @vn.io.lcx.reactive.annotation.HRResultSetMapping for @HRRepository methods instead of @vn.io.lcx.jpa.annotation.ResultSetMapping");
            valid = false;
        }
        for (VariableElement parameter : method.getParameters()) {
            if (parameter.getAnnotation(vn.io.lcx.jpa.annotation.Param.class) != null) {
                error(parameter, "Use @vn.io.lcx.reactive.annotation.HRParam for @HRRepository methods instead of @vn.io.lcx.jpa.annotation.Param");
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateBaseCrudMethod(MethodInfo methodInfo, ExecutableElement method, TypeMirror entityTypeMirror) {
        String methodName = methodInfo.getMethodName();
        List<? extends VariableElement> parameters = method.getParameters();
        String returnType = resolveTypeName(method.getReturnType(), entityTypeMirror);
        String entityType = entityTypeMirror.toString();
        String entityListType = "java.util.List<" + entityType + ">";
        boolean valid = true;

        switch (methodName) {
            case "save":
                valid &= requireParameterCount(method, parameters, 2);
                if (parameters.size() == 2) {
                    String modelType = resolveTypeName(parameters.get(1).asType(), entityTypeMirror);
                    if (modelType.equals(entityType)) {
                        valid &= requireReturnType(method, returnType, futureOf(entityType));
                    } else if (modelType.equals(entityListType)) {
                        valid &= requireReturnType(method, returnType, futureOf(entityListType));
                    } else {
                        error(method, "save must accept " + entityType + " or java.util.List<" + entityType + ">");
                        valid = false;
                    }
                }
                break;
            case "delete":
                valid &= requireParameterCount(method, parameters, 2);
                if (parameters.size() == 2) {
                    String modelType = resolveTypeName(parameters.get(1).asType(), entityTypeMirror);
                    if (!modelType.equals(entityType) && !modelType.equals(entityListType)) {
                        error(method, "delete must accept " + entityType + " or java.util.List<" + entityType + ">");
                        valid = false;
                    }
                    valid &= requireReturnType(method, returnType, futureOf("java.lang.Void"));
                }
                break;
            case "find":
                if (parameters.size() == 2) {
                    valid &= requireCriteriaHandler(method, parameters.get(1));
                    valid &= requireReturnType(method, returnType, futureOf("java.util.List<" + entityType + ">"));
                } else if (parameters.size() == 3) {
                    valid &= requireCriteriaHandler(method, parameters.get(1));
                    if (!isPageable(parameters.get(2))) {
                        error(method, "find page method must declare Pageable as the final parameter");
                        valid = false;
                    }
                    valid &= requireReturnType(method, returnType,
                            futureOf(PAGE_TYPE + "<" + entityType + ">"));
                } else {
                    error(method, "find must match HReactiveRepository find signatures");
                    valid = false;
                }
                break;
            case "findOne":
                valid &= requireParameterCount(method, parameters, 2);
                if (parameters.size() == 2) {
                    valid &= requireCriteriaHandler(method, parameters.get(1));
                    valid &= requireReturnType(method, returnType, futureOf("java.util.Optional<" + entityType + ">"));
                }
                break;
            default:
                break;
        }
        return valid;
    }

    private boolean requireParameterCount(ExecutableElement method, List<? extends VariableElement> parameters, int expected) {
        if (parameters.size() == expected) {
            return true;
        }
        error(method, method.getSimpleName() + " must declare exactly " + expected + " parameters");
        return false;
    }

    private boolean requireCriteriaHandler(ExecutableElement method, VariableElement parameter) {
        if (isDeclaredAssignableTo(parameter.asType(), CRITERIA_HANDLER_TYPE)) {
            return true;
        }
        error(method, "find methods must use " + CRITERIA_HANDLER_TYPE + "<T>");
        return false;
    }

    private boolean requireReturnType(ExecutableElement method, String actual, String expected) {
        if (actual.equals(expected)) {
            return true;
        }
        error(method, method.getSimpleName() + " must return " + expected);
        return false;
    }

    private boolean validateQuery(String query, List<VariableElement> actualParameters, Element element) {
        return buildQueryPlan(query, actualParameters, element).valid;
    }

    private HRReturnTypeModel buildReturnTypeModel(ExecutableElement method, TypeMirror entityTypeMirror) {
        TypeMirror returnType = method.getReturnType();
        if (!(returnType instanceof DeclaredType futureType) || !isFuture(returnType)) {
            return HRReturnTypeModel.unsupported("Return type must be io.vertx.core.Future<X>");
        }
        if (futureType.getTypeArguments().isEmpty()) {
            return HRReturnTypeModel.unsupported("Future return type must declare an output type");
        }

        TypeMirror innerType = futureType.getTypeArguments().get(0);
        String futureOutputType = resolveTypeName(innerType, entityTypeMirror);
        if (innerType instanceof DeclaredType declaredType) {
            String rawType = declaredType.asElement().asType().toString();
            if (rawType.startsWith("java.util.List") && !declaredType.getTypeArguments().isEmpty()) {
                String elementType = resolveTypeName(declaredType.getTypeArguments().get(0), entityTypeMirror);
                return new HRReturnTypeModel(ReturnKind.LIST, futureOutputType, elementType);
            }
            if (rawType.startsWith("vn.io.lcx.common.database.pageable.Page") && !declaredType.getTypeArguments().isEmpty()) {
                String elementType = resolveTypeName(declaredType.getTypeArguments().get(0), entityTypeMirror);
                return new HRReturnTypeModel(ReturnKind.PAGE, futureOutputType, elementType);
            }
            if (rawType.startsWith("java.util.Optional") && !declaredType.getTypeArguments().isEmpty()) {
                String elementType = resolveTypeName(declaredType.getTypeArguments().get(0), entityTypeMirror);
                if (elementType.startsWith("java.util.List<")) {
                    return HRReturnTypeModel.unsupported("Future<Optional<List<T>>> is not supported");
                }
                return new HRReturnTypeModel(ReturnKind.OPTIONAL, futureOutputType, elementType);
            }
        }

        String outputType = resolveTypeName(innerType, entityTypeMirror);
        if (outputType.equals("java.lang.Integer")) {
            return new HRReturnTypeModel(ReturnKind.MODIFIED_COUNT, futureOutputType, outputType);
        }
        if (outputType.equals("java.lang.Void")) {
            return new HRReturnTypeModel(ReturnKind.VOID, futureOutputType, outputType);
        }
        if (innerType.getKind().isPrimitive()) {
            return HRReturnTypeModel.unsupported("Primitive query return types are not supported inside Future; use wrapper types");
        }
        if (outputType.startsWith("java.")) {
            return HRReturnTypeModel.unsupported("Unsupported HR query return type: " + outputType);
        }
        return new HRReturnTypeModel(ReturnKind.ENTITY, futureOutputType, outputType);
    }

    private QueryPlan buildQueryPlan(String statementValue,
                                     List<VariableElement> actualParameters,
                                     Element diagnosticElement) {
        String rawQuery = removeTrailingSemicolon(statementValue);
        QueryPlan plan = new QueryPlan(rawQuery);
        PlaceholderStyle style = PlaceholderStyle.NONE;
        int implicitIndex = 0;
        boolean inString = false;

        for (int i = 0; i < rawQuery.length(); i++) {
            char current = rawQuery.charAt(i);
            if (current == '\'') {
                if (inString && i + 1 < rawQuery.length() && rawQuery.charAt(i + 1) == '\'') {
                    i++;
                } else {
                    inString = !inString;
                }
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == '?') {
                int placeholderEnd = i + 1;
                while (placeholderEnd < rawQuery.length() && Character.isDigit(rawQuery.charAt(placeholderEnd))) {
                    placeholderEnd++;
                }
                boolean explicit = placeholderEnd > i + 1;
                PlaceholderStyle currentStyle = explicit ? PlaceholderStyle.EXPLICIT : PlaceholderStyle.IMPLICIT;
                style = updatePlaceholderStyle(plan, style, currentStyle, diagnosticElement);
                int parameterIndex;
                if (explicit) {
                    parameterIndex = Integer.parseInt(rawQuery.substring(i + 1, placeholderEnd)) - 1;
                    if (parameterIndex < 0) {
                        plan.error("Query placeholder indexes are 1-based", diagnosticElement);
                    }
                } else {
                    parameterIndex = implicitIndex++;
                }
                addPositionalBinding(plan, actualParameters, parameterIndex, diagnosticElement);
                i = placeholderEnd - 1;
            } else if (current == ':' && isNamedPlaceholderStart(rawQuery, i)) {
                int placeholderEnd = i + 2;
                while (placeholderEnd < rawQuery.length() && isNamedPlaceholderPart(rawQuery.charAt(placeholderEnd))) {
                    placeholderEnd++;
                }
                style = updatePlaceholderStyle(plan, style, PlaceholderStyle.NAMED, diagnosticElement);
                String placeholderName = rawQuery.substring(i + 1, placeholderEnd);
                addNamedBinding(plan, actualParameters, placeholderName, diagnosticElement);
                i = placeholderEnd - 1;
            }
        }

        validateAllQueryParametersUsed(plan, actualParameters, diagnosticElement);
        return plan;
    }

    private PlaceholderStyle updatePlaceholderStyle(QueryPlan plan,
                                                    PlaceholderStyle existing,
                                                    PlaceholderStyle current,
                                                    Element diagnosticElement) {
        if (existing == PlaceholderStyle.NONE) {
            return current;
        }
        if (existing != current) {
            plan.error("Do not mix implicit ?, indexed ?1, and named :name query placeholders", diagnosticElement);
        }
        return existing;
    }

    private boolean isNamedPlaceholderStart(String query, int colonIndex) {
        if (colonIndex + 1 >= query.length() || query.charAt(colonIndex + 1) == ':') {
            return false;
        }
        if (colonIndex > 0 && query.charAt(colonIndex - 1) == ':') {
            return false;
        }
        char next = query.charAt(colonIndex + 1);
        return Character.isJavaIdentifierStart(next);
    }

    private boolean isNamedPlaceholderPart(char value) {
        return Character.isJavaIdentifierPart(value);
    }

    private void addPositionalBinding(QueryPlan plan,
                                      List<VariableElement> actualParameters,
                                      int parameterIndex,
                                      Element diagnosticElement) {
        int parameterCount = actualQueryParameterCount(actualParameters);
        if (parameterIndex >= parameterCount) {
            plan.error("Query placeholder index exceeds repository method parameter count", diagnosticElement);
            return;
        }
        if (parameterIndex < 0) {
            return;
        }
        VariableElement parameter = actualParameters.get(parameterIndex);
        plan.bindings.add(new QueryBinding(
                parameterIndex,
                parameter.getSimpleName().toString(),
                String.valueOf(parameterIndex + 1),
                false
        ));
    }

    private void addNamedBinding(QueryPlan plan,
                                 List<VariableElement> actualParameters,
                                 String placeholderName,
                                 Element diagnosticElement) {
        int parameterCount = actualQueryParameterCount(actualParameters);
        for (int i = 0; i < parameterCount; i++) {
            VariableElement parameter = actualParameters.get(i);
            HRParam hrParam = parameter.getAnnotation(HRParam.class);
            if (hrParam != null && hrParam.value().equals(placeholderName)) {
                plan.bindings.add(new QueryBinding(
                        i,
                        parameter.getSimpleName().toString(),
                        placeholderName,
                        true
                ));
                return;
            }
        }
        for (int i = 0; i < parameterCount; i++) {
            VariableElement parameter = actualParameters.get(i);
            if (parameter.getSimpleName().contentEquals(placeholderName)) {
                plan.error("Named placeholders require @HRParam on parameter '" + parameter.getSimpleName() + "'", diagnosticElement);
                return;
            }
        }
        plan.error("Named query placeholder ':" + placeholderName + "' does not match any @HRParam value", diagnosticElement);
    }

    private void validateAllQueryParametersUsed(QueryPlan plan,
                                                List<VariableElement> actualParameters,
                                                Element diagnosticElement) {
        boolean[] used = new boolean[actualQueryParameterCount(actualParameters)];
        for (QueryBinding binding : plan.bindings) {
            if (binding.parameterIndex >= 0 && binding.parameterIndex < used.length) {
                used[binding.parameterIndex] = true;
            }
        }
        for (int i = 0; i < used.length; i++) {
            if (!used[i]) {
                plan.error("Query parameter '" + actualParameters.get(i).getSimpleName()
                        + "' is not referenced by any placeholder", diagnosticElement);
            }
        }
    }

    private int actualQueryParameterCount(List<VariableElement> actualParameters) {
        if (lastParameterIsPageable(actualParameters)) {
            return actualParameters.size() - 1;
        }
        return actualParameters.size();
    }

    private String buildAutoCountQuery(String query) {
        String cleaned = removeTrailingSemicolon(query);
        int fromIndex = findTopLevelKeyword(cleaned, "from", 0);
        if (fromIndex < 0) {
            return "SELECT COUNT(1)";
        }
        int orderByIndex = findTopLevelKeyword(cleaned, "order by", fromIndex);
        String fromClause = orderByIndex >= 0 ? cleaned.substring(fromIndex, orderByIndex) : cleaned.substring(fromIndex);
        return "SELECT COUNT(1) " + fromClause.strip();
    }

    private boolean isComplexCountQuery(String query) {
        String cleaned = stripQuotedSql(query).toLowerCase();
        return cleaned.contains(" with ")
                || cleaned.stripLeading().startsWith("with ")
                || cleaned.contains(" union ")
                || cleaned.contains(" distinct ")
                || cleaned.contains(" group by ")
                || cleaned.contains(" having ")
                || cleaned.contains(" fetch ");
    }

    private int findTopLevelKeyword(String query, String keyword, int start) {
        String lower = stripQuotedSql(query).toLowerCase();
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

    private String stripQuotedSql(String query) {
        StringBuilder builder = new StringBuilder(query.length());
        boolean inString = false;
        for (int i = 0; i < query.length(); i++) {
            char current = query.charAt(i);
            if (current == '\'') {
                builder.append(' ');
                if (inString && i + 1 < query.length() && query.charAt(i + 1) == '\'') {
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

    private String removeTrailingSemicolon(String query) {
        int end = query.length() - 1;
        while (end >= 0 && Character.isWhitespace(query.charAt(end))) {
            end--;
        }
        if (end >= 0 && query.charAt(end) == ';') {
            return query.substring(0, end) + query.substring(end + 1);
        }
        return query;
    }

    private List<VariableElement> extractActualParameters(List<? extends VariableElement> parameters) {
        List<VariableElement> actualParameters = new ArrayList<>();
        for (int i = 1; i < parameters.size(); i++) {
            actualParameters.add(parameters.get(i));
        }
        return actualParameters;
    }

    private String resolveTypeName(TypeMirror typeMirror, TypeMirror entityTypeMirror) {
        String value = typeMirror.toString();
        if (value.equals("T")) {
            return entityTypeMirror.toString();
        }
        return value.replace("<T>", "<" + entityTypeMirror + ">");
    }

    private boolean isBaseCrudMethod(String methodName) {
        return methodName.equals("save")
                || methodName.equals("delete")
                || methodName.equals("find")
                || methodName.equals("findOne");
    }

    private String futureOf(String innerType) {
        return FUTURE_TYPE + "<" + innerType + ">";
    }

    private boolean isFuture(TypeMirror typeMirror) {
        return isDeclaredAssignableTo(typeMirror, FUTURE_TYPE);
    }

    private boolean isStageSession(VariableElement variableElement) {
        return isDeclaredAssignableTo(variableElement.asType(), STAGE_SESSION_TYPE);
    }

    private boolean lastParameterIsPageable(List<VariableElement> actualParameters) {
        return !actualParameters.isEmpty() && isPageable(actualParameters.get(actualParameters.size() - 1));
    }

    private boolean isPageable(VariableElement variableElement) {
        return isDeclaredAssignableTo(variableElement.asType(), PAGEABLE_TYPE);
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

    private String escapeJava(String value) {
        return StringEscapeUtils.escapeJava(value);
    }

    private void error(Element element, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private enum ReturnKind {
        ENTITY,
        OPTIONAL,
        LIST,
        PAGE,
        MODIFIED_COUNT,
        VOID,
        UNSUPPORTED
    }

    private enum PlaceholderStyle {
        NONE,
        IMPLICIT,
        EXPLICIT,
        NAMED
    }

    private static final class HRReturnTypeModel {
        private final ReturnKind kind;
        private final String futureOutputType;
        private final String elementType;
        private final String error;

        private HRReturnTypeModel(ReturnKind kind, String futureOutputType, String elementType) {
            this(kind, futureOutputType, elementType, null);
        }

        private HRReturnTypeModel(ReturnKind kind, String futureOutputType, String elementType, String error) {
            this.kind = kind;
            this.futureOutputType = futureOutputType;
            this.elementType = elementType;
            this.error = error;
        }

        private static HRReturnTypeModel unsupported(String error) {
            return new HRReturnTypeModel(ReturnKind.UNSUPPORTED, "java.lang.Object", "java.lang.Object", error);
        }
    }

    private final class QueryPlan {
        private final String rawQuery;
        private final List<QueryBinding> bindings = new ArrayList<>();
        private boolean valid = true;

        private QueryPlan(String rawQuery) {
            this.rawQuery = rawQuery;
        }

        private void error(String message, Element element) {
            valid = false;
            if (element != null) {
                HRRepositoryProcessor.this.error(element, message);
            }
        }
    }

    private static final class QueryBinding {
        private final int parameterIndex;
        private final String parameterName;
        private final String queryParameterName;
        private final boolean named;

        private QueryBinding(int parameterIndex, String parameterName, String queryParameterName, boolean named) {
            this.parameterIndex = parameterIndex;
            this.parameterName = parameterName;
            this.queryParameterName = queryParameterName;
            this.named = named;
        }
    }
}
