package vn.com.lcx.processor.info;

public class MethodInfo {

    private String methodName;
    private String returnDataType;

    public MethodInfo() {
    }

    public MethodInfo(String methodName, String returnDataType) {
        this.methodName = methodName;
        this.returnDataType = returnDataType;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getReturnDataType() {
        return returnDataType;
    }

    public void setReturnDataType(String returnDataType) {
        this.returnDataType = returnDataType;
    }

}
