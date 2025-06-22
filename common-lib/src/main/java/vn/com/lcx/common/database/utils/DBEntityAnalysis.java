package vn.com.lcx.common.database.utils;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.IdColumn;
import vn.com.lcx.common.annotation.TableName;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.MyStringUtils;

import javax.lang.model.element.Element;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static vn.com.lcx.common.utils.FileUtils.createFolderIfNotExists;
import static vn.com.lcx.common.utils.FileUtils.writeContentToFile;

@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public final class DBEntityAnalysis {

    public final static String PRIMARY_KEY_TEMPLATE = "PRIMARY KEY (%s)";

    public final static String CREATE_TABLE_TEMPLATE = "CREATE TABLE %s\n(\n    %s,\n    %s\n);";

    public final static String CREATE_TABLE_TEMPLATE_NO_PRIMARY_KEY = "CREATE TABLE %s\n(\n    %s\n);";

    public final static String CREATE_TABLE_TEMPLATE2 = "CREATE TABLE \"%s\".\"%s\"\n(\n    %s,\n    %s\n);";

    public final static Map<String, String> POSTGRESQL_DATATYPE_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = -2343568510347376282L;

        {
            put("String", "VARCHAR");
            // put("String", "TEXT");
            put("Boolean", "BOOLEAN");
            // put("Boolean", "BOOLEAN");
            put("Integer", "INTEGER");
            put("Long", "BIGINT");
            put("Short", "SMALLINT");
            put("Byte", "SMALLINT");
            put("Float", "REAL");
            put("Double", "DOUBLE PRECISION");
            put("BigDecimal", "NUMERIC");
            // put("BigDecimal", "DECIMAL");
            put("Character", "CHAR(1)");
            put("byte[]", "BYTEA");
            put("java.util.Date", "TIMESTAMP");
            put("java.sql.Date", "DATE");
            put("java.sql.Time", "TIME");
            put("java.sql.Timestamp", "TIMESTAMP");
            put("LocalDate", "DATE");
            put("LocalDateTime", "TIMESTAMP");
            put("UUID", "UUID");
            put("Enum", "VARCHAR");
            put("Blob", "BYTEA");
            // put("Blob", "OID");
            put("Clob", "TEXT");
        }
    };

    public final static Map<String, String> MYSQL_DATATYPE_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = 601202175188150937L;

        {
            // put("String", "TEXT");
            put("String", "VARCHAR(10000)");
            put("Boolean", "TINYINT(1)");
            // put("Boolean", "TINYINT(1)");
            put("Integer", "INT");
            put("Long", "BIGINT");
            put("Short", "SMALLINT");
            put("Byte", "TINYINT");
            put("Float", "FLOAT");
            put("Double", "DOUBLE");
            put("BigDecimal", "DECIMAL");
            put("Character", "CHAR(1)");
            put("byte[]", "BLOB");
            // put("byte[]", "VARBINARY");
            put("java.util.Date", "DATETIME");
            put("java.sql.Date", "DATE");
            put("java.sql.Time", "TIME");
            put("java.sql.Timestamp", "DATETIME");
            put("LocalDate", "DATE");
            put("LocalDateTime", "DATETIME");
            put("UUID", "CHAR(36)");
            put("Enum", "VARCHAR");
            put("Blob", "BLOB");
            put("Clob", "TEXT");
        }
    };

    public final static Map<String, String> ORACLE_DATATYPE_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = 1255172379342418894L;

        {
            put("String", "VARCHAR2(4000)");
            put("Boolean", "NUMBER(1)");
            // put("Boolean", "NUMBER(1)");
            put("Integer", "NUMBER");
            // put("Integer", "INTEGER");
            put("Long", "NUMBER");
            // put("Long", "LONG");
            put("Short", "NUMBER(5)");
            put("Byte", "NUMBER(3)");
            put("Float", "BINARY_FLOAT");
            // put("Float", "FLOAT");
            put("Double", "BINARY_DOUBLE");
            // put("Double", "FLOAT");
            put("BigDecimal", "NUMBER(18, 0)");
            put("Character", "CHAR(1)");
            put("byte[]", "RAW");
            // put("byte[]", "BLOB");
            put("java.util.Date", "DATE");
            // put("java.util.Date", "TIMESTAMP");
            put("java.sql.Date", "DATE");
            put("java.sql.Time", "DATE");
            put("java.sql.Timestamp", "TIMESTAMP");
            put("LocalDate", "DATE");
            // put("LocalDateTime", "DATE");
            put("LocalDateTime", "TIMESTAMP");
            put("UUID", "RAW(16)");
            put("Enum", "VARCHAR2");
            put("Blob", "BLOB");
            put("Clob", "CLOB");
        }
    };

    public final static Map<String, String> MSSQL_DATATYPE_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = 4121761359733723975L;

        {
            put("String", "NVARCHAR(4000)");
            // put("String", "TEXT");
            put("Boolean", "BIT");
            // put("Boolean", "BIT");
            put("Integer", "INT");
            put("Long", "BIGINT");
            put("Short", "SMALLINT");
            put("Byte", "TINYINT");
            put("Float", "REAL");
            put("Double", "FLOAT");
            // put("Double", "DOUBLE");
            put("BigDecimal", "DECIMAL");
            // put("BigDecimal", "NUMERIC");
            put("Character", "CHAR(1)");
            put("byte[]", "VARBINARY");
            // put("byte[]", "BLOB");
            put("java.util.Date", "DATETIME");
            put("java.sql.Date", "DATE");
            put("java.sql.Time", "TIME");
            put("java.sql.Timestamp", "DATETIME2");
            put("LocalDate", "DATE");
            put("LocalDateTime", "DATETIME");
            put("UUID", "UNIQUEIDENTIFIER");
            put("Enum", "NVARCHAR");
            put("Blob", "VARBINARY(MAX)");
            put("Clob", "NTEXT");
        }
    };

    private HashSet<Element> fieldsOfClass;
    private String fullClassName;
    private TableName tableNameAnnotation;

    public DBEntityAnalysis(HashSet<Element> fieldsOfClass, String fullClassName, TableName tableNameAnnotation) {
        this.fieldsOfClass = fieldsOfClass;
        this.fullClassName = fullClassName;
        this.tableNameAnnotation = tableNameAnnotation;
    }

    public HashSet<Element> getFieldsOfClass() {
        return fieldsOfClass;
    }

    public void setFieldsOfClass(HashSet<Element> fieldsOfClass) {
        this.fieldsOfClass = fieldsOfClass;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public TableName getTableNameAnnotation() {
        return tableNameAnnotation;
    }

    public void setTableNameAnnotation(TableName tableNameAnnotation) {
        this.tableNameAnnotation = tableNameAnnotation;
    }

    public void generateTableCreationSQL() {
        try {
            TableName tableName = this.getTableNameAnnotation();

            Optional<Element> idField = this.getFieldsOfClass().stream()
                    .filter(field ->
                            field.getAnnotation(IdColumn.class) != null &&
                                    field.getAnnotation(ColumnName.class) != null &&
                                    StringUtils.isNotBlank(field.getAnnotation(ColumnName.class).name())
                    )
                    .findAny();

            if (!idField.isPresent()) {
                return;
            }

            List<List<String>> columnDefinitionLines = this.getFieldsOfClass().stream().map(field -> {
                ColumnName columnname = field.getAnnotation(ColumnName.class);

                String sqlDataType = "";

                for (Map.Entry<String, String> entry : ORACLE_DATATYPE_MAP.entrySet()) {
                    String k = entry.getKey();
                    String v = entry.getValue();
                    if (field.asType().toString().contains(k)) {
                        sqlDataType = v;
                        break;
                    }
                }

                return Arrays.asList(columnname.name(), sqlDataType, "NULL");
            }).filter(array -> array.stream().noneMatch(StringUtils::isBlank)).collect(Collectors.toList());
            String createTableStatement = String.format(
                    CREATE_TABLE_TEMPLATE + "\n",
                    tableName.value(),
                    MyStringUtils.formatStringSpace2(columnDefinitionLines, ",\n    "),
                    String.format(PRIMARY_KEY_TEMPLATE, idField.get().getAnnotation(ColumnName.class).name())
            );
            String folderPath = FileUtils.pathJoining(CommonConstant.ROOT_DIRECTORY_PROJECT_PATH, "data", "sql");
            createFolderIfNotExists(folderPath);
            writeContentToFile(
                    FileUtils.pathJoining(folderPath, tableName.value() + ".sql"),
                    createTableStatement
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
