package vn.com.lcx.processor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Instance;
import vn.com.lcx.common.annotation.PostConstruct;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.processor.info.ClassInfo;
import vn.com.lcx.processor.info.ConstructorInfo;
import vn.com.lcx.processor.info.FieldInfo;
import vn.com.lcx.processor.info.MethodInfo;
import vn.com.lcx.processor.utility.TypeHierarchyAnalyzer;
import vn.com.lcx.vertx.base.annotation.app.VertxApplication;

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
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
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
        var isMain = false;
        List<ClassInfo> classInfos = new ArrayList<>();
        for (Element element : roundEnv.getRootElements()) {
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;
                if (typeElement.getAnnotation(VertxApplication.class) != null) {
                    System.out.println("Found main class");
                    isMain = true;
                }
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
            if (isMain /*&& roundEnv.processingOver()*/) {
                try {
                    System.out.println("Merging index");
                    // Truy cập thư mục META-INF qua classloader
                    Enumeration<URL> resources =
                            getClass().getClassLoader().getResources("META-INF/");
                    while (resources.hasMoreElements()) {
                        URL url = resources.nextElement();
                        File dir = new File(url.toURI());
                        if (dir.isDirectory()) {
                            File[] files = dir.listFiles((d, name) -> name.startsWith("class-index-") && name.endsWith(".json"));
                            if (files != null) {
                                for (File f : files) {
                                    try (Reader r = new FileReader(f)) {
                                        Type listType = new TypeToken<List<ClassInfo>>() {
                                        }.getType();
                                        List<ClassInfo> list = gson.fromJson(r, listType);
                                        if (list != null) classInfos.addAll(list);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    FileObject mergedFile = processingEnv.getFiler()
                            .createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/class-index-merged.json");
                    try (Writer writer = mergedFile.openWriter()) {
                        gson.toJson(classInfos, writer);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
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
        }
        return false;
    }
}
