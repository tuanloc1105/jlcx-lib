package vn.com.lcx.common.database.utils;

import java.util.List;

/**
 * MySQL implementation of DatabaseStrategy
 */
public class MySQLStrategy implements DatabaseStrategy {

    @Override
    public String generateIdColumnDefinition(String tableName, String columnName, String dataType) {
        return String.format("%s INT AUTO_INCREMENT PRIMARY KEY", columnName);
    }

    @Override
    public String generateCreateIndex(String columnName, String tableName, boolean isUnique) {
        String indexType = isUnique ? "UNIQUE INDEX" : "INDEX";
        return String.format("CREATE %s %s_INDEX\nON %s (%s);\n", indexType, columnName, tableName, columnName);
    }

    @Override
    public String generateDropIndex(String columnName, String tableName) {
        return String.format("DROP INDEX %s_INDEX ON %s;\n", columnName, tableName);
    }

    @Override
    public String generateRenameColumn(String columnName, String tableName) {
        return String.format("ALTER TABLE %s RENAME COLUMN %s TO %s_new;\n", tableName, columnName, columnName);
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
        return String.format("ALTER TABLE %s\n  MODIFY %s %s %s;\n",
                tableName, columnName, dataType, String.join(" ", constraints));
    }

    @Override
    public String generateSequenceStatement(String tableName) {
        return ""; // MySQL uses AUTO_INCREMENT
    }

    @Override
    public String generateForeignKeyCascade(boolean cascade) {
        return cascade ? "\nON DELETE CASCADE\nON UPDATE RESTRICT;" : ";";
    }
} 
