package vn.com.lcx.jpa.processor;

import jakarta.persistence.Id;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.MyStringUtils;
import vn.com.lcx.jpa.annotation.Repository;
import vn.com.lcx.jpa.respository.JpaRepository;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("vn.com.lcx.jpa.annotation.Repository")
public class RepositoryProcessor extends AbstractProcessor {

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
        String methodTemplate = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/jpa-method-template.txt"
        );
        String jpaCriteriaHandlerTemplate = FileUtils.readResourceFileAsText(
                this.getClass().getClassLoader(),
                "template/jpa-criteria-handler-template.txt"
        );
        assert template != null;
        assert methodTemplate != null;
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
        String findByIdMethod = methodTemplate
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
        methodCodeBody.append("\n").append(findByIdMethod);
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

}
