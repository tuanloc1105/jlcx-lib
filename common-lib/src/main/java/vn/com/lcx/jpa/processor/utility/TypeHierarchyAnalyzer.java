package vn.com.lcx.jpa.processor.utility;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TypeHierarchyAnalyzer {

    private TypeHierarchyAnalyzer() {
    }

    public static Optional<TypeElement> getExtendedClass(final Elements elementUtils,
                                                         final Types typeUtils,
                                                         final TypeElement typeElement) {
        TypeMirror superclassMirror = typeElement.getSuperclass();
        if (superclassMirror != null && superclassMirror.getKind() != javax.lang.model.type.TypeKind.NONE) {
            return Optional.of((TypeElement) typeUtils.asElement(superclassMirror));
        }
        return Optional.empty();
    }

    public static List<TypeElement> getImplementedInterfaces(final Elements elementUtils,
                                                             final Types typeUtils,
                                                             final TypeElement typeElement) {
        List<? extends TypeMirror> interfaceMirrors = typeElement.getInterfaces();
        if (interfaceMirrors.isEmpty()) {
            return Collections.emptyList();
        }
        List<TypeElement> implementedInterfaces = new ArrayList<>();
        for (TypeMirror ifaceMirror : interfaceMirrors) {

            implementedInterfaces.add((TypeElement) typeUtils.asElement(ifaceMirror));
        }
        return implementedInterfaces;
    }

    public static List<TypeMirror> getGenericTypeOfExtendingInterface(final Elements elementUtils,
                                                                      final Types typeUtils,
                                                                      final TypeElement typeElement,
                                                                      final String fullExtendingInterfaceClassName) {
        TypeElement extendingInterfaceElement = elementUtils.getTypeElement(fullExtendingInterfaceClassName);
        TypeMirror extendingInterfaceType = extendingInterfaceElement.asType();
        TypeMirror mirror = typeElement.asType();
        List<? extends TypeMirror> mirrors = typeUtils.directSupertypes(mirror);

        var result = new ArrayList<TypeMirror>();

        for (TypeMirror it : mirrors) {
            if (it.getKind() == TypeKind.DECLARED) {
                // this element is super class's element, do anything in here
                Element element = ((DeclaredType) it).asElement();
                if (typeUtils.isAssignable(element.asType(), extendingInterfaceType)) {
                    List<? extends TypeMirror> typeArguments = ((DeclaredType) it).getTypeArguments();
                    result.addAll(typeArguments);
                }
            }
        }
        return result;
    }

    public static HashSet<Element> getAllFields(final Types typeUtils,
                                                final TypeElement typeElement) {
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
                    @SuppressWarnings("UnnecessaryLocalVariable")
                    boolean elementIsField = element.getKind().isField();
                    // boolean fieldIsNotFinalOrStatic = !(element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC));
                    return elementIsField;
                })
                .collect(Collectors.toCollection(HashSet::new));
    }

    public static TypeElement getTypeElementFromClassName(final Elements elementUtils, final String className) {
        return elementUtils.getTypeElement(className);
    }

}
