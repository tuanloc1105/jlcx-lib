package vn.com.lcx.processor;

import jakarta.persistence.Id;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.MyStringUtils;
import vn.com.lcx.jpa.annotation.Modifying;
import vn.com.lcx.jpa.annotation.Param;
import vn.com.lcx.jpa.annotation.Query;
import vn.com.lcx.jpa.annotation.Repository;
import vn.com.lcx.jpa.annotation.ResultSetMapping;
import vn.com.lcx.jpa.exception.CodeGenError;
import vn.com.lcx.jpa.respository.JpaRepository;
import vn.com.lcx.processor.utility.MethodInfo;
import vn.com.lcx.processor.utility.ProcessorClassInfo;
import vn.com.lcx.processor.utility.TypeHierarchyAnalyzer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("vn.com.lcx.jpa.annotation.Repository")
public class RepositoryProcessor extends AbstractProcessor {

    private static final List<String> SKIPABLE_METHOD_LIST = new ArrayList<>(
            List.of(
                    "save",
                    "update",
                    "delete",
                    "find",
                    "findOne",
                    "findById"
            )
    );

    // Template placeholders
    private static final String PH_RETURN_TYPE = "${return-type}";
    private static final String PH_METHOD_NAME = "${method-name}";
    private static final String PH_METHOD_BODY_1 = "${method-body-1}";
    private static final String PH_METHOD_BODY_2 = "${method-body-2}";
    private static final String PH_LIST_OF_PARAMS = "${list-of-parameters}";
    private static final String PH_ENTITY_CLASS = "${entity-class}";
    private static final String PH_ERROR_MESSAGE = "${error-message}";
    private static final String PH_PACKAGE_NAME = "${package-name}";
    private static final String PH_PROXY_CLASS_NAME = "${proxy-class-name}";
    private static final String PH_INTERFACE_CLASS_NAME = "${interface-class-name}";
    private static final String PH_PRIMARY_KEY_TYPE = "${primary-key-type}";
    private static final String PH_METHODS = "${methods}";

    // Type checking strings
    private static final String TYPE_JAVA_LIST = "java.util.List";
    private static final String TYPE_JAVA_OPTIONAL = "java.util.Optional";
    private static final String TYPE_PAGE = "vn.com.lcx.common.database.pageable.Page";
    // private static final String TYPE_PAGEABLE = "vn.com.lcx.common.database.pageable.Pageable";

    // Code snippets
    private static final String TIMING_START = "final double startingTime = (double) java.lang.System.currentTimeMillis();";
    private static final String TIMING_END_DURATION = "final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;";
    private static final String TIMING_END_LOG = "vn.com.lcx.common.utils.LogUtils.writeLog(this.getClass(), vn.com.lcx.common.utils.LogUtils.Level.TRACE, \"Executed SQL in {} ms\", duration);";

    private final static String NOT_IMPLEMENT_CODE_TEMPLATE = """
                public ${return-type} ${method-name}(${list-of-parameters}) {
                    throw new vn.com.lcx.jpa.exception.JpaMethodNotImplementException("${error-message}");
                }
            """;

    private static void generateCodeForPageable(MethodInfo methodInfo, ArrayList<String> codeLines, String sql, String outputClass) {
        VariableElement pageableParameter = methodInfo.getInputParameters().get(methodInfo.getInputParameters().size() - 1);
        codeLines.add(
                "StringBuilder orderStatement = new StringBuilder();"
        );
        codeLines.add(
                String.format(
                        "vn.com.lcx.common.database.pageable.PageableImpl pageimpl = (vn.com.lcx.common.database.pageable.PageableImpl) %s;",
                        pageableParameter.getSimpleName()
                )
        );
        codeLines.add("if (!pageimpl.getFieldNameAndDirectionMap().isEmpty()) {");
        codeLines.add("    orderStatement.append(\" order by \");");
        codeLines.add("}");
        codeLines.add("pageimpl.getFieldNameAndDirectionMap().forEach((field, direction) -> {");
        codeLines.add("    if (vn.com.lcx.common.database.pageable.Direction.DESC.equals(direction)) {");
        codeLines.add("        orderStatement.append(\" \").append(field).append(\" desc\");");
        codeLines.add("    } else {");
        codeLines.add("        orderStatement.append(\" \").append(field).append(\" asc\");");
        codeLines.add("    }");
        codeLines.add("});");
        codeLines.add(
                String.format(
                        "org.hibernate.query.Query<%2$s> query = currentSessionInContext.createQuery(\"%1$s\" + orderStatement.toString(), %2$s.class);",
                        sql,
                        outputClass
                )
        );
        codeLines.add("query.setFirstResult(pageimpl.getOffset());");
        codeLines.add("query.setMaxResults(pageimpl.getPageSize());");
    }

