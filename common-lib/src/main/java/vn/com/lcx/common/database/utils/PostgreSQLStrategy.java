package vn.com.lcx.common.database.utils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PostgreSQL implementation of DatabaseStrategy
 */
public class PostgreSQLStrategy implements DatabaseStrategy {

    @Override
    public String generateIdColumnDefinition(String columnName, String dataType) {
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
    public String generateAddColumn(String columnName, String dataType, List<String> constraints, String tableName) {
        String constraintStr = constraints.stream()
                .filter(c -> !c.equalsIgnoreCase("unique"))
                .collect(Collectors.joining(" "));

        String result = String.format("ALTER TABLE %s\n  ADD COLUMN %s %s %s;\n",
                tableName, columnName, dataType, constraintStr);

        if (constraints.stream().anyMatch(c -> c.equalsIgnoreCase("unique"))) {
            result += String.format("ALTER TABLE %s\n  ADD CONSTRAINT %s_unique UNIQUE (%s);\n",
                    tableName, columnName, columnName);
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
    public String generateModifyColumn(String columnName, String dataType, List<String> constraints, String tableName) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("ALTER TABLE %s\n  ALTER COLUMN %s TYPE %s", tableName, columnName, dataType));

        if (!constraints.isEmpty()) {
            result.append(",\n  ");
            result.append(constraints.stream()
                    .map(constraint -> {
                        if (constraint.toLowerCase().contains("unique")) {
                            return String.format("ADD CONSTRAINT %s_unique UNIQUE (%s)", columnName, columnName);
                        } else if (constraint.equalsIgnoreCase("null")) {
                            return String.format("ALTER COLUMN %s DROP NOT NULL", columnName);
                        } else {
                            return String.format("ALTER COLUMN %s SET %s", columnName, constraint);
                        }
                    })
                    .collect(Collectors.joining(",\n  ")));
        }
        result.append(";\n");

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
