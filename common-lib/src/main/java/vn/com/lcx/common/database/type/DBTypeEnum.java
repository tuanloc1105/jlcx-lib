package vn.com.lcx.common.database.type;

import lombok.AllArgsConstructor;
import lombok.Generated;

@AllArgsConstructor
@Generated
public enum DBTypeEnum implements DBType {

    ORACLE {
        @Override
        public String getDefaultDriverClassName() {
            return "oracle.jdbc.driver.OracleDriver";
        }

        @Override
        public String getTemplateUrlConnectionString() {
            return "jdbc:oracle:thin:@//%s:%d/%s";
        }

        @Override
        public String getShowDbVersionSqlStatement() {
            return "SELECT * FROM v$version";
        }

        @Override
        public String getDialectClass() {
            return "org.hibernate.dialect.OracleDialect";
        }
    },
    POSTGRESQL {
        @Override
        public String getDefaultDriverClassName() {
            return "org.postgresql.Driver";
        }

        @Override
        public String getTemplateUrlConnectionString() {
            return "jdbc:postgresql://%s:%d/%s";
        }

        @Override
        public String getShowDbVersionSqlStatement() {
            return "SHOW server_version";
        }

        @Override
        public String getDialectClass() {
            return "org.hibernate.dialect.PostgreSQLDialect";
        }
    },
    MYSQL {
        @Override
        public String getDefaultDriverClassName() {
            return "com.mysql.cj.jdbc.Driver";
        }

        @Override
        public String getTemplateUrlConnectionString() {
            return "jdbc:mysql://%s:%d/%s";
        }

        @Override
        public String getShowDbVersionSqlStatement() {
            return "SELECT VERSION()";
        }

        @Override
        public String getDialectClass() {
            return "org.hibernate.dialect.MySQLDialect";
        }
    },
    MSSQL {
        @Override
        public String getDefaultDriverClassName() {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        }

        @Override
        public String getTemplateUrlConnectionString() {
            return "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false";
        }

        @Override
        public String getShowDbVersionSqlStatement() {
            return "SELECT @@VERSION";
        }

        @Override
        public String getDialectClass() {
            return "org.hibernate.dialect.SQLServerDialect";
        }
    },

}
