package vn.com.lcx.jpa.processor;

import jakarta.persistence.Id;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.MyStringUtils;
import vn.com.lcx.jpa.annotation.Modifying;
import vn.com.lcx.jpa.annotation.Param;
import vn.com.lcx.jpa.annotation.Query;
import vn.com.lcx.jpa.annotation.Repository;
import vn.com.lcx.jpa.exception.CodeGenError;
import vn.com.lcx.jpa.processor.utility.MethodInfo;
import vn.com.lcx.jpa.processor.utility.ProcessorClassInfo;
import vn.com.lcx.jpa.processor.utility.TypeHierarchyAnalyzer;
import vn.com.lcx.jpa.respository.JpaRepository;

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
import java.util.Arrays;
import java.util.Comparator;
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

    private static final List<String> WHERE_STATEMENT_DELIMITER_KEYWORDS = Arrays.asList(
            "NotEqual",
            "NotIn",
            "NotLike",
            "NotBetween",
            "NotNull",
            "Not",
            "Equal",
            "In",
            "Like",
            "Between",
            "Null",
            "LessThan",
            "GreaterThan",
            "LessEqual",
            "GreaterEqual"
    );

    private static String NOT_IMPLEMENT_CODE_TEMPLATE = "    public ${return-type} ${method-name}(${list-of-parameters}) {\n" +
            "        throw new vn.com.lcx.jpa.exception.JpaMethodNotImplementException(\"${error-message}\");\n" +
            "    }\n";

    private static final Pattern WHERE_STATEMENT_SPLIT_PATTERN;

    static {
        // Sort by keyword's length to avoid mismatching
        WHERE_STATEMENT_DELIMITER_KEYWORDS.sort(Comparator.comparingInt(String::length).reversed());
        String joinedKeywords = String.join("|", WHERE_STATEMENT_DELIMITER_KEYWORDS);
        WHERE_STATEMENT_SPLIT_PATTERN = Pattern.compile(joinedKeywords);
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
            if (annotatedElement instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) annotatedElement;
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
                } catch (Throwable e) {
                    this.processingEnv.
                            getMessager().
                            printMessage(
                                    Diagnostic.Kind.ERROR,
                                    e.getMessage()
                                    // ExceptionUtils.getStackTrace(e)
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
                                "${entity-class}",
                                genericClasses.get(0).toString()
                        ).replace(
                                "${primary-key-type}",
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
                        "${return-type}",
                        String.format(
                                "java.util.Optional<%s>",
                                genericClasses.get(0).toString()
                        )
                )
                .replace(
                        "${method-name}",
                        "findById"
                )
                .replace(
                        "${entity-class}",
                        genericClasses.get(0).toString()
                )
                .replace(
                        "${list-of-parameters}",
                        String.format(
                                "%s %s",
                                genericClasses.get(1).toString(),
                                "id"
                        )
                );
        String findByIdMethod = jpaMethodTemplate
                .replace(
                        "${return-type}",
                        String.format(
                                "java.util.Optional<%s>",
                                genericClasses.get(0).toString()
                        )
                )
                .replace(
                        "${method-name}",
                        "findById"
                )
                .replace(
                        "${entity-class}",
                        genericClasses.get(0).toString()
                )
                .replace(
                        "${list-of-parameters}",
                        String.format(
                                "%s %s",
                                genericClasses.get(1).toString(),
                                "id"
                        )
                );
        if (StringUtils.isBlank(idFieldName)) {
            findByIdMethod = findByIdMethod
                    .replace(
                            "${method-body-1}",
                            "throw new vn.com.lcx.jpa.exception.JpaMethodNotImplementException(\"This method is not implemented\");"
                    )
                    .replace(
                            "${method-body-2}",
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
                            "${method-body-1}",
                            String.join("\n            ", codeLines)
                    )
                    .replace(
                            "${method-body-2}",
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
            if (SKIPABLE_METHOD_LIST.contains(method.getMethodName())) {
                return;
            } else {
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
                        "${package-name}",
                        packageName
                )
                .replace(
                        "${proxy-class-name}",
                        className
                )
                .replace(
                        "${entity-class}",
                        genericClasses.get(0).toString()
                )
                .replace(
                        "${interface-class-name}",
                        processorClassInfo.getClazz().getQualifiedName()
                )
                .replace(
                        "${primary-key-type}",
                        genericClasses.get(1).toString()
                )
                .replace(
                        "${methods}",
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
            return;
        }
    }

    public void handleModifyingMethod(final TypeMirror genericEntityClass,
                                      final StringBuilder methodCodeBody,
                                      final String jpaMethodTemplate,
                                      final MethodInfo methodInfo,
                                      final ExecutableElement executableElement) {
        if (Optional.ofNullable(executableElement.getAnnotation(Query.class)).isEmpty()) {
            var code = buildBaseCode(
                    NOT_IMPLEMENT_CODE_TEMPLATE,
                    genericEntityClass,
                    methodCodeBody,
                    jpaMethodTemplate,
                    methodInfo,
                    executableElement
            ).replace("${error-message}", "A method annotated with `vn.com.lcx.jpa.annotation.Modifying` must be annotated with `vn.com.lcx.jpa.annotation.Query`");
            methodCodeBody.append(code).append("\n");
        } else {
            final var queryAnnotation = executableElement.getAnnotation(Query.class);
            final var codeLines = new ArrayList<String>();
            if (queryAnnotation.isNative()) {
                codeLines.add(
                        String.format(
                                "org.hibernate.query.MutationQuery query = currentSessionInContext.createNativeMutationQuery(\"%s\");",
                                queryAnnotation.value()
                        )
                );
            } else {
                codeLines.add(
                        String.format(
                                "org.hibernate.query.MutationQuery query = currentSessionInContext.createMutationQuery(\"%s\");",
                                queryAnnotation.value()
                        )
                );
            }
            setParameters(methodInfo, codeLines);
            if (methodInfo.getOutputParameter().getKind().equals(TypeKind.VOID)) {
                codeLines.add(
                        "query.executeUpdate();"
                );
            } else {
                codeLines.add(
                        "return query.executeUpdate();"
                );
            }
            var code = buildBaseCode(
                    jpaMethodTemplate,
                    genericEntityClass,
                    methodCodeBody,
                    jpaMethodTemplate,
                    methodInfo,
                    executableElement
            )
                    .replace(
                            "${method-body-1}",
                            String.join("\n            ", codeLines)
                    )
                    .replace(
                            "${method-body-2}",
                            String.join("\n                ", codeLines)
                    );
            methodCodeBody.append(code).append("\n");
        }
    }

    public void handleQueryMethod(final TypeMirror genericEntityClass,
                                  final StringBuilder methodCodeBody,
                                  final String jpaMethodTemplate,
                                  final MethodInfo methodInfo,
                                  final ExecutableElement executableElement) {
        if (methodInfo.getOutputParameter().getKind().equals(TypeKind.VOID)) {
            // var code = buildBaseCode(
            //         NOT_IMPLEMENT_CODE_TEMPLATE,
            //         genericEntityClass,
            //         methodCodeBody,
            //         jpaMethodTemplate,
            //         methodInfo,
            //         executableElement
            // ).replace("${error-message}", "Should not be a void method");
            // methodCodeBody.append(code).append("\n");
            // return;
            throw new CodeGenError(
                    String.format(
                            "%s should not be a void method",
                            methodInfo.getMethodName()
                    )
            );
        }
        final String outputClass = getGenericTypeOfListOrPage(methodInfo.getOutputParameter().toString());
        final var queryAnnotation = executableElement.getAnnotation(Query.class);
        final var codeLines = new ArrayList<String>();
        if (queryAnnotation.isNative()) {
            codeLines.add(
                    String.format(
                            "org.hibernate.query.Query<%2$s> query = currentSessionInContext.createNativeQuery(\"%1$s\", %2$s.class);",
                            queryAnnotation.value(),
                            outputClass
                    )
            );
        } else {
            if (lastParameterIsPageable(methodInfo)) {
                // processingEnv.getMessager().printMessage(
                //         Diagnostic.Kind.NOTE,
                //         vn.com.lcx.common.utils.DateTimeUtils.toUnixMil(vn.com.lcx.common.utils.DateTimeUtils.generateCurrentTimeDefault()) + ": " +
                //                 String.format(
                //                         "StringBuilder hsqlQuery = new StringBuilder(\"%s\");",
                //                         queryAnnotation.value()
                //                 )
                // );
                generateCodeForPageable(methodInfo, codeLines, queryAnnotation.value(), outputClass);
            } else {
                codeLines.add(
                        String.format(
                                "org.hibernate.query.Query<%2$s> query = currentSessionInContext.createQuery(\"%1$s\", %2$s.class);",
                                queryAnnotation.value(),
                                outputClass
                        )
                );
            }
        }
        setParameters(methodInfo, codeLines);
        if (methodInfo.getOutputParameter().toString().contains("java.util.List")) {
            codeLines.add(
                    "return query.getResultList();"
            );
        } else if (methodInfo.getOutputParameter().toString().contains("vn.com.lcx.common.database.pageable.Page")) {
            final var countHql = replaceSelectToCount(queryAnnotation.value());
            codeLines.add(
                    String.format(
                            "org.hibernate.query.Query<Long> countQuery = currentSessionInContext.createQuery(\"%1$s\", Long.class);",
                            countHql
                    )
            );
            setParametersForCount(methodInfo, codeLines);
            codeLines.add(
                    "long totalItems = countQuery.getSingleResult();"
            );
            codeLines.add(
                    String.format(
                            "final java.util.List<%1$s> queryResult = query.getResultList();",
                            outputClass
                    )
            );
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
        } else if (methodInfo.getOutputParameter().toString().contains("java.util.Optional")) {
            codeLines.add(
                    "return java.util.Optional.ofNullable(query.uniqueResult());"
            );
        } else {
            codeLines.add(
                    "return query.uniqueResult();"
            );
        }
        var code = buildBaseCode(
                jpaMethodTemplate,
                genericEntityClass,
                methodCodeBody,
                jpaMethodTemplate,
                methodInfo,
                executableElement
        )
                .replace(
                        "${method-body-1}",
                        String.join("\n            ", codeLines)
                )
                .replace(
                        "${method-body-2}",
                        String.join("\n                ", codeLines)
                );
        methodCodeBody.append(code).append("\n");
    }

    // TODO: implement a generate code technique for generating query from method name, base on `vn.com.lcx.common.database.reflect.SelectStatementBuilder`

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

    private String buildBaseCode(final String template,
                                 final TypeMirror genericEntityClass,
                                 final StringBuilder methodCodeBody,
                                 final String jpaMethodTemplate,
                                 final MethodInfo methodInfo,
                                 final ExecutableElement executableElement) {
        return template
                .replace("${return-type}", methodInfo.getOutputParameter().toString())
                .replace("${method-name}", methodInfo.getMethodName())
                .replace("${entity-class}", genericEntityClass.toString())
                .replace(
                        "${list-of-parameters}",
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
        if (!listTypeName.startsWith("java.util.List") && !listTypeName.startsWith("vn.com.lcx.common.database.pageable.Page")) {
            return listTypeName;
        }
        if ((listTypeName.startsWith("java.util.List") || listTypeName.startsWith("vn.com.lcx.common.database.pageable.Page")) && !listTypeName.contains("<")) {
            throw new CodeGenError("Unknow type " + listTypeName);
        }
        return listTypeName
                .replace("java.util.List<", CommonConstant.EMPTY_STRING)
                .replace("vn.com.lcx.common.database.pageable.Page<", CommonConstant.EMPTY_STRING)
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
            if (Optional.ofNullable(variableElement.getAnnotation(Param.class)).isPresent()) {
                codeLines.add(
                        String.format(
                                isCountQuery ? "countQuery.setParameter(\"%1$s\", %2$s);" : "query.setParameter(\"%1$s\", %2$s);",
                                variableElement.getAnnotation(Param.class).value(),
                                variableElement.getSimpleName()
                        )
                );
            } else {
                codeLines.add(
                        String.format(
                                isCountQuery ? "countQuery.setParameter(\"%1$s\", %2$s);" : "query.setParameter(%1$s, %2$s);",
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

}
