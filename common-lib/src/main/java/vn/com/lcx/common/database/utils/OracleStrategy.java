package vn.com.lcx.common.database.utils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Oracle implementation of DatabaseStrategy
 */
public class OracleStrategy implements DatabaseStrategy {
    
    @Override
    public String generateIdColumnDefinition(String columnName, String dataType) {
        return String.format("%s NUMBER(18) DEFAULT %s_SEQ.nextval NOT NULL PRIMARY KEY", columnName, columnName);
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
        
        String result = String.format("ALTER TABLE %s\n  ADD (%s %s %s);\n", 
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
        result += String.format("ALTER TABLE %s\n  DROP CONSTRAINT %s_unique;\n", tableName, columnName);
        return result;
    }

    @Override
    public String generateModifyColumn(String columnName, String dataType, List<String> constraints, String tableName) {
        StringBuilder result = new StringBuilder();
        
        // Basic modify
        String basicConstraints = constraints.stream()
                .filter(c -> !c.equalsIgnoreCase("null") && !c.equalsIgnoreCase("unique"))
                .collect(Collectors.joining(" "));
        result.append(String.format("ALTER TABLE %s\n  MODIFY (%s %s %s);\n", 
                tableName, columnName, dataType, basicConstraints));
        
        // Handle NULL/NOT NULL separately
        if (constraints.stream().anyMatch(c -> c.equalsIgnoreCase("null"))) {
            result.append(String.format("ALTER TABLE %s\n  MODIFY (%s NULL);\n", tableName, columnName));
        }
        if (constraints.stream().anyMatch(c -> c.equalsIgnoreCase("not null"))) {
            result.append(String.format("ALTER TABLE %s\n  MODIFY (%s NOT NULL);\n", tableName, columnName));
        }
        
        // Handle UNIQUE constraint
        if (constraints.stream().anyMatch(c -> c.equalsIgnoreCase("unique"))) {
            result.append(String.format("ALTER TABLE %s\n  ADD CONSTRAINT %s_unique UNIQUE (%s);\n", 
                    tableName, columnName, columnName));
        }
        
        return result.toString();
    }

    @Override
    public String generateSequenceStatement(String tableName) {
        return String.format(
                "CREATE SEQUENCE %1$s_SEQ START WITH 1 INCREMENT BY 1 CACHE 20;\n" +
                "-- SELECT %1$s_SEQ.NEXTVAL FROM dual;\n" +
                "-- SELECT %1$s_SEQ.CURRVAL FROM dual;\n",
                tableName
        );
    }

    @Override
    public String generateForeignKeyCascade(boolean cascade) {
        return cascade ? "\nON DELETE CASCADE;" : ";";
    }
} 
