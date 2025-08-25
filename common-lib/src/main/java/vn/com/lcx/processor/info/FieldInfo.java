package vn.com.lcx.processor.info;

public class FieldInfo {

    private String fieldName;
    private String fieldDataType;

    public FieldInfo() {
    }

    public FieldInfo(String fieldName, String fieldDataType) {
        this.fieldName = fieldName;
        this.fieldDataType = fieldDataType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldDataType() {
        return fieldDataType;
    }

    public void setFieldDataType(String fieldDataType) {
        this.fieldDataType = fieldDataType;
    }

}
