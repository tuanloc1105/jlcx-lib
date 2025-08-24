package vn.com.lcx.processor.info;

import java.util.List;

public class ConstructorInfo {

    private List<String> parametersDataTypes;

    public ConstructorInfo() {
    }

    public ConstructorInfo(List<String> parametersDataTypes) {
        this.parametersDataTypes = parametersDataTypes;
    }

    public List<String> getParametersDataTypes() {
        return parametersDataTypes;
    }

    public void setParametersDataTypes(List<String> parametersDataTypes) {
        this.parametersDataTypes = parametersDataTypes;
    }

}
