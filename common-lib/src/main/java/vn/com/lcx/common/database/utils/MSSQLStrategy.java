package vn.com.lcx.common.database.utils;

import java.util.List;

/**
 * MSSQL implementation of DatabaseStrategy
 */
public class MSSQLStrategy implements DatabaseStrategy {

    @Override
    public String generateIdColumnDefinition(String columnName, String dataType) {
        return String.format("%s INT IDENTITY(1,1) PRIMARY KEY", columnName);
    }

    @Override
    public String generateCreateIndex(String columnName, String tableName, boolean isUnique) {
        return String.format("CREATE INDEX %s_INDEX\nON %s (%s);\n", columnName, tableName, columnName);
    }

    @Override
    public String generateDropIndex(String columnName, String tableName) {
        return String.format("DROP INDEX %s_INDEX ON %s;\n", columnName, tableName);
    }

    @Override
    public String generateRenameColumn(String columnName, String tableName) {
        return String.format("EXEC sp_rename '%s.%s', '%s_new', 'COLUMN';\n", tableName, columnName, columnName);
    }

    @Override
    public String generateAddColumn(String columnName, String dataType, List<String> constraints, String tableName) {
        return String.format("ALTER TABLE %s\n  ADD %s %s %s;\n",
                tableName, columnName, dataType, String.join(" ", constraints));
    }

    @Override
    public String generateDropColumn(String columnName, String tableName) {
        return String.format("ALTER TABLE %s\n  DROP COLUMN %s;\n", tableName, columnName);
    }

    @Override
    public String generateModifyColumn(String columnName, String dataType, List<String> constraints, String tableName) {
        // MSSQL doesn't support MODIFY, would need to use ALTER COLUMN
        return String.format("-- MSSQL: ALTER TABLE %s ALTER COLUMN %s %s;\n", tableName, columnName, dataType);
    }

    @Override
    public String generateSequenceStatement(String tableName) {
        return ""; // MSSQL uses IDENTITY
    }

    @Override
    public String generateForeignKeyCascade(boolean cascade) {
        return cascade ? "\nON DELETE CASCADE\nON UPDATE CASCADE;" : ";";
    }
} 