    private static void generateCodeForPageableNative(MethodInfo methodInfo, ArrayList<String> codeLines, String sql, String outputClass, String resultSetMappingName) {
        VariableElement pageableParameter = methodInfo.getInputParameters().get(methodInfo.getInputParameters().size() - 1);
        codeLines.add(
                String.format(
                        "vn.com.lcx.common.database.pageable.PageableImpl pageimpl = (vn.com.lcx.common.database.pageable.PageableImpl) %s;",
                        pageableParameter.getSimpleName()
                )
        );
        if (resultSetMappingName != null) {
            codeLines.add(
                    String.format(
                            "org.hibernate.query.Query<%2$s> query = currentSessionInContext.createNativeQuery(\"%1$s\", \"%3$s\", %2$s.class);",
                            sql,
                            outputClass,
                            resultSetMappingName
                    )
            );
        } else {
            codeLines.add(
                    String.format(
                            "org.hibernate.query.Query<%2$s> query = currentSessionInContext.createNativeQuery(\"%1$s\", %2$s.class);",
                            sql,
                            outputClass
                    )
            );
        }
        codeLines.add("query.setFirstResult(pageimpl.getOffset());");
        codeLines.add("query.setMaxResults(pageimpl.getPageSize());");
    }

    private static String replaceSelectToCount(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }

        String regex = "SELECT\\s+.*?\\s+FROM";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(query);

