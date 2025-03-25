package vn.com.lcx.common.database.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PostgresTypeEnum {

    BIT(java.sql.Types.BIT),
    TINYINT(java.sql.Types.TINYINT),
    SMALLINT(java.sql.Types.SMALLINT),
    INTEGER(java.sql.Types.INTEGER),
    BIGINT(java.sql.Types.BIGINT),
    FLOAT(java.sql.Types.FLOAT),
    REAL(java.sql.Types.REAL),
    DOUBLE(java.sql.Types.DOUBLE),
    NUMERIC(java.sql.Types.NUMERIC),
    DECIMAL(java.sql.Types.DECIMAL),
    CHAR(java.sql.Types.CHAR),
    VARCHAR(java.sql.Types.VARCHAR),
    LONGVARCHAR(java.sql.Types.LONGVARCHAR),
    DATE(java.sql.Types.DATE),
    TIME(java.sql.Types.TIME),
    TIMESTAMP(java.sql.Types.TIMESTAMP),
    BINARY(java.sql.Types.BINARY),
    VARBINARY(java.sql.Types.VARBINARY),
    LONGVARBINARY(java.sql.Types.LONGVARBINARY),
    NULL(java.sql.Types.NULL),
    OTHER(java.sql.Types.OTHER),
    JAVA_OBJECT(java.sql.Types.JAVA_OBJECT),
    DISTINCT(java.sql.Types.DISTINCT),
    STRUCT(java.sql.Types.STRUCT),
    ARRAY(java.sql.Types.ARRAY),
    BLOB(java.sql.Types.BLOB),
    CLOB(java.sql.Types.CLOB),
    REF(java.sql.Types.REF),
    DATALINK(java.sql.Types.DATALINK),
    BOOLEAN(java.sql.Types.BOOLEAN),
    ROWID(java.sql.Types.ROWID),
    NCHAR(java.sql.Types.NCHAR),
    NVARCHAR(java.sql.Types.NVARCHAR),
    LONGNVARCHAR(java.sql.Types.LONGNVARCHAR),
    NCLOB(java.sql.Types.NCLOB),
    SQLXML(java.sql.Types.SQLXML),
    REF_CURSOR(java.sql.Types.REF_CURSOR),
    TIME_WITH_TIMEZONE(java.sql.Types.TIME_WITH_TIMEZONE),
    TIMESTAMP_WITH_TIMEZONE(java.sql.Types.TIMESTAMP_WITH_TIMEZONE),

    ;

    private final int type;

}
