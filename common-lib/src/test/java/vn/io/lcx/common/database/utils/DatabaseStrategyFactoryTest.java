package vn.io.lcx.common.database.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DatabaseStrategyFactoryTest {

    @Nested
    class CreateStrategy {

        @Test
        void returnsPostgreSQLStrategyForPostgresql() {
            DatabaseStrategy strategy = DatabaseStrategyFactory.createStrategy("postgresql");
            assertInstanceOf(PostgreSQLStrategy.class, strategy);
        }

        @Test
        void returnsPostgreSQLStrategyForPostgresqlUpperCase() {
            DatabaseStrategy strategy = DatabaseStrategyFactory.createStrategy("POSTGRESQL");
            assertInstanceOf(PostgreSQLStrategy.class, strategy);
        }

        @Test
        void returnsMySQLStrategyForMysql() {
            DatabaseStrategy strategy = DatabaseStrategyFactory.createStrategy("mysql");
            assertInstanceOf(MySQLStrategy.class, strategy);
        }

        @Test
        void returnsMySQLStrategyForMysqlMixedCase() {
            DatabaseStrategy strategy = DatabaseStrategyFactory.createStrategy("MySQL");
            assertInstanceOf(MySQLStrategy.class, strategy);
        }

        @Test
        void returnsMSSQLStrategyForMssql() {
            DatabaseStrategy strategy = DatabaseStrategyFactory.createStrategy("mssql");
            assertInstanceOf(MSSQLStrategy.class, strategy);
        }

        @Test
        void returnsMSSQLStrategyForMssqlUpperCase() {
            DatabaseStrategy strategy = DatabaseStrategyFactory.createStrategy("MSSQL");
            assertInstanceOf(MSSQLStrategy.class, strategy);
        }

        @Test
        void returnsOracleStrategyForOracle() {
            DatabaseStrategy strategy = DatabaseStrategyFactory.createStrategy("oracle");
            assertInstanceOf(OracleStrategy.class, strategy);
        }

        @Test
        void returnsOracleStrategyForOracleUpperCase() {
            DatabaseStrategy strategy = DatabaseStrategyFactory.createStrategy("ORACLE");
            assertInstanceOf(OracleStrategy.class, strategy);
        }

        @Test
        void returnsOracleStrategyAsDefaultForUnknownType() {
            DatabaseStrategy strategy = DatabaseStrategyFactory.createStrategy("unknown_db");
            assertInstanceOf(OracleStrategy.class, strategy);
        }

        @Test
        void returnsOracleStrategyForEmptyString() {
            DatabaseStrategy strategy = DatabaseStrategyFactory.createStrategy("something_else");
            assertInstanceOf(OracleStrategy.class, strategy);
        }
    }
}
