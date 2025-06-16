package vn.com.lcx.reactive.processor;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.ExceptionUtils;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.jpa.processor.utility.ProcessorClassInfo;
import vn.com.lcx.jpa.processor.utility.TypeHierarchyAnalyzer;
import vn.com.lcx.reactive.annotation.RRepository;
import vn.com.lcx.reactive.repository.ReactiveRepository;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void generateCode(ProcessorClassInfo processorClassInfo) {
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
                    final var codeLines = new ArrayList<String>();
                    if (!methodInfo.getInputParameters().get(0).asType().toString().equals("io.vertx.sqlclient.SqlConnection")) {
                        codeLines.add("throw new vn.com.lcx.jpa.exception.CodeGenError(\"First parameter must be a `io.vertx.sqlclient.SqlConnection`\");");
                    } else {
                        final VariableElement sqlConnectionVariable = methodInfo.getInputParameters().get(0);
                        final List<VariableElement> actualParameters = new ArrayList<>();
                        for (int i = 1; i < methodInfo.getInputParameters().size(); i++) {
                            actualParameters.add(methodInfo.getInputParameters().get(i));
                        }
                        codeLines.add(
                                String.format(
                                        "String databaseName = %s.databaseMetadata().productName();",
                                        sqlConnectionVariable.getSimpleName().toString()
                                )
                        );
                        if (methodInfo.getMethodName().equals("save")) {
                            codeLines.add(
                                    "io.vertx.core.Future<io.vertx.sqlclient.RowSet<io.vertx.sqlclient.Row>> future;"
                            );
                            codeLines.add(
                                    "if (databaseName.equals(\"PostgreSQL\")) {"
                            );
                            codeLines.add(
                                    String.format("    future %1$s.preparedQuery(%2$sUtils.reactiveInsertStatement(model) + \" returning \" + %2$sUtils.idColumnName())",
                                            sqlConnectionVariable.getSimpleName().toString(),
                                            entityTypeMirror.toString())
                            );
                            codeLines.add(
                                    String.format("            .execute(%sUtils.insertTupleParam(model));", entityTypeMirror)
                            );
                            codeLines.add(
                                    "} else if (databaseName.equals(\"MySQL\") || databaseName.equals(\"MariaDB\")) {"
                            );
                            codeLines.add(
                                    "} else if (databaseName.equals(\"Microsoft SQL Server\")) {"
                            );
                            codeLines.add(
                                    "} else if (databaseName.contains(\"Oracle\")) {"
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
                        }
                    }

                    methodCodeBody.append(
                            methodTemplate
                                    .replace(
                                            "${return-type}",
                                            methodInfo.getOutputParameter().toString()
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
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                vn.com.lcx.common.utils.DateTimeUtils.toUnixMil(vn.com.lcx.common.utils.DateTimeUtils.generateCurrentTimeDefault()) + ": \n" +
                        methodCodeBody
        );
    }

}
