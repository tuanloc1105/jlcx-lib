package vn.com.lcx.processor.info;

import java.util.List;

public class ClassInfo {

    private String fullQualifiedClassName;
    private List<String> superClassesFullName;
    private List<FieldInfo> fields;
    private ConstructorInfo constructor;
    private MethodInfo postConstruct;
    private List<MethodInfo> createInstanceMethods;

    public ClassInfo() {
    }

    public ClassInfo(String fullQualifiedClassName,
                     List<String> superClassesFullName,
                     List<FieldInfo> fields,
                     ConstructorInfo constructor,
                     MethodInfo postConstruct,
                     List<MethodInfo> createInstanceMethods) {
        this.fullQualifiedClassName = fullQualifiedClassName;
        this.superClassesFullName = superClassesFullName;
        this.fields = fields;
        this.constructor = constructor;
        this.postConstruct = postConstruct;
        this.createInstanceMethods = createInstanceMethods;
    }

    public String getFullQualifiedClassName() {
        return fullQualifiedClassName;
    }

    public void setFullQualifiedClassName(String fullQualifiedClassName) {
        this.fullQualifiedClassName = fullQualifiedClassName;
    }

    public List<String> getSuperClassesFullName() {
        return superClassesFullName;
    }

    public void setSuperClassesFullName(List<String> superClassesFullName) {
        this.superClassesFullName = superClassesFullName;
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public void setFields(List<FieldInfo> fields) {
        this.fields = fields;
    }

    public ConstructorInfo getConstructor() {
        return constructor;
    }

    public void setConstructor(ConstructorInfo constructor) {
        this.constructor = constructor;
    }

    public MethodInfo getPostConstruct() {
        return postConstruct;
    }

    public void setPostConstruct(MethodInfo postConstruct) {
        this.postConstruct = postConstruct;
    }

    public List<MethodInfo> getCreateInstanceMethods() {
        return createInstanceMethods;
    }

    public void setCreateInstanceMethods(List<MethodInfo> createInstanceMethods) {
        this.createInstanceMethods = createInstanceMethods;
    }

}
