package vn.io.lcx.common.database.utils;

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
    public String generateCreateIndex(String indexName, String tableName, String columnExpression, boolean isUnique) {
        String indexType = isUnique ? "UNIQUE INDEX" : "INDEX";
        return String.format("CREATE %s %s\nON %s (%s);\n", indexType, indexName, tableName, columnExpression);
    }

    @Override
    public String generateDropIndex(String indexName, String tableName) {
        return String.format("DROP INDEX %s ON %s;\n", indexName, tableName);
    }

    @Override
    public String generateRenameColumn(String columnName, String tableName) {
        return String.format("ALTER TABLE %s RENAME COLUMN %s TO %s_new;\n", tableName, columnName, columnName);
    }

    @Override
    public String generateAddColumn(ColumnDefinition columnDefinition, String tableName) {
        StringBuilder constraints = new StringBuilder();
        if (columnDefinition.getDefaultValue() != null && !columnDefinition.getDefaultValue().isEmpty()) {
            constraints.append(" DEFAULT ").append(columnDefinition.getDefaultValue());
        }
        if (!columnDefinition.isNullable()) {
            constraints.append(" NOT NULL");
        }
        if (columnDefinition.isUnique()) {
            constraints.append(" UNIQUE");
        }

        return String.format("ALTER TABLE %s\n  ADD %s %s%s;\n",
                tableName, columnDefinition.getColumnName(), columnDefinition.getDataType(), constraints);
    }

    @Override
    public String generateDropColumn(String columnName, String tableName) {
        return String.format("ALTER TABLE %s\n  DROP COLUMN %s;\n", tableName, columnName);
    }

    @Override
    public String generateModifyColumn(ColumnDefinition columnDefinition, String tableName) {
        StringBuilder constraints = new StringBuilder();
        if (columnDefinition.getDefaultValue() != null && !columnDefinition.getDefaultValue().isEmpty()) {
            constraints.append(" DEFAULT ").append(columnDefinition.getDefaultValue());
        }
        if (!columnDefinition.isNullable()) {
            constraints.append(" NOT NULL");
        } else {
            constraints.append(" NULL");
        }

        return String.format("ALTER TABLE %s\n  MODIFY %s %s%s;\n",
                tableName, columnDefinition.getColumnName(), columnDefinition.getDataType(), constraints);
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
