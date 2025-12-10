package vn.com.lcx.common.database.utils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PostgreSQL implementation of DatabaseStrategy
 */
public class PostgreSQLStrategy implements DatabaseStrategy {

    @Override
    public String generateIdColumnDefinition(String tableName, String columnName, String dataType) {
        return String.format("%s SERIAL PRIMARY KEY", columnName);
    }

    @Override
    public String generateCreateIndex(String columnName, String tableName, boolean isUnique) {
        String indexType = isUnique ? "UNIQUE INDEX" : "INDEX";
        return String.format("CREATE %s %s_INDEX\nON %s (%s);\n", indexType, columnName, tableName, columnName);
    }

    @Override
    public String generateDropIndex(String columnName, String tableName) {
        return String.format("DROP INDEX %s_INDEX;\n", columnName);
    }

    @Override
    public String generateRenameColumn(String columnName, String tableName) {
        return String.format("ALTER TABLE %s RENAME COLUMN %s TO %s_new;\n", tableName, columnName, columnName);
    }

    @Override
    public String generateAddColumn(ColumnDefinition columnDefinition, String tableName) {
        StringBuilder constraints = new StringBuilder();
        if (!columnDefinition.isNullable()) {
            constraints.append(" NOT NULL");
        }
        if (columnDefinition.getDefaultValue() != null && !columnDefinition.getDefaultValue().isEmpty()) {
            constraints.append(" DEFAULT ").append(columnDefinition.getDefaultValue());
        }

        String result = String.format("ALTER TABLE %s\n  ADD COLUMN %s %s%s;\n",
                tableName, columnDefinition.getColumnName(), columnDefinition.getDataType(), constraints);

        if (columnDefinition.isUnique()) {
            result += String.format("ALTER TABLE %s\n  ADD CONSTRAINT %s_unique UNIQUE (%s);\n",
                    tableName, columnDefinition.getColumnName(), columnDefinition.getColumnName());
        }

        return result;
    }

    @Override
    public String generateDropColumn(String columnName, String tableName) {
        String result = String.format("ALTER TABLE %s\n  DROP COLUMN %s;\n", tableName, columnName);
        result += String.format("ALTER TABLE %s\n  DROP CONSTRAINT IF EXISTS %s_unique;\n", tableName, columnName);
        return result;
    }

    @Override
    public String generateModifyColumn(ColumnDefinition columnDefinition, String tableName) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("ALTER TABLE %s\n  ALTER COLUMN %s TYPE %s",
                tableName, columnDefinition.getColumnName(), columnDefinition.getDataType()));

        if (columnDefinition.isNullable()) {
            result.append(",\n  ALTER COLUMN ").append(columnDefinition.getColumnName()).append(" DROP NOT NULL");
        } else {
            result.append(",\n  ALTER COLUMN ").append(columnDefinition.getColumnName()).append(" SET NOT NULL");
        }

        if (columnDefinition.getDefaultValue() != null && !columnDefinition.getDefaultValue().isEmpty()) {
            result.append(",\n  ALTER COLUMN ").append(columnDefinition.getColumnName()).append(" SET DEFAULT ")
                    .append(columnDefinition.getDefaultValue());
        } else {
            result.append(",\n  ALTER COLUMN ").append(columnDefinition.getColumnName()).append(" DROP DEFAULT");
        }

        result.append(";\n");

        if (columnDefinition.isUnique()) {
            result.append(String.format("ALTER TABLE %s\n  ADD CONSTRAINT %s_unique UNIQUE (%s);\n",
                    tableName, columnDefinition.getColumnName(), columnDefinition.getColumnName()));
        }

        return result.toString();
    }

    @Override
    public String generateSequenceStatement(String tableName) {
        return ""; // PostgreSQL uses SERIAL
    }

    @Override
    public String generateForeignKeyCascade(boolean cascade) {
        return cascade ? "\nON DELETE SET NULL\nON UPDATE CASCADE;" : ";";
    }
}
