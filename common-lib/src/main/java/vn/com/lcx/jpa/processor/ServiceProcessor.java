package vn.com.lcx.jpa.processor;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.MyStringUtils;
import vn.com.lcx.jpa.annotation.Service;
import vn.com.lcx.jpa.annotation.Transactional;
import vn.com.lcx.jpa.processor.utility.ProcessorClassInfo;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("vn.com.lcx.jpa.annotation.Service")
public class ServiceProcessor extends AbstractProcessor {

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
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Service.class)) {
            if (annotatedElement instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                try {
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

    private void generateCode(ProcessorClassInfo processorClassInfo) throws Exception {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                vn.com.lcx.common.utils.DateTimeUtils.toUnixMil(vn.com.lcx.common.utils.DateTimeUtils.generateCurrentTimeDefault()) + ": " +
                        String.format(
                                "Generating code for service : %s",
                                processorClassInfo.getClazz().getQualifiedName()
                        )
        );
        String template = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/service-template.txt"
        );
        String methodTemplate = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/method-template.txt"
        );
        if (StringUtils.isBlank(template) || StringUtils.isBlank(methodTemplate)) {
            return;
        }
        StringBuilder methodCodeBody = new StringBuilder();
        methodCodeBody.append("\n");
        processorClassInfo.getMethods().forEach((method, executableElement) -> {

            final var codeLines = new ArrayList<String>();

            codeLines.add(
                    "final boolean isRoot = vn.com.lcx.jpa.context.JpaContext.isJpaContextEmpty();"
            );
            codeLines.add(
                    "if (isRoot) {"
            );
            codeLines.add(
                    "    vn.com.lcx.jpa.context.JpaContext.put(vn.com.lcx.common.utils.RandomUtils.generateRandomString(6), vn.com.lcx.common.utils.RandomUtils.getRandomNumber(0, 100));"
            );
            codeLines.add(
                    "}"
            );
            Transactional transactionalAnnotation = executableElement.getAnnotation(Transactional.class);
            final Set<String> exceptionClassesToRollback = new HashSet<>();
            if (transactionalAnnotation != null) {
                codeLines.add(
                        "vn.com.lcx.jpa.context.JpaContext.setTransactionOpen(true);"
                );
                codeLines.add(
                        String.format(
                                "vn.com.lcx.jpa.context.JpaContext.setTransactionIsolation(%d);",
                                transactionalAnnotation.isolation()
                        )
                );
                codeLines.add(
                        String.format(
                                "vn.com.lcx.jpa.context.JpaContext.setTransactionMode(%d);",
                                transactionalAnnotation.mode()
                        )
                );
                for (AnnotationMirror annotationMirror : executableElement.getAnnotationMirrors()) {
                    if (annotationMirror.getAnnotationType().toString().equals("vn.com.lcx.jpa.annotation.Transactional")) {
                        exceptionClassesToRollback.addAll(getOnRollbackClassNamesFromAnnotationMirror(annotationMirror));
                    }
                }
            }
            if (!method.getOutputParameter().getKind().equals(TypeKind.VOID)) {
                codeLines.add(method.getOutputParameter() + " actualResult;");
            }
            codeLines.add("try {");
            codeLines.add(
                    String.format(
                            "    %1$sactual.%2$s(%3$s);",
                            method.getOutputParameter().getKind().equals(TypeKind.VOID) ?
                                    "" : "actualResult = ",
                            method.getMethodName(),
                            method.getInputParameters()
                                    .stream()
                                    .map(VariableElement::getSimpleName)
                                    .collect(Collectors.joining(", "))
                    )
            );
            codeLines.add(
                    "    if (isRoot) {"
            );
            codeLines.add(
                    "        vn.com.lcx.jpa.context.JpaContext.commit();"
            );
            codeLines.add(
                    "    }"
            );
            codeLines.add("}");
            exceptionClassesToRollback.forEach(
                    exceptionClass -> {
                        codeLines.add(
                                String.format(
                                        "catch (%s e) {",
                                        exceptionClass
                                )
                        );
                        codeLines.add("    if (isRoot) {");
                        codeLines.add("        vn.com.lcx.jpa.context.JpaContext.rollback();");
                        codeLines.add("    }");
                        if (!"vn.com.lcx.vertx.base.exception.InternalServiceException".equals(exceptionClass)) {
                            codeLines.add("    if (e instanceof java.lang.RuntimeException) {");
                            codeLines.add("        throw e;");
                            codeLines.add("    }");
                            codeLines.add("    if (e instanceof java.lang.Exception) {");
                            codeLines.add("        throw new java.lang.RuntimeException(e);");
                            codeLines.add("    }");
                        } else {
                            codeLines.add("    throw e;");
                        }
                        codeLines.add("}");
                    }
            );
            if (!exceptionClassesToRollback.contains("java.lang.Exception")) {
                codeLines.add("catch (Exception e) {");
                codeLines.add("    if (e instanceof vn.com.lcx.vertx.base.exception.InternalServiceException) {");
                codeLines.add("        throw e;");
                codeLines.add("    }");
                codeLines.add("    throw new java.lang.RuntimeException(e);");
                codeLines.add("}");
            }
            codeLines.add("finally {");
            codeLines.add(
                    "    if (isRoot) {"
            );
            codeLines.add(
                    "        vn.com.lcx.jpa.context.JpaContext.commit();"
            );
            codeLines.add(
                    "        vn.com.lcx.jpa.context.JpaContext.close();"
            );
            codeLines.add(
                    "        vn.com.lcx.jpa.context.JpaContext.clearAll();"
            );
            codeLines.add(
                    "    }"
            );
            codeLines.add("}");
            if (!method.getOutputParameter().getKind().equals(TypeKind.VOID)) {
                codeLines.add("return actualResult;");
            }

            methodCodeBody.append(
                    methodTemplate
                            .replace(
                                    "${return-type}",
                                    method.getOutputParameter().toString()
                            )
                            .replace(
                                    "${method-name}",
                                    method.getMethodName()
                            )
                            .replace(
                                    "${list-of-parameters}",
                                    method.getInputParameters()
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
        });
        final var packageName = processingEnv
                .getElementUtils()
                .getPackageOf(processorClassInfo.getClazz())
                .getQualifiedName()
                .toString();
        final var className = processorClassInfo.getClazz().getSimpleName() + "Proxy";
        String fullClassName = packageName + "." + className;
        final var code = template
                .replace(
                        "${package-name}",
                        packageName
                )
                .replace(
                        "${class-name}",
                        className
                )
                .replace(
                        "${actual-class-name}",
                        processorClassInfo.getClazz().getSimpleName()
                )
                .replace(
                        "${super-parameters}",
                        processorClassInfo.getFields()
                                .stream()
                                .filter(element -> !element.getModifiers().contains(Modifier.STATIC))
                                .map(it -> "null")
                                .collect(Collectors.joining(", "))
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

    private List<String> getOnRollbackClassNamesFromAnnotationMirror(AnnotationMirror transactionalAnnotationMirror) {
        List<String> onRollbackClassNames = new ArrayList<>();
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = transactionalAnnotationMirror.getElementValues();

        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
            ExecutableElement attribute = entry.getKey();
            AnnotationValue value = entry.getValue();
            if (attribute.getSimpleName().toString().equals("onRollback")) {
                if (value.getValue() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<? extends AnnotationValue> classValues = (List<? extends AnnotationValue>) value.getValue();
                    for (AnnotationValue classValue : classValues) {
                        if (classValue.getValue() instanceof TypeMirror) {
                            TypeMirror typeMirror = (TypeMirror) classValue.getValue();
                            onRollbackClassNames.add(typeMirror.toString());
                        }
                    }
                }
                break;
            }
        }
        return onRollbackClassNames;
    }

}
