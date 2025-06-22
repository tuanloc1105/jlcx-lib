package vn.com.lcx.common.database.type;

import oracle.jdbc.OracleTypes;

public enum OracleTypeEnum {

    BIT(OracleTypes.BIT),
    TINYINT(OracleTypes.TINYINT),
    SMALLINT(OracleTypes.SMALLINT),
    INTEGER(OracleTypes.INTEGER),
    BIGINT(OracleTypes.BIGINT),
    FLOAT(OracleTypes.FLOAT),
    REAL(OracleTypes.REAL),
    DOUBLE(OracleTypes.DOUBLE),
    NUMERIC(OracleTypes.NUMERIC),
    DECIMAL(OracleTypes.DECIMAL),
    CHAR(OracleTypes.CHAR),
    VARCHAR(OracleTypes.VARCHAR),
    LONGVARCHAR(OracleTypes.LONGVARCHAR),
    DATE(OracleTypes.DATE),
    TIME(OracleTypes.TIME),
    TIMESTAMP(OracleTypes.TIMESTAMP),
    PLSQL_BOOLEAN(OracleTypes.PLSQL_BOOLEAN),
    TIMESTAMPTZ(OracleTypes.TIMESTAMPTZ),
    TIMESTAMPLTZ(OracleTypes.TIMESTAMPLTZ),
    INTERVALYM(OracleTypes.INTERVALYM),
    INTERVALDS(OracleTypes.INTERVALDS),
    BINARY(OracleTypes.BINARY),
    VARBINARY(OracleTypes.VARBINARY),
    LONGVARBINARY(OracleTypes.LONGVARBINARY),
    ROWID(OracleTypes.ROWID),
    CURSOR(OracleTypes.CURSOR),
    BLOB(OracleTypes.BLOB),
    CLOB(OracleTypes.CLOB),
    BFILE(OracleTypes.BFILE),
    STRUCT(OracleTypes.STRUCT),
    ARRAY(OracleTypes.ARRAY),
    REF(OracleTypes.REF),
    NCHAR(OracleTypes.NCHAR),
    NCLOB(OracleTypes.NCLOB),
    NVARCHAR(OracleTypes.NVARCHAR),
    LONGNVARCHAR(OracleTypes.LONGNVARCHAR),
    SQLXML(OracleTypes.SQLXML),
    REF_CURSOR(OracleTypes.REF_CURSOR),
    JSON(OracleTypes.JSON),
    OPAQUE(OracleTypes.OPAQUE),
    JAVA_STRUCT(OracleTypes.JAVA_STRUCT),
    JAVA_OBJECT(OracleTypes.JAVA_OBJECT),
    PLSQL_INDEX_TABLE(OracleTypes.PLSQL_INDEX_TABLE),
    BINARY_FLOAT(OracleTypes.BINARY_FLOAT),
    BINARY_DOUBLE(OracleTypes.BINARY_DOUBLE),
    NULL(OracleTypes.NULL),
    NUMBER(OracleTypes.NUMBER),
    RAW(OracleTypes.RAW),
    OTHER(OracleTypes.OTHER),
    FIXED_CHAR(OracleTypes.FIXED_CHAR),
    DATALINK(OracleTypes.DATALINK),
    BOOLEAN(OracleTypes.BOOLEAN),

    ;

    private final int type;

    OracleTypeEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

}
