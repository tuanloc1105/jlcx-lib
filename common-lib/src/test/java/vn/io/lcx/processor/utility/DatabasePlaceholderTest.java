package vn.io.lcx.processor.utility;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link DatabasePlaceholder} enum.
 * <p>
 * DatabasePlaceholder encodes database-specific SQL placeholder patterns
 * (e.g. "$" for PostgreSQL, "?" for MySQL, "@p" for MSSQL) used during
 * reactive repository code generation.
 */
class DatabasePlaceholderTest {

    // ------------------------------------------------------------------ //
    //  Enum values and basic properties
    // ------------------------------------------------------------------ //

    @Nested
    class EnumValues {

        @Test
        void hasFiveValues() {
            assertEquals(5, DatabasePlaceholder.values().length);
        }

        @Test
        void valueOfReturnsCorrectConstant() {
            assertEquals(DatabasePlaceholder.POSTGRESQL, DatabasePlaceholder.valueOf("POSTGRESQL"));
            assertEquals(DatabasePlaceholder.MYSQL, DatabasePlaceholder.valueOf("MYSQL"));
            assertEquals(DatabasePlaceholder.MARIADB, DatabasePlaceholder.valueOf("MARIADB"));
            assertEquals(DatabasePlaceholder.MSSQL, DatabasePlaceholder.valueOf("MSSQL"));
            assertEquals(DatabasePlaceholder.ORACLE, DatabasePlaceholder.valueOf("ORACLE"));
        }
    }

    // ------------------------------------------------------------------ //
    //  getDatabaseName
    // ------------------------------------------------------------------ //

    @Nested
    class GetDatabaseName {

        @Test
        void postgresqlReturnsPostgreSQL() {
            assertEquals("PostgreSQL", DatabasePlaceholder.POSTGRESQL.getDatabaseName());
        }

        @Test
        void mysqlReturnsMySQL() {
            assertEquals("MySQL", DatabasePlaceholder.MYSQL.getDatabaseName());
        }

        @Test
        void mariadbReturnsMariaDB() {
            assertEquals("MariaDB", DatabasePlaceholder.MARIADB.getDatabaseName());
        }

        @Test
        void mssqlReturnsMicrosoftSQLServer() {
            assertEquals("Microsoft SQL Server", DatabasePlaceholder.MSSQL.getDatabaseName());
        }

        @Test
        void oracleReturnsOracle() {
            assertEquals("Oracle", DatabasePlaceholder.ORACLE.getDatabaseName());
        }
    }

    // ------------------------------------------------------------------ //
    //  getPlaceholder
    // ------------------------------------------------------------------ //

    @Nested
    class GetPlaceholder {

        @Test
        void postgresqlUsesDollarSign() {
            assertEquals("$", DatabasePlaceholder.POSTGRESQL.getPlaceholder());
        }

        @Test
        void mysqlUsesQuestionMark() {
            assertEquals("?", DatabasePlaceholder.MYSQL.getPlaceholder());
        }

        @Test
        void mariadbUsesQuestionMark() {
            assertEquals("?", DatabasePlaceholder.MARIADB.getPlaceholder());
        }

        @Test
        void mssqlUsesAtP() {
            assertEquals("@p", DatabasePlaceholder.MSSQL.getPlaceholder());
        }

        @Test
        void oracleUsesQuestionMark() {
            assertEquals("?", DatabasePlaceholder.ORACLE.getPlaceholder());
        }
    }

    // ------------------------------------------------------------------ //
    //  requiresIndex
    // ------------------------------------------------------------------ //

    @Nested
    class RequiresIndex {

        @Test
        void postgresqlRequiresIndex() {
            assertTrue(DatabasePlaceholder.POSTGRESQL.requiresIndex());
        }

        @Test
        void mssqlRequiresIndex() {
            assertTrue(DatabasePlaceholder.MSSQL.requiresIndex());
        }

        @Test
        void mysqlDoesNotRequireIndex() {
            assertFalse(DatabasePlaceholder.MYSQL.requiresIndex());
        }

        @Test
        void mariadbDoesNotRequireIndex() {
            assertFalse(DatabasePlaceholder.MARIADB.requiresIndex());
        }

