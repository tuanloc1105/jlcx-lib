package vn.com.lcx.processor.utility;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public class MethodInfo {
    private String methodName;
    private List<? extends VariableElement> inputParameters;
    private TypeMirror outputParameter;

    public MethodInfo() {
    }

    public MethodInfo(String methodName, List<? extends VariableElement> inputParameters, TypeMirror outputParameter) {
        this.methodName = methodName;
        this.inputParameters = inputParameters;
        this.outputParameter = outputParameter;
    }

    public static MethodInfoBuilder builder() {
        return new MethodInfoBuilder();
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

    public static class MethodInfoBuilder {
        private String methodName;
        private List<? extends VariableElement> inputParameters;
        private TypeMirror outputParameter;

        public MethodInfoBuilder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public MethodInfoBuilder inputParameters(List<? extends VariableElement> inputParameters) {
            this.inputParameters = inputParameters;
            return this;
        }

        public MethodInfoBuilder outputParameter(TypeMirror outputParameter) {
            this.outputParameter = outputParameter;
            return this;
        }

        public MethodInfo build() {
            return new MethodInfo(methodName, inputParameters, outputParameter);
        }
    }
}
