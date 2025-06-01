package vn.com.lcx.jpa.processor;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.lang.model.element.TypeElement;
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

}
