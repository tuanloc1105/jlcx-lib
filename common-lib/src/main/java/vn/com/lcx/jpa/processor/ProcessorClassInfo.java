package vn.com.lcx.jpa.processor;

import lombok.Data;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProcessorClassInfo {
    private final Types typeUtils;
    private final Elements elementUtils;
    private final HashSet<Element> fields;
    private final HashMap<MethodInfo, ExecutableElement> methods;
    private final TypeElement clazz;

    public static ProcessorClassInfo init(TypeElement clazz, Types typeUtils, Elements elementUtils) {
        final HashMap<MethodInfo, ExecutableElement> allMethods = new HashMap<>();
        List<ExecutableElement> allMethodsOfClass = elementUtils.getAllMembers(clazz).stream()
                .filter(e -> {
                    boolean elementIsAMethod = e.getKind() == ElementKind.METHOD;
                    boolean isNotStaticAndFinal = !(e.getModifiers().contains(Modifier.FINAL) ||
                            e.getModifiers().contains(Modifier.STATIC));
                    boolean notHashCodeMethod = !"hashCode".equalsIgnoreCase(e.getSimpleName().toString());
                    boolean notEqualsMethod = !"equals".equalsIgnoreCase(e.getSimpleName().toString());
                    boolean notCloneMethod = !"clone".equalsIgnoreCase(e.getSimpleName().toString());
                    boolean notFinalizeMethod = !"finalize".equalsIgnoreCase(e.getSimpleName().toString());
                    boolean notToStringMethod = !"toString".equalsIgnoreCase(e.getSimpleName().toString());
                    return elementIsAMethod &&
                            isNotStaticAndFinal &&
                            notHashCodeMethod &&
                            notEqualsMethod &&
                            notCloneMethod &&
                            notFinalizeMethod &&
                            notToStringMethod;
                })
                .map(member -> (ExecutableElement) member).collect(Collectors.toList());
        allMethodsOfClass.forEach(method -> {
            MethodInfo methodInfo = MethodInfo.builder()
                    .methodName(method.getSimpleName().toString())
                    .inputParameters(method.getParameters())
                    .outputParameter(method.getReturnType())
                    .build();
            allMethods.put(methodInfo, method);
        });
        return new ProcessorClassInfo(typeUtils, elementUtils, getAllFields(typeUtils, clazz), allMethods, clazz);
    }

    private static HashSet<Element> getAllFields(Types typeUtils, TypeElement typeElement) {
        // Collect fields from the current class
        HashSet<Element> fields = new HashSet<>(ElementFilter.fieldsIn(typeElement.getEnclosedElements()));
        // Get the superclass and repeat the process
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass != null && !superclass.toString().equals(Object.class.getCanonicalName())) {
            Element superclassElement = typeUtils.asElement(superclass);
            if (superclassElement instanceof TypeElement) {
                fields.addAll(getAllFields(typeUtils, (TypeElement) superclassElement));
            }
        }
        return fields.stream()
                .filter(element -> {
                    boolean elementIsField = element.getKind().isField();
                    // boolean fieldIsNotFinalOrStatic = !(element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC));
                    return elementIsField;
                })
                .collect(Collectors.toCollection(HashSet::new));
    }

}
