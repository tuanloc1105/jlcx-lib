package vn.io.lcx.common.database.utils;

/**
 * Data Transfer Object for Column Definition
 */
public class ColumnDefinition {
    private String columnName;
    private String dataType;
    private boolean isNullable;
    private String defaultValue;
    private boolean isUnique;

    public ColumnDefinition(String columnName, String dataType, boolean isNullable, String defaultValue,
            boolean isUnique) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.isNullable = isNullable;
        this.defaultValue = defaultValue;
        this.isUnique = isUnique;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public void setNullable(boolean nullable) {
        isNullable = nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }
}
