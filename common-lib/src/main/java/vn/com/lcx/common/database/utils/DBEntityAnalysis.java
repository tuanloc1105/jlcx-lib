package vn.com.lcx.common.database.utils;

import vn.com.lcx.common.annotation.TableName;

import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public final class DBEntityAnalysis {

    public final static String PRIMARY_KEY_TEMPLATE = "PRIMARY KEY (%s)";

    public final static String CREATE_TABLE_TEMPLATE = "CREATE TABLE %s\n(\n    %s,\n    %s\n);";

    public final static String CREATE_TABLE_TEMPLATE_NO_PRIMARY_KEY = "CREATE TABLE %s\n(\n    %s\n);";

    public final static String DROP_TABLE_TEMPLATE_NO_PRIMARY_KEY = "-- DROP TABLE %s;";

    public final static String TRUNCATE_TABLE_TEMPLATE_NO_PRIMARY_KEY = "-- TRUNCATE TABLE %s;";

    // public final static String POSTGRESQL_TRUNCATE_TABLE_TEMPLATE_NO_PRIMARY_KEY = "-- TRUNCATE TABLE %s RESTART IDENTITY CASCADE;";

    public final static String CREATE_TABLE_TEMPLATE2 = "CREATE TABLE \"%s\".\"%s\"\n(\n    %s,\n    %s\n);";

    public final static Map<String, String> POSTGRESQL_DATATYPE_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = -2343568510347376282L;

        {
            put("String", "VARCHAR(255)");
            put("Boolean", "BOOLEAN");
            put("Integer", "INTEGER");
            put("Long", "BIGINT");
            put("Short", "SMALLINT");
            put("Byte", "SMALLINT");
            put("Float", "REAL");
            put("Double", "DOUBLE PRECISION");
            put("BigDecimal", "NUMERIC(19,2)");
            put("BigInteger", "NUMERIC(38)");
            put("Character", "CHAR(1)");
            put("byte[]", "BYTEA");
            put("java.util.Date", "TIMESTAMP");
            put("java.sql.Date", "DATE");
            put("java.sql.Time", "TIME");
            put("java.sql.Timestamp", "TIMESTAMP");
            put("LocalDate", "DATE");
            put("LocalDateTime", "TIMESTAMP");
            put("LocalTime", "TIME");
            put("OffsetDateTime", "TIMESTAMP WITH TIME ZONE");
            put("Instant", "TIMESTAMP");
            put("UUID", "UUID");
            put("Enum", "VARCHAR(50)");
            put("Blob", "BYTEA");
            put("Clob", "TEXT");
            put("List", "TEXT");
            put("Set", "TEXT");
            put("Map", "JSONB");
            put("JSON", "JSONB");
            put("XML", "XML");
            put("Array", "TEXT[]");
        }
    };

    public final static Map<String, String> MYSQL_DATATYPE_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = 601202175188150937L;

        {
            put("String", "VARCHAR(255)");
            put("Boolean", "BOOLEAN");
            put("Integer", "INT");
            put("Long", "BIGINT");
            put("Short", "SMALLINT");
            put("Byte", "TINYINT");
            put("Float", "FLOAT");
            put("Double", "DOUBLE");
            put("BigDecimal", "DECIMAL(19,2)");
            put("BigInteger", "DECIMAL(38,0)");
            put("Character", "CHAR(1)");
            put("byte[]", "BLOB");
            put("java.util.Date", "DATETIME");
            put("java.sql.Date", "DATE");
            put("java.sql.Time", "TIME");
            put("java.sql.Timestamp", "DATETIME");
            put("LocalDate", "DATE");
            put("LocalDateTime", "DATETIME");
            put("LocalTime", "TIME");
            put("OffsetDateTime", "DATETIME");
            put("Instant", "DATETIME");
            put("UUID", "CHAR(36)");
            put("Enum", "VARCHAR(50)");
            put("Blob", "BLOB");
            put("Clob", "TEXT");
            put("List", "TEXT");
            put("Set", "TEXT");
            put("Map", "JSON");
            put("JSON", "JSON");
            put("XML", "TEXT");
            put("Array", "TEXT");
        }
    };

    public final static Map<String, String> ORACLE_DATATYPE_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = 1255172379342418894L;

        {
            put("String", "VARCHAR2(255)");
            put("Boolean", "NUMBER(1)");
            put("Integer", "NUMBER(10)");
            put("Long", "NUMBER(19)");
            put("Short", "NUMBER(5)");
            put("Byte", "NUMBER(3)");
            put("Float", "BINARY_FLOAT");
            put("Double", "BINARY_DOUBLE");
            put("BigDecimal", "NUMBER(19,2)");
            put("BigInteger", "NUMBER(38)");
            put("Character", "CHAR(1)");
            put("byte[]", "RAW(2000)");
            put("java.util.Date", "DATE");
            put("java.sql.Date", "DATE");
            put("java.sql.Time", "DATE");
            put("java.sql.Timestamp", "TIMESTAMP");
            put("LocalDate", "DATE");
            put("LocalDateTime", "TIMESTAMP");
            put("LocalTime", "DATE");
            put("OffsetDateTime", "TIMESTAMP WITH TIME ZONE");
            put("Instant", "TIMESTAMP");
            put("UUID", "RAW(16)");
            put("Enum", "VARCHAR2(50)");
            put("Blob", "BLOB");
            put("Clob", "CLOB");
            put("List", "CLOB");
            put("Set", "CLOB");
            put("Map", "CLOB");
            put("JSON", "CLOB");
            put("XML", "XMLTYPE");
            put("Array", "VARRAY");
        }
    };

    public final static Map<String, String> MSSQL_DATATYPE_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = 4121761359733723975L;

        {
            put("String", "NVARCHAR(255)");
            put("Boolean", "BIT");
            put("Integer", "INT");
            put("Long", "BIGINT");
            put("Short", "SMALLINT");
            put("Byte", "TINYINT");
            put("Float", "REAL");
            put("Double", "FLOAT(53)");
            put("BigDecimal", "DECIMAL(19,2)");
            put("BigInteger", "DECIMAL(38,0)");
            put("Character", "CHAR(1)");
            put("byte[]", "VARBINARY(MAX)");
            put("java.util.Date", "DATETIME");
            put("java.sql.Date", "DATE");
            put("java.sql.Time", "TIME");
            put("java.sql.Timestamp", "DATETIME2");
            put("LocalDate", "DATE");
            put("LocalDateTime", "DATETIME2");
            put("LocalTime", "TIME");
            put("OffsetDateTime", "DATETIMEOFFSET");
            put("Instant", "DATETIME2");
            put("UUID", "UNIQUEIDENTIFIER");
            put("Enum", "NVARCHAR(50)");
            put("Blob", "VARBINARY(MAX)");
            put("Clob", "NTEXT");
            put("List", "NTEXT");
            put("Set", "NTEXT");
            put("Map", "NTEXT");
            put("JSON", "NVARCHAR(MAX)");
            put("XML", "XML");
            put("Array", "NTEXT");
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

}
