package vn.com.lcx.jpa.processor.utility;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessorClassInfo {
    private final Types typeUtils;
    private final Elements elementUtils;
    private final HashSet<Element> fields;
    private final HashMap<MethodInfo, ExecutableElement> methods;
    private final TypeElement clazz;

    public ProcessorClassInfo(Types typeUtils, Elements elementUtils, HashSet<Element> fields, HashMap<MethodInfo, ExecutableElement> methods, TypeElement clazz) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.fields = fields;
        this.methods = methods;
        this.clazz = clazz;
    }

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
        return new ProcessorClassInfo(
                typeUtils,
                elementUtils,
                TypeHierarchyAnalyzer.getAllFields(typeUtils, clazz),
                allMethods,
                clazz
        );
    }

    public Types getTypeUtils() {
        return typeUtils;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public HashSet<Element> getFields() {
        return fields;
    }

    public HashMap<MethodInfo, ExecutableElement> getMethods() {
        return methods;
    }

    public TypeElement getClazz() {
        return clazz;
    }

}
