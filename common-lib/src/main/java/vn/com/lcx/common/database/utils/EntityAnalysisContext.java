package vn.com.lcx.common.database.utils;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.IdColumn;
import vn.com.lcx.common.annotation.TableName;
import vn.com.lcx.common.constant.JavaSqlResultSetConstant;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static vn.com.lcx.common.database.utils.DBEntityAnalysis.MSSQL_DATATYPE_MAP;
import static vn.com.lcx.common.database.utils.DBEntityAnalysis.MYSQL_DATATYPE_MAP;
import static vn.com.lcx.common.database.utils.DBEntityAnalysis.ORACLE_DATATYPE_MAP;
import static vn.com.lcx.common.database.utils.DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP;

/**
 * Context class to hold analysis data for entity processing
 */
public class EntityAnalysisContext {
    private final Class<?> entityClass;
    private final String databaseType;
    private final String folderPath;
    private final TableName tableNameAnnotation;
    private final String finalTableName;
    private final List<Field> entityFields;
    private final Field idField;
    private final Map<String, String> databaseDatatypeMap;
    private final List<List<String>> columnDefinitionLines;
    private final List<String> createIndexList;
    private final List<String> dropIndexList;
    private final List<String> alterAddColumnList;
    private final List<String> alterDropColumnList;
    private final List<String> alterModifyColumnList;
    private final List<String> renameColumnList;
    private final List<String> addForeignKeyList;
    private String createSequenceStatement;

    public EntityAnalysisContext(Class<?> entityClass, String databaseType, String folderPath) {
        this.entityClass = entityClass;
        this.databaseType = databaseType;
        this.folderPath = folderPath;
        
        // Validate entity class
        if (entityClass.isInterface()) {
            throw new IllegalArgumentException("Entity class cannot be an interface: " + entityClass.getName());
        }
        
        this.tableNameAnnotation = entityClass.getAnnotation(TableName.class);
        if (tableNameAnnotation == null) {
            throw new IllegalArgumentException("Entity class must have @TableName annotation: " + entityClass.getName());
        }
        
        this.finalTableName = buildFinalTableName();
        this.entityFields = getSortedEntityFields();
        this.idField = findIdField();
        this.databaseDatatypeMap = getDatabaseDatatypeMapFromDatabaseType();
        
        // Initialize collections
        this.columnDefinitionLines = new ArrayList<>();
        this.createIndexList = new ArrayList<>();
        this.dropIndexList = new ArrayList<>();
        this.alterAddColumnList = new ArrayList<>();
        this.alterDropColumnList = new ArrayList<>();
        this.alterModifyColumnList = new ArrayList<>();
        this.renameColumnList = new ArrayList<>();
        this.addForeignKeyList = new ArrayList<>();
        this.createSequenceStatement = "";
    }

    private String buildFinalTableName() {
        String tableNameValue = tableNameAnnotation.value();
        if (StringUtils.isNotBlank(tableNameAnnotation.schema())) {
            String schemaName = tableNameAnnotation.schema() + ".";
            if (tableNameValue.contains(".")) {
                String[] tableNameValueArray = tableNameValue.split(JavaSqlResultSetConstant.DOT);
                return schemaName + tableNameValueArray[tableNameValueArray.length - 1];
            } else {
                return schemaName + tableNameValue;
            }
        }
        return tableNameValue;
    }

    private List<Field> getSortedEntityFields() {
        List<Field> fields = new ArrayList<>(Arrays.asList(entityClass.getDeclaredFields()));
        fields.sort(Comparator.comparing(Field::getName));
        return fields;
    }

    private Field findIdField() {
        return entityFields.stream()
                .filter(field -> field.getAnnotation(IdColumn.class) != null)
                .findFirst()
                .orElse(null);
    }

    private Map<String, String> getDatabaseDatatypeMapFromDatabaseType() {
        switch (databaseType.toLowerCase()) {
            case "postgresql":
                return POSTGRESQL_DATATYPE_MAP;
            case "mysql":
                return MYSQL_DATATYPE_MAP;
            case "mssql":
                return MSSQL_DATATYPE_MAP;
            case "oracle":
            default:
                return ORACLE_DATATYPE_MAP;
        }
    }

    // Getters
    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public TableName getTableNameAnnotation() {
        return tableNameAnnotation;
    }

    public String getFinalTableName() {
        return finalTableName;
    }

    public List<Field> getEntityFields() {
        return entityFields;
    }

    public Field getIdField() {
        return idField;
    }

    public Map<String, String> getDatabaseDatatypeMap() {
        return databaseDatatypeMap;
    }

    public List<List<String>> getColumnDefinitionLines() {
        return columnDefinitionLines;
    }

    public List<String> getCreateIndexList() {
        return createIndexList;
    }

    public List<String> getDropIndexList() {
        return dropIndexList;
    }

    public List<String> getAlterAddColumnList() {
        return alterAddColumnList;
    }

    public List<String> getAlterDropColumnList() {
        return alterDropColumnList;
    }

    public List<String> getAlterModifyColumnList() {
        return alterModifyColumnList;
    }

    public List<String> getRenameColumnList() {
        return renameColumnList;
    }

    public List<String> getAddForeignKeyList() {
        return addForeignKeyList;
    }

    public String getCreateSequenceStatement() {
        return createSequenceStatement;
    }

    public void setCreateSequenceStatement(String createSequenceStatement) {
        this.createSequenceStatement = createSequenceStatement;
    }
} 
