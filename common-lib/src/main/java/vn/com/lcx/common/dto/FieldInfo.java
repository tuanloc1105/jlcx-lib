package vn.com.lcx.common.dto;

public class FieldInfo {

    private String fieldName;
    private String fieldDataType;

    public FieldInfo() {
    }

    public FieldInfo(String fieldDataType, String fieldName) {
        this.fieldDataType = fieldDataType;
        this.fieldName = fieldName;
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
