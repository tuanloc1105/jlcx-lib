package vn.com.lcx.jpa.processor;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Data
public class TypeHierarchyAnalyzer {

    private final Elements elementUtils;
    private final Types typeUtils;

    public Optional<TypeElement> getExtendedClass(TypeElement typeElement) {
        TypeMirror superclassMirror = typeElement.getSuperclass();
        if (superclassMirror != null && superclassMirror.getKind() != javax.lang.model.type.TypeKind.NONE) {
            return Optional.of((TypeElement) typeUtils.asElement(superclassMirror));
        }
        return Optional.empty();
    }

    public List<TypeElement> getImplementedInterfaces(TypeElement typeElement) {
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

    public List<TypeMirror> getGenericTypeOfExtendingInterface(TypeElement typeElement, String fullExtendingInterfaceClassName) {
        TypeElement extendingInterfaceElement = elementUtils.getTypeElement("vn.com.lcx.jpa.respository.JpaRepository");
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

}
