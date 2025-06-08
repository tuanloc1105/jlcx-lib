package vn.com.lcx.common.processor;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public class MethodInfo {
    private String methodName;
    private List<? extends VariableElement> inputParameters;
    private TypeMirror outputParameter;
    private String nativeQueryValue;
    private boolean isModifying;

    public MethodInfo(String methodName, List<? extends VariableElement> inputParameters, TypeMirror outputParameter, String nativeQueryValue, boolean isModifying) {
        this.methodName = methodName;
        this.inputParameters = inputParameters;
        this.outputParameter = outputParameter;
        this.nativeQueryValue = nativeQueryValue;
        this.isModifying = isModifying;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<? extends VariableElement> getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(List<? extends VariableElement> inputParameters) {
        this.inputParameters = inputParameters;
    }

    public TypeMirror getOutputParameter() {
        return outputParameter;
    }

    public void setOutputParameter(TypeMirror outputParameter) {
        this.outputParameter = outputParameter;
    }

    public String getNativeQueryValue() {
        return nativeQueryValue;
    }

    public void setNativeQueryValue(String nativeQueryValue) {
        this.nativeQueryValue = nativeQueryValue;
    }

    public boolean isModifying() {
        return isModifying;
    }

    public void setModifying(boolean modifying) {
        isModifying = modifying;
    }
}
