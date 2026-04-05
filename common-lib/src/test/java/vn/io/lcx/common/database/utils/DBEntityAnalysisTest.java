package vn.io.lcx.common.database.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DBEntityAnalysisTest {

    @Nested
    class PostgreSQLDatatypeMap {

        @Test
        void mapsStringToVarchar255() {
            assertEquals("VARCHAR(255)", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("String"));
        }

        @Test
        void mapsBooleanToBoolean() {
            assertEquals("BOOLEAN", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("Boolean"));
        }

        @Test
        void mapsIntegerToInteger() {
            assertEquals("INTEGER", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("Integer"));
        }

        @Test
        void mapsLongToBigint() {
            assertEquals("BIGINT", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("Long"));
        }

        @Test
        void mapsFloatToReal() {
            assertEquals("REAL", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("Float"));
        }

        @Test
        void mapsDoubleToDoublePrecision() {
            assertEquals("DOUBLE PRECISION", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("Double"));
        }

        @Test
        void mapsBigDecimalToNumeric() {
            assertEquals("NUMERIC(19,2)", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("BigDecimal"));
        }

        @Test
        void mapsUUIDToUUID() {
            assertEquals("UUID", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("UUID"));
        }

        @Test
        void mapsLocalDateTimeToTimestamp() {
            assertEquals("TIMESTAMP", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("LocalDateTime"));
        }

        @Test
        void mapsMapToJsonb() {
            assertEquals("JSONB", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("Map"));
        }

        @Test
        void mapsJsonToJsonb() {
            assertEquals("JSONB", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("JSON"));
        }

        @Test
        void mapsOffsetDateTimeToTimestampWithTimezone() {
            assertEquals("TIMESTAMP WITH TIME ZONE", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("OffsetDateTime"));
        }

        @Test
        void mapsByteArrayToBytea() {
            assertEquals("BYTEA", DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("byte[]"));
        }
    }

    @Nested
    class MySQLDatatypeMap {

        @Test
        void mapsStringToVarchar255() {
            assertEquals("VARCHAR(255)", DBEntityAnalysis.MYSQL_DATATYPE_MAP.get("String"));
        }

        @Test
        void mapsIntegerToInt() {
            assertEquals("INT", DBEntityAnalysis.MYSQL_DATATYPE_MAP.get("Integer"));
        }

        @Test
        void mapsLongToBigint() {
            assertEquals("BIGINT", DBEntityAnalysis.MYSQL_DATATYPE_MAP.get("Long"));
        }

        @Test
        void mapsByteToTinyint() {
            assertEquals("TINYINT", DBEntityAnalysis.MYSQL_DATATYPE_MAP.get("Byte"));
        }

        @Test
        void mapsDoubleToDouble() {
            assertEquals("DOUBLE", DBEntityAnalysis.MYSQL_DATATYPE_MAP.get("Double"));
        }

        @Test
        void mapsUUIDToChar36() {
            assertEquals("CHAR(36)", DBEntityAnalysis.MYSQL_DATATYPE_MAP.get("UUID"));
        }

        @Test
        void mapsLocalDateTimeToDatetime() {
            assertEquals("DATETIME", DBEntityAnalysis.MYSQL_DATATYPE_MAP.get("LocalDateTime"));
        }

        @Test
        void mapsMapToJson() {
            assertEquals("JSON", DBEntityAnalysis.MYSQL_DATATYPE_MAP.get("Map"));
        }

        @Test
        void mapsByteArrayToBlob() {
            assertEquals("BLOB", DBEntityAnalysis.MYSQL_DATATYPE_MAP.get("byte[]"));
        }
    }

    @Nested
    class OracleDatatypeMap {

        @Test
        void mapsStringToVarchar2() {
            assertEquals("VARCHAR2(255)", DBEntityAnalysis.ORACLE_DATATYPE_MAP.get("String"));
        }

        @Test
        void mapsBooleanToNumber1() {
            assertEquals("NUMBER(1)", DBEntityAnalysis.ORACLE_DATATYPE_MAP.get("Boolean"));
        }

        @Test
        void mapsIntegerToNumber10() {
            assertEquals("NUMBER(10)", DBEntityAnalysis.ORACLE_DATATYPE_MAP.get("Integer"));
        }

        @Test
        void mapsLongToNumber19() {
            assertEquals("NUMBER(19)", DBEntityAnalysis.ORACLE_DATATYPE_MAP.get("Long"));
        }

        @Test
        void mapsFloatToBinaryFloat() {
            assertEquals("BINARY_FLOAT", DBEntityAnalysis.ORACLE_DATATYPE_MAP.get("Float"));
        }

        @Test
        void mapsDoubleToBinaryDouble() {
            assertEquals("BINARY_DOUBLE", DBEntityAnalysis.ORACLE_DATATYPE_MAP.get("Double"));
        }

        @Test
        void mapsUUIDToRaw16() {
            assertEquals("RAW(16)", DBEntityAnalysis.ORACLE_DATATYPE_MAP.get("UUID"));
        }

        @Test
        void mapsXmlToXmltype() {
            assertEquals("XMLTYPE", DBEntityAnalysis.ORACLE_DATATYPE_MAP.get("XML"));
        }

        @Test
        void mapsMapToClob() {
            assertEquals("CLOB", DBEntityAnalysis.ORACLE_DATATYPE_MAP.get("Map"));
        }

        @Test
        void mapsByteArrayToRaw2000() {
            assertEquals("RAW(2000)", DBEntityAnalysis.ORACLE_DATATYPE_MAP.get("byte[]"));
        }
    }

    @Nested
    class MSSQLDatatypeMap {

        @Test
        void mapsStringToNvarchar255() {
            assertEquals("NVARCHAR(255)", DBEntityAnalysis.MSSQL_DATATYPE_MAP.get("String"));
        }

        @Test
        void mapsBooleanToBit() {
            assertEquals("BIT", DBEntityAnalysis.MSSQL_DATATYPE_MAP.get("Boolean"));
        }

        @Test
        void mapsIntegerToInt() {
            assertEquals("INT", DBEntityAnalysis.MSSQL_DATATYPE_MAP.get("Integer"));
        }

        @Test
        void mapsLongToBigint() {
            assertEquals("BIGINT", DBEntityAnalysis.MSSQL_DATATYPE_MAP.get("Long"));
        }

        @Test
        void mapsDoubleToFloat53() {
            assertEquals("FLOAT(53)", DBEntityAnalysis.MSSQL_DATATYPE_MAP.get("Double"));
        }

        @Test
        void mapsUUIDToUniqueidentifier() {
            assertEquals("UNIQUEIDENTIFIER", DBEntityAnalysis.MSSQL_DATATYPE_MAP.get("UUID"));
        }

        @Test
        void mapsLocalDateTimeToDatetime2() {
            assertEquals("DATETIME2", DBEntityAnalysis.MSSQL_DATATYPE_MAP.get("LocalDateTime"));
        }

        @Test
        void mapsJsonToNvarcharMax() {
            assertEquals("NVARCHAR(MAX)", DBEntityAnalysis.MSSQL_DATATYPE_MAP.get("JSON"));
        }

        @Test
        void mapsByteArrayToVarbinaryMax() {
            assertEquals("VARBINARY(MAX)", DBEntityAnalysis.MSSQL_DATATYPE_MAP.get("byte[]"));
        }

        @Test
        void mapsOffsetDateTimeToDatetimeoffset() {
            assertEquals("DATETIMEOFFSET", DBEntityAnalysis.MSSQL_DATATYPE_MAP.get("OffsetDateTime"));
        }
    }

    @Nested
    class NullTypeDefaults {

        @Test
        void postgresqlReturnsNullForUnknownType() {
            assertNull(DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get("UnknownType"));
        }

        @Test
        void mysqlReturnsNullForUnknownType() {
            assertNull(DBEntityAnalysis.MYSQL_DATATYPE_MAP.get("UnknownType"));
        }

        @Test
        void oracleReturnsNullForUnknownType() {
            assertNull(DBEntityAnalysis.ORACLE_DATATYPE_MAP.get("UnknownType"));
        }

        @Test
        void mssqlReturnsNullForUnknownType() {
            assertNull(DBEntityAnalysis.MSSQL_DATATYPE_MAP.get("UnknownType"));
        }

        @Test
        void postgresqlReturnsNullForNullKey() {
            assertNull(DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get(null));
        }
    }

    @Nested
    class TypeMappingCompleteness {

        private static final String[] COMMON_TYPES = {
                "String", "Boolean", "Integer", "Long", "Short", "Byte",
                "Float", "Double", "BigDecimal", "BigInteger", "Character",
                "byte[]", "java.util.Date", "java.sql.Date", "java.sql.Time",
                "java.sql.Timestamp", "LocalDate", "LocalDateTime", "LocalTime",
                "OffsetDateTime", "Instant", "UUID", "Enum", "Blob", "Clob",
                "List", "Set", "Map", "JSON", "XML", "Array"
        };

        @Test
        void postgresqlMapsAllCommonTypes() {
            for (String type : COMMON_TYPES) {
                assertNotNull(DBEntityAnalysis.POSTGRESQL_DATATYPE_MAP.get(type),
                        "PostgreSQL missing mapping for: " + type);
            }
        }

        @Test
        void mysqlMapsAllCommonTypes() {
            for (String type : COMMON_TYPES) {
                assertNotNull(DBEntityAnalysis.MYSQL_DATATYPE_MAP.get(type),
                        "MySQL missing mapping for: " + type);
            }
        }

        @Test
        void oracleMapsAllCommonTypes() {
            for (String type : COMMON_TYPES) {
                assertNotNull(DBEntityAnalysis.ORACLE_DATATYPE_MAP.get(type),
                        "Oracle missing mapping for: " + type);
            }
        }

        @Test
        void mssqlMapsAllCommonTypes() {
            for (String type : COMMON_TYPES) {
                assertNotNull(DBEntityAnalysis.MSSQL_DATATYPE_MAP.get(type),
                        "MSSQL missing mapping for: " + type);
            }
        }
    }

    @Nested
    class Templates {

        @Test
        void primaryKeyTemplateIsCorrect() {
            assertEquals("PRIMARY KEY (%s)", DBEntityAnalysis.PRIMARY_KEY_TEMPLATE);
        }

        @Test
        void createTableTemplateIsCorrect() {
            assertEquals("CREATE TABLE %s\n(\n    %s,\n    %s\n);", DBEntityAnalysis.CREATE_TABLE_TEMPLATE);
        }

        @Test
        void createTableTemplateNoPrimaryKeyIsCorrect() {
            assertEquals("CREATE TABLE %s\n(\n    %s\n);", DBEntityAnalysis.CREATE_TABLE_TEMPLATE_NO_PRIMARY_KEY);
        }

        @Test
        void dropTableTemplateIsCorrect() {
            assertEquals("-- DROP TABLE %s;", DBEntityAnalysis.DROP_TABLE_TEMPLATE_NO_PRIMARY_KEY);
        }

        @Test
        void truncateTableTemplateIsCorrect() {
            assertEquals("-- TRUNCATE TABLE %s;", DBEntityAnalysis.TRUNCATE_TABLE_TEMPLATE_NO_PRIMARY_KEY);
        }
    }
}
