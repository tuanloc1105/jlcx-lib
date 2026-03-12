package vn.io.lcx.processor;

import org.apache.commons.lang3.StringUtils;
import vn.io.lcx.common.utils.ExceptionUtils;
import vn.io.lcx.common.utils.FileUtils;
import vn.io.lcx.common.utils.MyStringUtils;
import vn.io.lcx.jpa.annotation.Modifying;
import vn.io.lcx.jpa.annotation.Param;
import vn.io.lcx.jpa.annotation.Query;
import vn.io.lcx.processor.utility.ProcessorClassInfo;
import vn.io.lcx.processor.utility.TypeHierarchyAnalyzer;
import vn.io.lcx.reactive.annotation.HRRepository;
import vn.io.lcx.reactive.repository.HReactiveRepository;

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
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("vn.io.lcx.reactive.annotation.HRRepository")
public class HRRepositoryProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(HRRepository.class)) {
            if (annotatedElement instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                try {
                    ProcessorClassInfo processorClassInfo = ProcessorClassInfo.init(
                            typeElement,
                            processingEnv.getTypeUtils(),
                            processingEnv.getElementUtils());
                    generateCode(processorClassInfo);
                } catch (Exception e) {
                    this.processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            ExceptionUtils.getStackTrace(e));
                }
            }
        }
        return true;
    }

    public void generateCode(ProcessorClassInfo processorClassInfo) throws IOException {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                vn.io.lcx.common.utils.DateTimeUtils
                        .toUnixMillis(vn.io.lcx.common.utils.DateTimeUtils.generateCurrentTimeDefault()) + ": " +
                        String.format(
                                "Generating code for HRRepository : %s",
                                processorClassInfo.getClazz().getQualifiedName()));

        List<TypeMirror> genericClasses = TypeHierarchyAnalyzer.getGenericTypeOfExtendingInterface(
                processingEnv.getElementUtils(),
                processingEnv.getTypeUtils(),
                processorClassInfo.getClazz(),
                HReactiveRepository.class.getName());

        if (genericClasses.isEmpty()) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "HRRepository must extend HReactiveRepository with generic type parameters: "
                            + processorClassInfo.getClazz().getQualifiedName());
            return;
        }

        String repositoryTemplate = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/repository-template.txt");
        String methodTemplate = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/method-template.txt");

        if (StringUtils.isBlank(repositoryTemplate) || StringUtils.isBlank(methodTemplate)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Failed to load template files for HRRepository code generation");
            return;
        }

        final TypeMirror entityTypeMirror = genericClasses.get(0);
        StringBuilder methodCodeBody = new StringBuilder();
        methodCodeBody.append("\n");

        processorClassInfo.getMethods().forEach((methodInfo, executableElement) -> {
            final String actualReturnType;
            if (methodInfo.getOutputParameter().toString().equals("T")) {
                actualReturnType = entityTypeMirror.toString();
            } else if (methodInfo.getOutputParameter().toString().contains("<T>")) {
                actualReturnType = methodInfo.getOutputParameter().toString().replace("<T>",
                        "<" + entityTypeMirror.toString() + ">");
            } else {
                actualReturnType = methodInfo.getOutputParameter().toString();
            }

            final var codeLines = new ArrayList<String>();
            if (methodInfo.getInputParameters().isEmpty()) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Method " + methodInfo.getMethodName()
                                + " must have at least one parameter (Stage.Session)");
                return;
            }

            final VariableElement sessionVariable = methodInfo.getInputParameters().get(0);
            if (!sessionVariable.asType().toString().equals("org.hibernate.reactive.stage.Stage.Session")) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "First parameter of method " + methodInfo.getMethodName()
                                + " must be org.hibernate.reactive.stage.Stage.Session");
                return;
            }

            final String methodName = methodInfo.getMethodName();
            final List<? extends VariableElement> parameters = methodInfo.getInputParameters();

            if (executableElement.getAnnotation(Query.class) != null) {
                buildQueryMethodCodeBody(executableElement, codeLines, parameters, actualReturnType, entityTypeMirror);
            } else {
                if (parameters.size() < 2) {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "Method " + methodName
                                    + " requires at least 2 parameters (Stage.Session, ...)");
                    return;
                }
                switch (methodName) {
                    case "save":
                        buildSaveMethod(codeLines, parameters, entityTypeMirror);
                        break;
                    case "delete":
                        buildDeleteMethod(codeLines, parameters, entityTypeMirror);
                        break;
                    case "find":
                        if (parameters.size() == 3 && parameters.get(2).asType().toString().contains("Pageable")) {
                            buildFindPageMethod(codeLines, parameters, entityTypeMirror);
                        } else {
                            buildFindListMethod(codeLines, parameters, entityTypeMirror);
                        }
                        break;
                    case "findOne":
                        buildFindOneMethod(codeLines, parameters, entityTypeMirror);
                        break;
                    default:
                        codeLines.add(
                                "return io.vertx.core.Future.failedFuture(new vn.io.lcx.jpa.exception.CodeGenError(\"Method "
                                        + methodName + " not implemented\"));");
                        break;
                }
            }

            if (!codeLines.isEmpty()) {
                methodCodeBody.append(
                        methodTemplate
                                .replace("${return-type}", actualReturnType)
                                .replace("${method-name}", methodName)
                                .replace("${list-of-parameters}",
                                        methodInfo.getInputParameters().stream().map(v -> {
                                            String type = v.asType().toString();
                                            if (type.equals("T"))
                                                type = entityTypeMirror.toString();
                                            type = type.replace("<T>", "<" + entityTypeMirror.toString() + ">");
                                            return type + " " + v.getSimpleName();
                                        }).collect(Collectors.joining(", ")))
                                .replace("${method-body}",
                                        codeLines.stream().collect(Collectors.joining("\n        "))))
                        .append("\n");
            }
        });

        final var packageName = processingEnv.getElementUtils().getPackageOf(processorClassInfo.getClazz())
                .getQualifiedName().toString();
        final var className = processorClassInfo.getClazz().getSimpleName() + "Impl";
        final var code = repositoryTemplate
                .replace("${package-name}", packageName)
                .replace("${proxy-class-name}", className)
                .replace("${interface-class-name}", processorClassInfo.getClazz().getSimpleName())
                .replace("${methods}", MyStringUtils.removeSuffixOfString(methodCodeBody.toString(), "\n"));

        String fullClassName = packageName + "." + className;
        JavaFileObject builderFile = this.processingEnv.getFiler().createSourceFile(fullClassName);
        try (Writer writer = builderFile.openWriter()) {
            writer.write(code);
        }
    }

    private void buildSaveMethod(List<String> codeLines,
            List<? extends VariableElement> parameters,
            TypeMirror entityType) {
        String session = parameters.get(0).getSimpleName().toString();
        String entity = parameters.get(1).getSimpleName().toString();
        if (parameters.get(1).asType().toString().contains("java.util.List")) {
            codeLines.add("io.vertx.core.Future<java.util.List<" + entityType
                    + ">> chain = io.vertx.core.Future.succeededFuture(new java.util.ArrayList<>());");
            codeLines.add(
                    "java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);");
            codeLines.add("for (" + entityType + " item : " + entity + ") {");
            codeLines.add("    chain = chain.compose(list ->");
            codeLines.add("            io.vertx.core.Future.fromCompletionStage(" + session + ".merge(item))");
            codeLines.add("                    .compose(rs ->");
            codeLines.add("                            count.incrementAndGet() % 50 == 0 ?");
            codeLines.add("                                    io.vertx.core.Future.fromCompletionStage(");
            codeLines.add("                                            " + session + ".flush()");
            codeLines.add(
                    "                                                    .thenAccept(v -> " + session + ".clear())");
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
        if (parameters.get(1).asType().toString().contains("java.util.List")) {
            codeLines.add("io.vertx.core.Future<Void> chain = io.vertx.core.Future.succeededFuture();");
            codeLines.add(
                    "java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);");
            codeLines.add("for (" + entityType + " item : " + entity + ") {");
            codeLines.add("    chain = chain.compose(v ->");
            codeLines.add("            io.vertx.core.Future.fromCompletionStage(" + session + ".remove(item))");
            codeLines.add("                    .compose(rs ->");
            codeLines.add("                            count.incrementAndGet() % 50 == 0 ?");
            codeLines.add("                                    io.vertx.core.Future.fromCompletionStage(");
            codeLines.add("                                            " + session + ".flush()");
            codeLines.add("                                                    .thenAccept(ignored -> " + session
                    + ".clear())");
            codeLines.add(
                    "                                    ).map(ignored -> null) : io.vertx.core.Future.succeededFuture()");
            codeLines.add("                    )");
            codeLines.add("    );");
            codeLines.add("}");
            codeLines.add("return chain;");
        } else {
            codeLines.add("return io.vertx.core.Future.fromCompletionStage(");
            codeLines
                    .add("        (java.util.concurrent.CompletionStage<Void>) " + session + ".remove(" + entity + ")");
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

        codeLines.add("final var pageimpl = (vn.io.lcx.common.database.pageable.PageableImpl) " + pageable + ";");
        codeLines.add("final var criteriaBuilder = " + session + ".getCriteriaBuilder();");
        codeLines.add("final var criteriaQuery = criteriaBuilder.createQuery(" + entityType + ".class);");
        codeLines.add("final var root = criteriaQuery.from(" + entityType + ".class);");
        codeLines.add("java.util.List<jakarta.persistence.criteria.Order> orders = new java.util.ArrayList<>();");
        codeLines.add("pageimpl.getFieldNameAndDirectionMap().forEach((field, direction) -> {");
        codeLines.add("    if (vn.io.lcx.common.database.pageable.Direction.DESC == direction) {");
        codeLines.add("        orders.add(criteriaBuilder.desc(root.get(field)));");
        codeLines.add("    } else {");
        codeLines.add("        orders.add(criteriaBuilder.asc(root.get(field)));");
        codeLines.add("    }");
        codeLines.add("});");
        codeLines.add("criteriaQuery.select(root).orderBy(orders);");
        codeLines.add("if (" + handler + " != null) {");
        codeLines.add("    final var predicate = " + handler + ".toPredicate(criteriaBuilder, criteriaQuery, root);");
        codeLines.add("    criteriaQuery.where(predicate);");
        codeLines.add("}");
        codeLines.add("java.util.List<" + entityType + "> queryResult = new java.util.ArrayList<>();");
        codeLines.add("return io.vertx.core.Future.fromCompletionStage(");
        codeLines.add("        (java.util.concurrent.CompletionStage<java.util.List<" + entityType + ">>) " + session
                + ".createQuery(criteriaQuery)");
        codeLines.add("                .setFirstResult(pageimpl.getOffset())");
        codeLines.add("                .setMaxResults(pageimpl.getPageSize())");
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
        codeLines.add("                pageimpl.getPageNumber(),");
        codeLines.add("                pageimpl.getPageSize()");
        codeLines.add("        )");
        codeLines.add(");");
    }

    private void buildFindOneMethod(List<String> codeLines, List<? extends VariableElement> parameters,
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

    private void buildQueryMethodCodeBody(ExecutableElement executableElement, List<String> codeLines,
            List<? extends VariableElement> parameters, String returnType, TypeMirror entityType) {
        String session = parameters.get(0).getSimpleName().toString();
        Query queryAnn = executableElement.getAnnotation(Query.class);
        String queryStr = queryAnn.value();
        boolean isNative = queryAnn.isNative();

        boolean isModifying = executableElement.getAnnotation(Modifying.class) != null;

        vn.io.lcx.jpa.annotation.ResultSetMapping resultSetMappingAnn = executableElement
                .getAnnotation(vn.io.lcx.jpa.annotation.ResultSetMapping.class);
        String createQueryCall;
        if (resultSetMappingAnn != null) {
            if (isNative) {
                createQueryCall = session + ".createNativeQuery(\"" + queryStr.replace("\"", "\\\"")
                        + "\", new org.hibernate.reactive.common.ResultSetMapping<" + entityType + ">() {\n" +
                        "    @Override\n" +
                        "    public String getName() {\n" +
                        "        return \"" + resultSetMappingAnn.name() + "\";\n" +
                        "    }\n" +
                        "\n" +
                        "    @Override\n" +
                        "    public Class<" + entityType + "> getResultType() {\n" +
                        "        return " + entityType + ".class;\n" +
                        "    }\n" +
                        "})";
            } else {
                codeLines.add(
                        "return io.vertx.core.Future.failedFuture(new vn.io.lcx.jpa.exception.CodeGenError(\"Native query is required for ResultSetMapping\"));");
                return;
            }
        } else if (isModifying) {
            if (isNative) {
                createQueryCall = session + ".createNativeQuery(\"" + queryStr.replace("\"", "\\\"") + "\")";
            } else {
                createQueryCall = session + ".createMutationQuery(\"" + queryStr.replace("\"", "\\\"") + "\")";
            }
        } else {
            if (isNative) {
                String resultClass = "Object.class";
                if (returnType.contains("Integer")) {
                    resultClass = "Integer.class";
                } else if (returnType.contains(entityType.toString())) {
                    resultClass = entityType + ".class";
                }
                createQueryCall = session + ".createNativeQuery(\"" + queryStr.replace("\"", "\\\"") + "\", "
                        + resultClass
                        + ")";
            } else {
                // Use generic createQuery if possible, defaults to Object if not known entity
                // type return or similar
                // But we have entityTypeMirror
                // If return type is related to entityType, use it.
                if (returnType.contains(entityType.toString())) {
                    createQueryCall = session + ".createQuery(\"" + queryStr.replace("\"", "\\\"") + "\", " + entityType
                            + ".class)";
                } else {
                    createQueryCall = session + ".createQuery(\"" + queryStr.replace("\"", "\\\"") + "\")";
                }
            }
        }

        // Handle parameter binding
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("final var query = ").append(createQueryCall).append(";");
        // We need to match parameters?
        // Hibernate Reactive uses numeric or named types.
        // Assuming user uses ?1, ?2 etc or :name.
        // If they use typical JPA parameters, we can map method args to it.
        // For simplicity, let's map by position 1, 2, 3.. (excluding Session).

        // Actually the session is the first arg.
        // So arg 1 (index 1) maps to ?1 ?
        int paramIndex = 1;
        for (int i = 1; i < parameters.size(); i++) {
            // Skip Pageable from regular query binding
            if (parameters.get(i).asType().toString().contains("Pageable"))
                continue;

            String paramName = parameters.get(i).getSimpleName().toString();
            Param paramAnn = parameters.get(i).getAnnotation(Param.class);
            if (paramAnn != null) {
                queryBuilder.append("\n        query.setParameter(\"").append(paramAnn.value()).append("\", ")
                        .append(paramName).append(");");
            } else {
                queryBuilder.append("\n        query.setParameter(").append(paramIndex++).append(", ").append(paramName)
                        .append(");");
            }
        }

        codeLines.add(queryBuilder.toString());

        if (isModifying) {
            if (returnType.contains("Integer")) {
                codeLines.add(
                        "return io.vertx.core.Future.fromCompletionStage((java.util.concurrent.CompletionStage<Integer>) query.executeUpdate());");
            } else {
                codeLines.add(
                        "return io.vertx.core.Future.fromCompletionStage((java.util.concurrent.CompletionStage<Integer>) query.executeUpdate()).map(v -> null);");
            }
        } else {
            // Extract inner type for witness
            String futureInnerType = returnType;
            if (returnType.startsWith("io.vertx.core.Future<")) {
                futureInnerType = returnType.substring("io.vertx.core.Future<".length(), returnType.lastIndexOf(">"));
            }

            if (returnType.contains("Page")) {
                // Find Pageable parameter
                String pageableParam = null;
                for (VariableElement param : parameters) {
                    if (param.asType().toString().contains("Pageable")) {
                        pageableParam = param.getSimpleName().toString();
                        break;
                    }
                }

                if (pageableParam == null) {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "Method with Page return type requires a Pageable parameter");
                    codeLines.clear();
                    return;
                }

                codeLines.add("final var pageimpl = (vn.io.lcx.common.database.pageable.PageableImpl) " + pageableParam
                        + ";");
                // Main Query for List
                codeLines.add("java.util.List<" + entityType + "> queryResult = new java.util.ArrayList<>();");

                // We need to re-generate query builder for list specifically because we need to
                // set max/first result
                // The implicit 'query' variable from above is created before this block.
                // But we should use it.
                // However, we need to distinguish between list query and count query.

                // Actually the 'query' variable above is already created with createQueryCall.
                // We can use it for the list fetch.

                codeLines.add("return io.vertx.core.Future.fromCompletionStage(");
                codeLines.add(
                        "        (java.util.concurrent.CompletionStage<java.util.List<" + entityType + ">>) query");
                codeLines.add("                .setFirstResult(pageimpl.getOffset())");
                codeLines.add("                .setMaxResults(pageimpl.getPageSize())");
                codeLines.add("                .getResultList()");
                codeLines.add(").map(rs -> {");
                codeLines.add("    queryResult.addAll(rs);");
                codeLines.add("    return vn.io.lcx.common.constant.CommonConstant.VOID;");
                codeLines.add("})");
                codeLines.add(".compose(v -> {");
                // Count Query
                // Use regex to replace 'select ... from' with 'select count(*) from'
                // Default to prepending 'select count(*) ' if no 'select ... from' structure
                // found
                // We use codeLines.add to generate the regex replacement at runtime? No, we do
                // it at compile time if possible.
                // But the query is a string literal in the annotation. We have it in
                // 'queryStr'.

                String countQueryStr;
                String trimmedQuery = queryStr.trim();
                if (trimmedQuery.toLowerCase().startsWith("select")) {
                    // Replace the first select ... from with select count(*) from
                    // Regex explanation:
                    // (?i) - case insensitive
                    // ^select\s+ - starts with select and whitespace
                    // .*? - lazy match content
                    // \s+from\s+ - match from surrounded by whitespace
                    // We want to replace "select [projection] from" with "select count(*) from"
                    // But we need to keep the rest of the query.
                    // So we replace the match.
                    countQueryStr = trimmedQuery.replaceFirst("(?i)^select\\s+.*?\\s+from\\s+",
                            "select count(*) from ");
                } else {
                    countQueryStr = "select count(*) " + queryStr;
                }

                String createCountQueryCall;
                if (isNative) {
                    createCountQueryCall = session + ".createNativeQuery(\"" + countQueryStr.replace("\"", "\\\"")
                            + "\", Long.class)";
                } else {
                    createCountQueryCall = session + ".createQuery(\"" + countQueryStr.replace("\"", "\\\"")
                            + "\", Long.class)";
                }

                codeLines.add("    final var countQuery = " + createCountQueryCall + ";");
                // Bind params for count query too!
                // Reuse the binding logic?
                // We need to bind specific parameters again to countQuery.

                int pIndex = 1;
                for (int i = 1; i < parameters.size(); i++) {
                    // Skip Pageable for binding
                    if (parameters.get(i).asType().toString().contains("Pageable"))
                        continue;

                    String pName = parameters.get(i).getSimpleName().toString();
                    Param pAnn = parameters.get(i).getAnnotation(Param.class);
                    if (pAnn != null) {
                        codeLines.add("    countQuery.setParameter(\"" + pAnn.value() + "\", " + pName + ");");
                    } else {
                        codeLines.add("    countQuery.setParameter(" + pIndex++ + ", " + pName + ");");
                    }
                }

                codeLines.add("    return io.vertx.core.Future.fromCompletionStage(");
                codeLines.add("            (java.util.concurrent.CompletionStage<Long>) countQuery.getSingleResult()");
                codeLines.add("    );");
                codeLines.add("})");
                codeLines.add(".map(totalItems ->");
                codeLines.add("    vn.io.lcx.common.database.pageable.Page.<" + entityType + ">create(");
                codeLines.add("            queryResult,");
                codeLines.add("            totalItems,");
                codeLines.add("            pageimpl.getPageNumber(),");
                codeLines.add("            pageimpl.getPageSize()");
                codeLines.add("    )");
                codeLines.add(");");

            } else if (returnType.contains("java.util.List")) {
                codeLines.add(
                        "return io.vertx.core.Future.fromCompletionStage((java.util.concurrent.CompletionStage<"
                                + futureInnerType
                                + ">) query.getResultList());");
            } else if (returnType.contains("java.util.Optional")) {
                // Optional handling is tricky because getSingleResultOrNull returns T, but
                // Future is Optional<T>
                // We need witness for T
                String optionalInner = futureInnerType;
                if (futureInnerType.startsWith("java.util.Optional<")) {
                    optionalInner = futureInnerType.substring("java.util.Optional<".length(),
                            futureInnerType.lastIndexOf(">"));
                }
                codeLines.add(
                        "return io.vertx.core.Future.fromCompletionStage((java.util.concurrent.CompletionStage<"
                                + optionalInner
                                + ">) query.getSingleResultOrNull()).map(java.util.Optional::ofNullable);");
            } else {
                codeLines.add("return io.vertx.core.Future.fromCompletionStage((java.util.concurrent.CompletionStage<"
                        + futureInnerType
                        + ">) query.getSingleResult());");
            }
        }
    }
}