        if (matcher.find()) {
            int selectKeywordStart = query.toLowerCase().indexOf("select");
            String beforeSelect = query.substring(0, selectKeywordStart);

            String afterFrom = query.substring(matcher.end() - "FROM".length());

            return beforeSelect + "SELECT COUNT(1) " + afterFrom;
        } else {
            return "";
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Repository.class)) {
            if (annotatedElement instanceof TypeElement typeElement) {
                try {
                    boolean classIsNotInterface = !(typeElement.getKind() == ElementKind.INTERFACE);
                    if (classIsNotInterface) {
                        throw new IllegalArgumentException("Invalid class " + typeElement.getSimpleName() + ". Only apply for Interface");
                    }
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
                                    e.getMessage()
                            );
                }

            }
        }
        return true;
    }

    /**
     * Generate code for the given interface.
     *
     * @param processorClassInfo the processor class info
     * @throws Exception if any error occurs
     */
    public void generateCode(ProcessorClassInfo processorClassInfo) throws Exception {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                vn.com.lcx.common.utils.DateTimeUtils.toUnixMil(vn.com.lcx.common.utils.DateTimeUtils.generateCurrentTimeDefault()) + ": " +
                        String.format(
                                "Generating code for respository : %s",
                                processorClassInfo.getClazz().getQualifiedName()
                        )
        );
        // get generic type of interface
        List<TypeMirror> genericClasses =
                TypeHierarchyAnalyzer.getGenericTypeOfExtendingInterface(
                        processingEnv.getElementUtils(),
                        processingEnv.getTypeUtils(),
                        processorClassInfo.getClazz(),
                        JpaRepository.class.getName()
                );
        String template = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/repository-template.txt"
        );
        String jpaMethodTemplate = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/jpa-method-template.txt"
        );
        String jpaDoWorkMethodTemplate = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/jpa-do-work-method-template.txt"
        );
        String jpaCriteriaHandlerTemplate = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/jpa-criteria-handler-template.txt"
        );
        assert template != null;
        assert jpaMethodTemplate != null;
        assert jpaDoWorkMethodTemplate != null;
        assert jpaCriteriaHandlerTemplate != null;
        final var packageName = processingEnv
                .getElementUtils()
                .getPackageOf(processorClassInfo.getClazz())
                .getQualifiedName()
                .toString();
        final var className = processorClassInfo.getClazz().getSimpleName() + "Proxy";
        String fullClassName = packageName + "." + className;
        StringBuilder methodCodeBody = new StringBuilder();
        methodCodeBody.append(
                jpaCriteriaHandlerTemplate
                        .replace(
                                PH_ENTITY_CLASS,
                                genericClasses.get(0).toString()
                        ).replace(
                                PH_PRIMARY_KEY_TYPE,
                                genericClasses.get(1).toString()
                        )
        );
        final var idFieldName = findIdFieldNameOfEntity(
                TypeHierarchyAnalyzer
                        .getTypeElementFromClassName(
                                processingEnv.getElementUtils(),
                                genericClasses.get(0).toString()
                        )
        );
        String doWorkMethod = jpaDoWorkMethodTemplate
                .replace(
                        PH_RETURN_TYPE,
                        String.format(
                                "java.util.Optional<%s>",
                                genericClasses.get(0).toString()
                        )
                )
                .replace(
                        PH_METHOD_NAME,
                        "findById"
                )
                .replace(
                        PH_ENTITY_CLASS,
                        genericClasses.get(0).toString()
                )
                .replace(
                        PH_LIST_OF_PARAMS,
                        String.format(
                                "%s %s",
                                genericClasses.get(1).toString(),
                                "id"
                        )
                );
        String findByIdMethod = jpaMethodTemplate
                .replace(
                        PH_RETURN_TYPE,
                        String.format(
                                "java.util.Optional<%s>",
                                genericClasses.get(0).toString()
                        )
                )
                .replace(
                        PH_METHOD_NAME,
                        "findById"
                )
                .replace(
                        PH_ENTITY_CLASS,
                        genericClasses.get(0).toString()
                )
                .replace(
                        PH_LIST_OF_PARAMS,
                        String.format(
                                "%s %s",
                                genericClasses.get(1).toString(),
                                "id"
                        )
                );
        if (StringUtils.isBlank(idFieldName)) {
            findByIdMethod = findByIdMethod
                    .replace(
                            PH_METHOD_BODY_1,
                            "throw new vn.com.lcx.jpa.exception.JpaMethodNotImplementException(\"This method is not implemented\");"
                    )
                    .replace(
                            PH_METHOD_BODY_2,
                            "throw new vn.com.lcx.jpa.exception.JpaMethodNotImplementException(\"This method is not implemented\");"
                    );
        } else {
            final var codeLines = new ArrayList<String>();
            codeLines.add(
                    String.format(
                            "org.hibernate.query.Query<%1$s> query = currentSessionInContext.createQuery(\"FROM %1$s where %2$s = ?\", %1$s.class);",
                            genericClasses.get(0).toString(),
                            idFieldName
                    )
            );
            codeLines.add(
                    "return java.util.Optional.ofNullable(query.uniqueResult());"
            );
            findByIdMethod = findByIdMethod
                    .replace(
                            PH_METHOD_BODY_1,
                            String.join("\n            ", codeLines)
                    )
                    .replace(
                            PH_METHOD_BODY_2,
                            String.join("\n                ", codeLines)
                    );
        }
        methodCodeBody.
                append("\n").
                append(doWorkMethod).
                append("\n").
                append(findByIdMethod).
                append("\n");

        processorClassInfo.getMethods().forEach((method, executableElement) -> {
            if (!SKIPABLE_METHOD_LIST.contains(method.getMethodName())) {
                handleCustomMethod(
                        genericClasses.get(0),
                        methodCodeBody,
                        jpaMethodTemplate,
                        method,
                        executableElement
                );
            }
        });

        final var code = template
                .replace(
                        PH_PACKAGE_NAME,
                        packageName
                )
                .replace(
                        PH_PROXY_CLASS_NAME,
                        className
                )
                .replace(
                        PH_ENTITY_CLASS,
                        genericClasses.get(0).toString()
                )
                .replace(
                        PH_INTERFACE_CLASS_NAME,
                        processorClassInfo.getClazz().getQualifiedName()
                )
                .replace(
                        PH_PRIMARY_KEY_TYPE,
                        genericClasses.get(1).toString()
                )
                .replace(
                        PH_METHODS,
                        MyStringUtils.removeSuffixOfString(
                                methodCodeBody.toString(),
                                "\n"
                        )
                );
        JavaFileObject builderFile = this.processingEnv.getFiler().createSourceFile(fullClassName);
        try (Writer writer = builderFile.openWriter()) {
            writer.write(code);
        }
    }

    /**
     * This method takes a TypeElement as parameter and returns the name of the primary key field of that class.
     * If no primary key field is found, it returns an empty string.
     *
     * @param entity the TypeElement of the class you want to find the primary key field
     * @return the name of the primary key field as String, or an empty String if no primary key is found
     */
    public String findIdFieldNameOfEntity(TypeElement entity) {
        if (entity == null) {
            return CommonConstant.EMPTY_STRING;
        }
        final var fields = TypeHierarchyAnalyzer.getAllFields(processingEnv.getTypeUtils(), entity);
        if (fields.isEmpty()) {
            return CommonConstant.EMPTY_STRING;
        }
        var idFieldOptional = fields.stream().filter(field -> field.getAnnotation(Id.class) != null).findAny();
        if (idFieldOptional.isEmpty()) {
            return CommonConstant.EMPTY_STRING;
        }
        return idFieldOptional.get().getSimpleName().toString();
    }
    /**
     * Handle custom methods declared in the repository interface.
     * Delegates to appropriate handler based on method annotations.
     *
     * @param genericEntityClass the entity class type
     * @param methodCodeBody     the StringBuilder to append generated code
     * @param jpaMethodTemplate  the JPA method template
     * @param methodInfo         information about the method
     * @param executableElement  the executable element representing the method
     */
    public void handleCustomMethod(final TypeMirror genericEntityClass,
                                   final StringBuilder methodCodeBody,
                                   final String jpaMethodTemplate,
                                   final MethodInfo methodInfo,
                                   final ExecutableElement executableElement) {
        if (Optional.ofNullable(executableElement.getAnnotation(Modifying.class)).isPresent()) {
            handleModifyingMethod(
                    genericEntityClass,
                    methodCodeBody,
                    jpaMethodTemplate,
                    methodInfo,
                    executableElement
            );
            return;
        }
        if (Optional.ofNullable(executableElement.getAnnotation(Query.class)).isPresent()) {
            handleQueryMethod(
                    genericEntityClass,
                    methodCodeBody,
                    jpaMethodTemplate,
                    methodInfo,
                    executableElement
            );
        }
    }

    /**
     * Handle methods annotated with @Modifying.
     * Generates code for INSERT, UPDATE, DELETE operations.
     *
     * @param genericEntityClass the entity class type
     * @param methodCodeBody     the StringBuilder to append generated code
     * @param jpaMethodTemplate  the JPA method template
     * @param methodInfo         information about the method
     * @param executableElement  the executable element representing the method
     */
    public void handleModifyingMethod(final TypeMirror genericEntityClass,
                                      final StringBuilder methodCodeBody,
                                      final String jpaMethodTemplate,
                                      final MethodInfo methodInfo,
                                      final ExecutableElement executableElement) {
        final Query queryAnnotation = executableElement.getAnnotation(Query.class);
        if (queryAnnotation == null) {
            var code = buildBaseCode(
                    NOT_IMPLEMENT_CODE_TEMPLATE,
                    genericEntityClass,
                    methodInfo
            ).replace(PH_ERROR_MESSAGE, "A method annotated with `vn.com.lcx.jpa.annotation.Modifying` must be annotated with `vn.com.lcx.jpa.annotation.Query`");
            methodCodeBody.append(code).append("\n");
        } else {
            final var codeLines = new ArrayList<String>();
            final var codeLines2 = new ArrayList<String>();
            addTimingStart(codeLines);
            codeLines2.add("org.hibernate.Transaction transaction = currentSessionInContext.beginTransaction();");
            final var finalStatement = queryAnnotation.value().replace("\n", "\\n\" +\n                        \"");
            if (queryAnnotation.isNative()) {
                codeLines.add(
                        String.format(
                                "org.hibernate.query.MutationQuery query = currentSessionInContext.createNativeMutationQuery(\"%s\");",
                                finalStatement
                        )
                );
            } else {
                codeLines.add(
                        String.format(
                                "org.hibernate.query.MutationQuery query = currentSessionInContext.createMutationQuery(\"%s\");",
                                finalStatement
                        )
                );
            }
            setParameters(methodInfo, codeLines);
            codeLines2.addAll(codeLines);
            if (methodInfo.getOutputParameter().getKind().equals(TypeKind.VOID)) {
                codeLines.add("query.executeUpdate();");
                addTimingEnd(codeLines);
                codeLines2.add("query.executeUpdate();");
                addTimingEnd(codeLines2);
                codeLines2.add("transaction.commit();");
            } else {
                codeLines.add("final int rowAffected = query.executeUpdate();");
                addTimingEnd(codeLines);
                codeLines.add("return rowAffected;");
                codeLines2.add("final int rowAffected = query.executeUpdate();");
                addTimingEnd(codeLines2);
                codeLines2.add("transaction.commit();");
                codeLines2.add("return rowAffected;");
            }
            var code = buildBaseCode(
                    jpaMethodTemplate,
                    genericEntityClass,
                    methodInfo
            )
                    .replace(
                            PH_METHOD_BODY_1,
                            String.join("\n            ", codeLines)
                    )
                    .replace(
                            PH_METHOD_BODY_2,
                            String.join("\n                ", codeLines2)
                    );
            methodCodeBody.append(code).append("\n");
        }
    }

    /**
     * Handle methods annotated with @Query.
     * Generates code for SELECT operations (List, Page, Optional, single result).
     *
     * @param genericEntityClass the entity class type
     * @param methodCodeBody     the StringBuilder to append generated code
     * @param jpaMethodTemplate  the JPA method template
     * @param methodInfo         information about the method
     * @param executableElement  the executable element representing the method
     */
    public void handleQueryMethod(final TypeMirror genericEntityClass,
                                  final StringBuilder methodCodeBody,
                                  final String jpaMethodTemplate,
                                  final MethodInfo methodInfo,
                                  final ExecutableElement executableElement) {
        if (methodInfo.getOutputParameter().getKind().equals(TypeKind.VOID)) {
            throw new CodeGenError(
                    String.format(
                            "%s should not be a void method",
                            methodInfo.getMethodName()
                    )
            );
        }
        final String outputClass = getGenericTypeOfListOrPage(methodInfo.getOutputParameter().toString());
        final Query queryAnnotation = java.util.Objects.requireNonNull(
                executableElement.getAnnotation(Query.class),
                "Method must be annotated with @Query"
        );
        final var codeLines = new ArrayList<String>();
        final var queryStatement = queryAnnotation.value().replace("\n", "\\n\" +\n                        \"");
        addTimingStart(codeLines);
        if (queryAnnotation.isNative()) {
            if (lastParameterIsPageable(methodInfo)) {
                generateCodeForPageableNative(
                        methodInfo,
                        codeLines,
                        queryStatement,
                        outputClass,
                        Optional.ofNullable(executableElement.getAnnotation(ResultSetMapping.class))
                                .map(ResultSetMapping::name)
                                .orElse(null)
                );
            } else {
                ResultSetMapping resultSetMappingAnnotation = executableElement.getAnnotation(ResultSetMapping.class);
                if (resultSetMappingAnnotation != null) {
                    codeLines.add(
                            String.format(
                                    "org.hibernate.query.Query<%2$s> query = currentSessionInContext.createNativeQuery(\"%1$s\", \"%3$s\", %2$s.class);",
                                    queryStatement,
                                    outputClass,
                                    resultSetMappingAnnotation.name()
                            )
                    );
                } else {
                    codeLines.add(
                            String.format(
                                    "org.hibernate.query.Query<%2$s> query = currentSessionInContext.createNativeQuery(\"%1$s\", %2$s.class);",
                                    queryStatement,
                                    outputClass
                            )
                    );
                }
            }
        } else {
            if (lastParameterIsPageable(methodInfo)) {
                generateCodeForPageable(methodInfo, codeLines, queryStatement, outputClass);
            } else {
                codeLines.add(
                        String.format(
                                "org.hibernate.query.Query<%2$s> query = currentSessionInContext.createQuery(\"%1$s\", %2$s.class);",
                                queryStatement,
                                outputClass
                        )
                );
            }
        }
        setParameters(methodInfo, codeLines);
        String returnType = methodInfo.getOutputParameter().toString();
        if (returnType.contains(TYPE_JAVA_LIST)) {
            codeLines.add("final java.util.List<" + outputClass + "> result = query.getResultList();");
            addTimingEnd(codeLines);
            codeLines.add("return result;");
        } else if (returnType.contains(TYPE_PAGE)) {
            final var countHql = replaceSelectToCount(queryAnnotation.value());
            final var finalCountHql = countHql.replace("\n", "\\n\" +\n                        \"");
            if (queryAnnotation.isNative()) {
                codeLines.add(
                        String.format(
                                "org.hibernate.query.Query<Long> countQuery = currentSessionInContext.createNativeQuery(\"%1$s\", Long.class);",
                                finalCountHql
                        )
                );
            } else {
                codeLines.add(
                        String.format(
                                "org.hibernate.query.Query<Long> countQuery = currentSessionInContext.createQuery(\"%1$s\", Long.class);",
                                finalCountHql
                        )
                );
            }
            setParametersForCount(methodInfo, codeLines);
            codeLines.add("long totalItems = countQuery.getSingleResult();");
            codeLines.add(
                    String.format(
                            "final java.util.List<%1$s> queryResult = query.getResultList();",
                            outputClass
                    )
            );
            addTimingEnd(codeLines);
            codeLines.add(
                    String.format(
                            "return vn.com.lcx.common.database.pageable.Page.<%s>create(",
                            outputClass
                    )
            );
            codeLines.add("        queryResult,");
            codeLines.add("        totalItems,");
            codeLines.add("        pageimpl.getPageNumber(),");
            codeLines.add("        pageimpl.getPageSize()");
            codeLines.add(");");
        } else if (returnType.contains(TYPE_JAVA_OPTIONAL)) {
            codeLines.add("final java.util.Optional<" + outputClass + "> optional = java.util.Optional.ofNullable(query.uniqueResult());");
            addTimingEnd(codeLines);
            codeLines.add("return optional;");
        } else {
            codeLines.add("final " + outputClass + " result = query.uniqueResult();");
            addTimingEnd(codeLines);
            codeLines.add("return result;");
        }
        var code = buildBaseCode(
                jpaMethodTemplate,
                genericEntityClass,
                methodInfo
        )
                .replace(
                        PH_METHOD_BODY_1,
                        String.join("\n            ", codeLines)
                )
                .replace(
                        PH_METHOD_BODY_2,
                        String.join("\n                ", codeLines)
                );
        methodCodeBody.append(code).append("\n");
    }

    /**
     * Add timing start code line.
     */
    private void addTimingStart(List<String> codeLines) {
        codeLines.add(TIMING_START);
    }

    /**
     * Add timing end code lines (duration calculation and logging).
     */
    private void addTimingEnd(List<String> codeLines) {
        codeLines.add(TIMING_END_DURATION);
        codeLines.add(TIMING_END_LOG);
    }

    /**
     * Build base code from template with common replacements.
     *
     * @param template           the template to use
     * @param genericEntityClass the entity class type
     * @param methodInfo         the method info
     * @return the processed template string
     */
    private String buildBaseCode(final String template,
                                 final TypeMirror genericEntityClass,
                                 final MethodInfo methodInfo) {
        return template
                .replace(PH_RETURN_TYPE, methodInfo.getOutputParameter().toString())
                .replace(PH_METHOD_NAME, methodInfo.getMethodName())
                .replace(PH_ENTITY_CLASS, genericEntityClass.toString())
                .replace(
                        PH_LIST_OF_PARAMS,
                        methodInfo.getInputParameters()
                                .stream()
                                .map(
                                        variableElement ->
                                                String.format(
                                                        "%s %s",
                                                        variableElement.asType(),
                                                        variableElement.getSimpleName()
                                                )
                                )
                                .collect(Collectors.joining(", "))
                );
    }

    private String getGenericTypeOfListOrPage(String listTypeName) {
        if (!listTypeName.startsWith(TYPE_JAVA_LIST) &&
                !listTypeName.startsWith(TYPE_PAGE) &&
                !listTypeName.startsWith(TYPE_JAVA_OPTIONAL)) {
            return listTypeName;
        }
        if ((listTypeName.startsWith(TYPE_JAVA_LIST) ||
                listTypeName.startsWith(TYPE_PAGE) ||
                listTypeName.startsWith(TYPE_JAVA_OPTIONAL)) &&
                !listTypeName.contains("<")) {
            throw new CodeGenError("Unknown type " + listTypeName);
        }
        return listTypeName
                .replace(TYPE_JAVA_LIST + "<", CommonConstant.EMPTY_STRING)
                .replace(TYPE_PAGE + "<", CommonConstant.EMPTY_STRING)
                .replace(TYPE_JAVA_OPTIONAL + "<", CommonConstant.EMPTY_STRING)
                .replace(">", CommonConstant.EMPTY_STRING);
    }

    private void setParameters(MethodInfo methodInfo, ArrayList<String> codeLines) {
        setParameters(methodInfo, codeLines, false);
    }

    private void setParametersForCount(MethodInfo methodInfo, ArrayList<String> codeLines) {
        setParameters(methodInfo, codeLines, true);
    }

    private void setParameters(MethodInfo methodInfo, ArrayList<String> codeLines, boolean isCountQuery) {
        for (int i = 0; i < methodInfo.getInputParameters().size(); i++) {
            VariableElement variableElement = methodInfo.getInputParameters().get(i);
            if (
                    isPageable(variableElement)
            ) {
                continue;
            }
            Param paramAnnotation = variableElement.getAnnotation(Param.class);
            if (paramAnnotation != null) {
                codeLines.add(
                        String.format(
                                isCountQuery ? "countQuery.setParameter(\"%1$s\", %2$s);" : "query.setParameter(\"%1$s\", %2$s);",
                                paramAnnotation.value(),
                                variableElement.getSimpleName()
                        )
                );
            } else {
                codeLines.add(
                        String.format(
                                isCountQuery ? "countQuery.setParameter(%1$s, %2$s);" : "query.setParameter(%1$s, %2$s);",
                                i + 1,
                                variableElement.getSimpleName()
                        )
                );
            }
        }
    }

    public boolean lastParameterIsPageable(MethodInfo methodInfo) {
        return !methodInfo.getInputParameters().isEmpty() && isPageable(methodInfo.getInputParameters().get(methodInfo.getInputParameters().size() - 1));
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