        @Test
        void oracleDoesNotRequireIndex() {
            assertFalse(DatabasePlaceholder.ORACLE.requiresIndex());
        }
    }

    // ------------------------------------------------------------------ //
    //  getPlaceholderWithIndex
    // ------------------------------------------------------------------ //

    @Nested
    class GetPlaceholderWithIndex {

        @ParameterizedTest(name = "PostgreSQL index {0} -> ${0}")
        @ValueSource(ints = {1, 2, 5, 99})
        void postgresqlAppendsIndex(int index) {
            assertEquals("$" + index, DatabasePlaceholder.POSTGRESQL.getPlaceholderWithIndex(index));
        }

        @ParameterizedTest(name = "MSSQL index {0} -> @p{0}")
        @ValueSource(ints = {1, 2, 5, 99})
        void mssqlAppendsIndex(int index) {
            assertEquals("@p" + index, DatabasePlaceholder.MSSQL.getPlaceholderWithIndex(index));
        }

        @ParameterizedTest(name = "MySQL index {0} -> ? (unchanged)")
        @ValueSource(ints = {1, 2, 5, 99})
        void mysqlIgnoresIndex(int index) {
            assertEquals("?", DatabasePlaceholder.MYSQL.getPlaceholderWithIndex(index));
        }

        @ParameterizedTest(name = "MariaDB index {0} -> ? (unchanged)")
        @ValueSource(ints = {1, 2, 5, 99})
        void mariadbIgnoresIndex(int index) {
            assertEquals("?", DatabasePlaceholder.MARIADB.getPlaceholderWithIndex(index));
        }

        @ParameterizedTest(name = "Oracle index {0} -> ? (unchanged)")
        @ValueSource(ints = {1, 2, 5, 99})
        void oracleIgnoresIndex(int index) {
            assertEquals("?", DatabasePlaceholder.ORACLE.getPlaceholderWithIndex(index));
        }
    }

    // ------------------------------------------------------------------ //
    //  fromDatabaseName
    // ------------------------------------------------------------------ //

    @Nested
    class FromDatabaseName {

        @ParameterizedTest(name = "fromDatabaseName(\"{0}\") resolves correctly")
        @CsvSource({
                "PostgreSQL,    POSTGRESQL",
                "MySQL,         MYSQL",
                "MariaDB,       MARIADB",
                "Microsoft SQL Server, MSSQL",
                "Oracle,        ORACLE"
        })
        void resolvesKnownDatabases(String dbName, String expectedEnum) {
            DatabasePlaceholder result = DatabasePlaceholder.fromDatabaseName(dbName);

            assertEquals(DatabasePlaceholder.valueOf(expectedEnum), result);
        }

        @Test
        void throwsForUnknownDatabase() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> DatabasePlaceholder.fromDatabaseName("SQLite"));

            assertTrue(ex.getMessage().contains("Unsupported database"));
            assertTrue(ex.getMessage().contains("SQLite"));
        }

        @Test
        void throwsForNullDatabaseName() {
            assertThrows(Exception.class,
                    () -> DatabasePlaceholder.fromDatabaseName(null));
        }

        @Test
        void isCaseSensitive() {
            // "postgresql" (lowercase) should NOT match "PostgreSQL"
            assertThrows(IllegalArgumentException.class,
                    () -> DatabasePlaceholder.fromDatabaseName("postgresql"));
        }
    }

    // ------------------------------------------------------------------ //
    //  isSupported
    // ------------------------------------------------------------------ //

    @Nested
    class IsSupported {

        @ParameterizedTest(name = "\"{0}\" is supported")
        @ValueSource(strings = {"PostgreSQL", "MySQL", "MariaDB", "Microsoft SQL Server", "Oracle"})
        void returnsTrueForSupportedDatabases(String dbName) {
            assertTrue(DatabasePlaceholder.isSupported(dbName));
        }

        @ParameterizedTest(name = "\"{0}\" is not supported")
        @ValueSource(strings = {"SQLite", "H2", "DB2", "postgresql", "mysql", ""})
        void returnsFalseForUnsupportedDatabases(String dbName) {
            assertFalse(DatabasePlaceholder.isSupported(dbName));
        }
    }
}
