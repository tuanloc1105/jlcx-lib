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
    },

}
