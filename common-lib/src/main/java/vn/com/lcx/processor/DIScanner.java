package vn.com.lcx.processor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Instance;
import vn.com.lcx.common.annotation.PostConstruct;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.processor.info.ClassInfo;
import vn.com.lcx.processor.info.ConstructorInfo;
import vn.com.lcx.processor.info.FieldInfo;
import vn.com.lcx.processor.info.MethodInfo;
import vn.com.lcx.processor.utility.TypeHierarchyAnalyzer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("*")
public class DIScanner extends AbstractProcessor {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<ClassInfo> classInfos = new ArrayList<>();
        for (Element element : roundEnv.getRootElements()) {
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;
                if (typeElement.getAnnotation(Component.class) != null) {
                    ClassInfo info = new ClassInfo();
                    info.setFullQualifiedClassName(typeElement.getQualifiedName().toString());
                    final var superSet = TypeHierarchyAnalyzer.getAllSuperTypes(processingEnv.getTypeUtils(), typeElement);
                    info.setSuperClassesFullName(new ArrayList<>(superSet));
                    final var fields = TypeHierarchyAnalyzer.getAllFields(processingEnv.getTypeUtils(), typeElement);
                    info.setFields(new ArrayList<>(
                            fields.stream()
                                    .map(e -> new FieldInfo(e.getSimpleName() + CommonConstant.EMPTY_STRING, e.asType().toString()))
                                    .collect(Collectors.toList())
                    ));
                    ExecutableElement constructor = ElementFilter.constructorsIn(typeElement.getEnclosedElements())
                            .stream().findFirst().orElse(null);
                    if (constructor != null) {
                        List<String> paramTypes = new ArrayList<>();
                        for (VariableElement p : constructor.getParameters()) {
                            paramTypes.add(p.asType().toString());
                        }
                        info.setConstructor(new ConstructorInfo(paramTypes));
                    }
                    final List<MethodInfo> createInstanceMethods = new ArrayList<>();
                    processingEnv.getElementUtils().getAllMembers(typeElement)
                            .forEach(
                                    e -> {
                                        if (e.getKind() != ElementKind.METHOD) {
                                            return;
                                        }
                                        ExecutableElement executableElement = (ExecutableElement) e;
                                        if (executableElement.getAnnotation(PostConstruct.class) != null) {
                                            MethodInfo methodInfo = new MethodInfo();
                                            methodInfo.setMethodName(executableElement.getSimpleName() + CommonConstant.EMPTY_STRING);
                                            methodInfo.setReturnDataType(executableElement.getReturnType() + CommonConstant.EMPTY_STRING);
                                            info.setPostConstruct(methodInfo);
                                        }
                                        if (executableElement.getAnnotation(Instance.class) != null) {
                                            MethodInfo methodInfo = new MethodInfo();
                                            methodInfo.setMethodName(executableElement.getSimpleName() + CommonConstant.EMPTY_STRING);
                                            methodInfo.setReturnDataType(executableElement.getReturnType() + CommonConstant.EMPTY_STRING);
                                            createInstanceMethods.add(methodInfo);
                                        }
                                    }
                            );
                    info.setCreateInstanceMethods(createInstanceMethods);
                    classInfos.add(info);
                }
            }
        }
        if (!classInfos.isEmpty()) {
            try {
                FileObject file = processingEnv.getFiler()
                        .createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/class-index-" + UUID.randomUUID() + ".json");
                try (Writer writer = file.openWriter()) {
                    gson.toJson(classInfos, writer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
