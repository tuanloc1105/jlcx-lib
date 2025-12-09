package vn.com.lcx.common.database.utils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Oracle implementation of DatabaseStrategy
 */
public class OracleStrategy implements DatabaseStrategy {

    @Override
    public String generateIdColumnDefinition(String tableName, String columnName, String dataType) {
        return String.format("%s NUMBER(18) DEFAULT %s_SEQ.nextval NOT NULL PRIMARY KEY", columnName, tableName);
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

        String result = String.format("ALTER TABLE %s\n  ADD (%s %s%s);\n",
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
        result += String.format("ALTER TABLE %s\n  DROP CONSTRAINT %s_unique;\n", tableName, columnName);
        return result;
    }

    @Override
    public String generateModifyColumn(ColumnDefinition columnDefinition, String tableName) {
        StringBuilder constraints = new StringBuilder();
        if (columnDefinition.getDefaultValue() != null && !columnDefinition.getDefaultValue().isEmpty()) {
            constraints.append(" DEFAULT ").append(columnDefinition.getDefaultValue());
        }

        // Basic modify for type and default
        StringBuilder result = new StringBuilder();
        result.append(String.format("ALTER TABLE %s\n  MODIFY (%s %s%s);\n",
                tableName, columnDefinition.getColumnName(), columnDefinition.getDataType(), constraints));

        // Handle NULL/NOT NULL separately as Oracle Modify syntax allows specifying it
        // to change state
        if (columnDefinition.isNullable()) {
            result.append(String.format("ALTER TABLE %s\n  MODIFY (%s NULL);\n", tableName,
                    columnDefinition.getColumnName()));
        } else {
            result.append(String.format("ALTER TABLE %s\n  MODIFY (%s NOT NULL);\n", tableName,
                    columnDefinition.getColumnName()));
        }

        // Handle UNIQUE constraint (Add if unique)
        // Note: Removing unique is harder without knowing constraint name, but here we
        // only add if requested.
        if (columnDefinition.isUnique()) {
            result.append(String.format("ALTER TABLE %s\n  ADD CONSTRAINT %s_unique UNIQUE (%s);\n",
                    tableName, columnDefinition.getColumnName(), columnDefinition.getColumnName()));
        }

        return result.toString();
    }

    @Override
    public String generateSequenceStatement(String tableName) {
        return String.format(
                "CREATE SEQUENCE %1$s_SEQ START WITH 1 INCREMENT BY 1 CACHE 20;\n" +
                        "-- SELECT %1$s_SEQ.NEXTVAL FROM dual;\n" +
                        "-- SELECT %1$s_SEQ.CURRVAL FROM dual;\n" +
                        "-- DROP SEQUENCE %1$s_SEQ;",
                tableName);
    }

    @Override
    public String generateForeignKeyCascade(boolean cascade) {
        return cascade ? "\nON DELETE CASCADE;" : ";";
    }
}
