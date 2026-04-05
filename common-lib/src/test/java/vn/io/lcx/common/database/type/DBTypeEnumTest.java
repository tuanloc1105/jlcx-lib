package vn.io.lcx.common.database.type;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DBTypeEnumTest {

    @Nested
    class Oracle {

        @Test
        void hasCorrectDriver() {
            assertEquals("oracle.jdbc.OracleDriver", DBTypeEnum.ORACLE.getDefaultDriverClassName());
        }

        @Test
        void hasCorrectUrlTemplate() {
            assertEquals("jdbc:oracle:thin:@//%s:%d/%s", DBTypeEnum.ORACLE.getTemplateUrlConnectionString());
        }

        @Test
        void hasCorrectDialect() {
            assertEquals("org.hibernate.dialect.OracleDialect", DBTypeEnum.ORACLE.getDialectClass());
        }

        @Test
        void hasShowDbVersionStatement() {
            assertEquals("SELECT * FROM v$version", DBTypeEnum.ORACLE.getShowDbVersionSqlStatement());
        }
    }

    @Nested
    class PostgreSQL {

        @Test
        void hasCorrectDriver() {
            assertEquals("org.postgresql.Driver", DBTypeEnum.POSTGRESQL.getDefaultDriverClassName());
        }

        @Test
        void hasCorrectUrlTemplate() {
            assertEquals("jdbc:postgresql://%s:%d/%s", DBTypeEnum.POSTGRESQL.getTemplateUrlConnectionString());
        }

        @Test
        void hasCorrectDialect() {
            assertEquals("org.hibernate.dialect.PostgreSQLDialect", DBTypeEnum.POSTGRESQL.getDialectClass());
        }

        @Test
        void hasShowDbVersionStatement() {
            assertEquals("SHOW server_version", DBTypeEnum.POSTGRESQL.getShowDbVersionSqlStatement());
        }
    }

    @Nested
    class MySQL {

        @Test
        void hasCorrectDriver() {
            assertEquals("com.mysql.cj.jdbc.Driver", DBTypeEnum.MYSQL.getDefaultDriverClassName());
        }

        @Test
        void hasCorrectUrlTemplate() {
            assertEquals("jdbc:mysql://%s:%d/%s", DBTypeEnum.MYSQL.getTemplateUrlConnectionString());
        }

        @Test
        void hasCorrectDialect() {
            assertEquals("org.hibernate.dialect.MySQLDialect", DBTypeEnum.MYSQL.getDialectClass());
        }

        @Test
        void hasShowDbVersionStatement() {
            assertEquals("SELECT VERSION()", DBTypeEnum.MYSQL.getShowDbVersionSqlStatement());
        }
    }

    @Nested
    class MSSQL {

        @Test
        void hasCorrectDriver() {
            assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", DBTypeEnum.MSSQL.getDefaultDriverClassName());
        }

        @Test
        void hasCorrectUrlTemplate() {
            assertEquals("jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false", DBTypeEnum.MSSQL.getTemplateUrlConnectionString());
        }

        @Test
        void hasCorrectDialect() {
            assertEquals("org.hibernate.dialect.SQLServerDialect", DBTypeEnum.MSSQL.getDialectClass());
        }

        @Test
        void hasShowDbVersionStatement() {
            assertEquals("SELECT @@VERSION", DBTypeEnum.MSSQL.getShowDbVersionSqlStatement());
        }
    }

    @Nested
    class EnumValues {

        @Test
        void hasFourValues() {
            assertEquals(4, DBTypeEnum.values().length);
        }

        @Test
        void allValuesImplementDBType() {
            for (DBTypeEnum dbType : DBTypeEnum.values()) {
                assertNotNull(dbType.getDefaultDriverClassName());
                assertNotNull(dbType.getTemplateUrlConnectionString());
                assertNotNull(dbType.getDialectClass());
                assertNotNull(dbType.getShowDbVersionSqlStatement());
            }
        }
    }
}
