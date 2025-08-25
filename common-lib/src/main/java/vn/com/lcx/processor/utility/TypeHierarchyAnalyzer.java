package vn.com.lcx.processor.utility;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for analyzing type hierarchies and related metadata
 * in the context of Java Annotation Processing.
 *
 * <p>This class provides helper methods for:
 * <ul>
 *   <li>Getting superclass and implemented interfaces of a given {@link TypeElement}.</li>
 *   <li>Extracting generic type arguments from extending interfaces.</li>
 *   <li>Collecting all declared fields in a class hierarchy.</li>
 *   <li>Checking whether a field belongs to an enum type.</li>
 *   <li>Walking through the full inheritance tree (all superclasses and interfaces).</li>
 * </ul>
 */
public final class TypeHierarchyAnalyzer {

    private TypeHierarchyAnalyzer() {
    }

    /**
     * Get the direct extended superclass of a given type element (if any).
     *
     * @param elementUtils Utility for operating on program elements
     * @param typeUtils    Utility for operating on types
     * @param typeElement  The type element to inspect
     * @return Optional containing the direct superclass as a {@link TypeElement}, or empty if none
     */
    public static Optional<TypeElement> getExtendedClass(final Elements elementUtils,
                                                         final Types typeUtils,
                                                         final TypeElement typeElement) {
        TypeMirror superclassMirror = typeElement.getSuperclass();
        if (superclassMirror != null && superclassMirror.getKind() != TypeKind.NONE) {
            return Optional.of((TypeElement) typeUtils.asElement(superclassMirror));
        }
        return Optional.empty();
    }

    /**
     * Get the directly implemented interfaces of a given type element.
     *
     * @param elementUtils Utility for operating on program elements
     * @param typeUtils    Utility for operating on types
     * @param typeElement  The type element to inspect
     * @return List of directly implemented interfaces as {@link TypeElement}
     */
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

    /**
     * Extracts the generic type arguments when a given type element extends or implements
     * a target interface.
     *
     * @param elementUtils                    Utility for operating on program elements
     * @param typeUtils                       Utility for operating on types
     * @param typeElement                     The type element to inspect
     * @param fullExtendingInterfaceClassName Fully qualified class name of the interface
     * @return List of {@link TypeMirror} representing the generic type arguments
     */
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
                Element element = ((DeclaredType) it).asElement();
                if (typeUtils.isAssignable(element.asType(), extendingInterfaceType)) {
                    List<? extends TypeMirror> typeArguments = ((DeclaredType) it).getTypeArguments();
                    result.addAll(typeArguments);
                }
            }
        }
        return result;
    }

    /**
     * Collects all fields (recursively) declared in the given type element and its superclasses.
     *
     * @param typeUtils   Utility for operating on types
     * @param typeElement The type element to inspect
     * @return A set of {@link Element} representing all fields in the hierarchy
     */
    public static HashSet<Element> getAllFields(final Types typeUtils,
                                                final TypeElement typeElement) {
        HashSet<Element> fields = new HashSet<>(ElementFilter.fieldsIn(typeElement.getEnclosedElements()));

        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass != null && !superclass.toString().equals(Object.class.getCanonicalName())) {
            Element superclassElement = typeUtils.asElement(superclass);
            if (superclassElement instanceof TypeElement) {
                fields.addAll(getAllFields(typeUtils, (TypeElement) superclassElement));
            }
        }
        return fields.stream()
                .filter(element -> element.getKind().isField())
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Get the {@link TypeElement} representation of a class from its fully qualified name.
     *
     * @param elementUtils Utility for operating on program elements
     * @param className    Fully qualified class name
     * @return The {@link TypeElement} corresponding to the given class name
     */
    public static TypeElement getTypeElementFromClassName(final Elements elementUtils, final String className) {
        return elementUtils.getTypeElement(className);
    }

    /**
     * Checks if a given field is of an enum type.
     *
     * @param field     The field element to inspect
     * @param typeUtils Utility for operating on types
     * @return true if the field type is an enum, false otherwise
     */
    public static boolean isEnumField(Element field, Types typeUtils) {
        if (field.getKind() != ElementKind.FIELD) {
            return false;
        }

        TypeMirror typeMirror = field.asType();
        Element typeElement = typeUtils.asElement(typeMirror);
        if (typeElement instanceof TypeElement) {
            return typeElement.getKind() == ElementKind.ENUM;
        }
        return false;
    }

    /**
     * Get all superclasses and interfaces (recursively) of a given {@link TypeElement}.
     * <p>
     * This method traverses the entire inheritance tree, collecting
     * all parent classes and interfaces (including indirect ones).
     *
     * @param typeUtils   Utility for operating on types
     * @param typeElement The type element to inspect
     * @return A set of fully qualified class/interface names in the hierarchy
     */
    public static Set<String> getAllSuperTypes(final Types typeUtils, final TypeElement typeElement) {
        Set<String> result = new HashSet<>();
        collectSuperTypes(typeUtils, typeElement.asType(), result);
        // remove itself
        result.remove(typeElement.getQualifiedName().toString());
        return result;
    }

    private static void collectSuperTypes(Types typeUtils, TypeMirror type, Set<String> result) {
        if (!(type instanceof DeclaredType)) {
            return;
        }
        DeclaredType declaredType = (DeclaredType) type;
        TypeElement element = (TypeElement) declaredType.asElement();

        result.add(element.getQualifiedName().toString());

        TypeMirror superclass = element.getSuperclass();
        if (superclass instanceof DeclaredType) {
            collectSuperTypes(typeUtils, superclass, result);
        }

        for (TypeMirror iface : element.getInterfaces()) {
            collectSuperTypes(typeUtils, iface, result);
        }
    }

}
