package vn.com.lcx.common.processor;

import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.Repository;
import vn.com.lcx.common.annotation.TableName;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.ExceptionUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("vn.com.lcx.common.annotation.Repository")
public class LCXRepositoryProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        final Map<String, HashSet<Element>> entitiesClassMap = new HashMap<>();

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(TableName.class)) {
            if (annotatedElement instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                try {
                    String className = typeElement.getSimpleName() + CommonConstant.EMPTY_STRING;
                    String packageName = this.processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
                    String fullClassName = packageName + "." + className;
                    HashSet<Element> fieldsOfClass = this.getAllFields(typeElement);
                    entitiesClassMap.put(fullClassName, fieldsOfClass);
                } catch (Exception e) {
                    System.out.println(ExceptionUtils.getStackTrace(e));
                    this.processingEnv.
                            getMessager().
                            printMessage(
                                    Diagnostic.Kind.ERROR,
                                    ExceptionUtils.getStackTrace(e)
                            );
                }

            }
        }

        if (entitiesClassMap.isEmpty()) {
            return true;
        }

        // System.out.println("Found " + entitiesClassMap.size() + " entity class");

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Repository.class)) {
            if (annotatedElement instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                try {
                    Repository myRepository = typeElement.getAnnotation(Repository.class);
                    this.implementRepositoryClass(typeElement, entitiesClassMap);
                } catch (Throwable e) {
                    System.out.println(ExceptionUtils.getStackTrace(e));
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

    private void implementRepositoryClass(TypeElement typeElement, Map<String, HashSet<Element>> entityClasses) throws IOException {
        String className = typeElement.getSimpleName() + "Implement";
        String packageName = this.processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
        String fullClassName = packageName + "." + className;
        JavaFileObject builderFile = this.processingEnv.getFiler().createSourceFile(fullClassName);

        if (!this.extendsInterface(typeElement, "vn.com.lcx.common.database.repository.LCXRepository")) {
            return;
        }

        SQLParser parser = new SQLParser(
                typeElement,
                entityClasses,
                this.processingEnv.getTypeUtils(),
                this.processingEnv.getElementUtils()
        );
        final String code1 = parser.parseRepositoryMethods() + "\n";
        final String code2 = parser.parseExecutionMethodCode();
        final String code3 = parser.generateBatchExecutionMethodCode();
        final String code4 = parser.parseQueryAnnotationMethod();
        final String code5 = parser.parseQueryAnnotationMethodWithModifying();
        final String code6 = parser.parseReturnIdSaveMethod();
        final String code7 = parser.parseSpecification();
        final String code8 = parser.parseSpecificationWithPagination();
        try (Writer writer = builderFile.openWriter()) {
            writer.write("package " + packageName + ";\n\n");
            writer.write("import java.math.BigDecimal;\n");
            writer.write("import java.sql.ResultSet;\n");
            writer.write("import java.sql.SQLException;\n");
            writer.write("import java.sql.Statement;\n");
            writer.write("import java.util.*;\n");
            writer.write("import java.util.stream.Collectors;\n\n");
            writer.write("public class " + className + " implements " + typeElement.getSimpleName() + " {\n\n");
            writer.write("    private vn.com.lcx.common.database.DatabaseExecutor executor;\n\n");
            writer.write("    private static " + className + " instance;\n\n");
            writer.write("    public " + className + "(vn.com.lcx.common.database.DatabaseExecutor executor) {\n");
            writer.write("        this.executor = executor;\n");
            writer.write("    }\n\n");
            writer.write("    public static " + className + " getInstance(vn.com.lcx.common.database.DatabaseExecutor executor) {\n");
            writer.write("        if (instance == null) {\n");
            writer.write("            synchronized (" + className + ".class) {\n");
            writer.write("                if (instance == null) {\n");
            writer.write("                    instance = new " + className + "(executor);\n");
            writer.write("                }\n");
            writer.write("            }\n");
            writer.write("        }\n");
            writer.write("        return instance;\n");
            writer.write("    }\n");
            writer.write("\n");
            writer.write(code1);
            writer.write(code2);
            writer.write(code3);
            writer.write(code4);
            writer.write(code5);
            writer.write(code6);
            writer.write(code7);
            writer.write(code8);
            writer.write("\n}\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashSet<Element> getAllFields(TypeElement typeElement) {
        // Collect fields from the current class
        HashSet<Element> fields = new HashSet<>(ElementFilter.fieldsIn(typeElement.getEnclosedElements()));
        // Get the superclass and repeat the process
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass != null && !superclass.toString().equals(Object.class.getCanonicalName())) {
            Element superclassElement = processingEnv.getTypeUtils().asElement(superclass);
            if (superclassElement instanceof TypeElement) {
                fields.addAll(getAllFields((TypeElement) superclassElement));
            }
        }
        return fields.stream()
                .filter(element -> {
                    boolean elementIsField = element.getKind().isField();
                    boolean fieldIsNotFinalOrStatic = !(element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC));
                    ColumnName columnName = element.getAnnotation(ColumnName.class);
                    final boolean isAnnotatedWithColumnNameAnnotation = columnName != null;
                    return elementIsField && fieldIsNotFinalOrStatic && isAnnotatedWithColumnNameAnnotation;
                })
                .collect(Collectors.toCollection(HashSet::new));
    }

    public boolean extendsInterface(TypeElement typeElement, String interfaceName) {
        boolean result = false;

        TypeElement interfaceElement = this.processingEnv.getElementUtils().getTypeElement(interfaceName);
        TypeMirror interfaceType = interfaceElement.asType();

        TypeMirror mirror = typeElement.asType();
        if (mirror == null) {
            return result;
        }
        List<? extends TypeMirror> mirrors = this.processingEnv.getTypeUtils().directSupertypes(mirror);
        if (mirrors == null || mirrors.isEmpty()) {
            return result;
        }
        for (TypeMirror it : mirrors) {
            if (it.getKind() == TypeKind.DECLARED) {
                // this element is super class's element, do anything in here
                Element element = ((DeclaredType) it).asElement();
                result = this.processingEnv.getTypeUtils().isAssignable(element.asType(), interfaceType);
            }
        }
        return result;
    }

}
