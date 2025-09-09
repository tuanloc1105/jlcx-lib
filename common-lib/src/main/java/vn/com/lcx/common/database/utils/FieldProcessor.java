package vn.com.lcx.common.database.utils;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.ForeignKey;
import vn.com.lcx.common.constant.CommonConstant;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Field processor for handling individual fields
 */
public class FieldProcessor {
    private final EntityAnalysisContext context;
    private final DatabaseStrategy databaseStrategy;

    public FieldProcessor(EntityAnalysisContext context, DatabaseStrategy databaseStrategy) {
        this.context = context;
        this.databaseStrategy = databaseStrategy;
    }

    public void processField(Field field) {
        ColumnName columnNameAnnotation = field.getAnnotation(ColumnName.class);
        String columnName = columnNameAnnotation.name();
        String dataType = getSqlDataType(field, columnNameAnnotation);

        if (isIdField(field)) {
            processIdField(columnName, field.getType().getName());
        } else {
            processRegularField(field, columnName, dataType);
        }
    }

    private boolean isIdField(Field field) {
        return context.getIdField() != null && context.getIdField().getName().equals(field.getName());
    }

    private String getSqlDataType(Field field, ColumnName columnNameAnnotation) {
        if (StringUtils.isNotBlank(columnNameAnnotation.columnDataTypeDefinition())) {
            return columnNameAnnotation.columnDataTypeDefinition();
        }

        if (field.getType().isEnum()) {
            return context.getDatabaseDatatypeMap().get("String");
        }

        String fieldTypeName = field.getType().getName();
        return context.getDatabaseDatatypeMap().entrySet().stream()
                .filter(entry -> fieldTypeName.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("VARCHAR(255)");
    }

    private void processIdField(String columnName, String fieldTypeName) {
        if (fieldTypeName.contains("Long") || fieldTypeName.contains("BigDecimal") || fieldTypeName.contains("BigInteger")) {
            String idDefinition = databaseStrategy.generateIdColumnDefinition(context.getFinalTableName(), columnName, fieldTypeName);
            final var definitionLineParts = new ArrayList<String>();
            definitionLineParts.add(columnName);
            definitionLineParts.addAll(
                    Arrays.stream(idDefinition.split(" ")).collect(Collectors.toCollection(ArrayList::new))
            );
            context.getColumnDefinitionLines().add(new ArrayList<>(new LinkedHashSet<>(definitionLineParts)));

            String sequenceStatement = databaseStrategy.generateSequenceStatement(context.getFinalTableName());
            if (StringUtils.isNotBlank(sequenceStatement)) {
                context.setCreateSequenceStatement(sequenceStatement);
            }
        } else {
            context.getColumnDefinitionLines().add(Arrays.asList(columnName, "VARCHAR(255)", "PRIMARY KEY"));
        }
    }

    private void processRegularField(Field field, String columnName, String dataType) {
        ColumnName columnNameAnnotation = field.getAnnotation(ColumnName.class);
        List<String> constraints = buildConstraints(columnNameAnnotation);

        // Add column definition
        List<String> columnDefinition = new ArrayList<>();
        columnDefinition.add(columnName);
        columnDefinition.add(dataType);
        columnDefinition.addAll(constraints);
        context.getColumnDefinitionLines().add(columnDefinition);

        // Process index
        if (columnNameAnnotation.index()) {
            processIndex(columnName, columnNameAnnotation.unique());
        }

        // Process foreign key
        processForeignKey(field, columnName);

        // Generate ALTER statements
        generateAlterStatements(columnName, dataType, constraints);
    }

    private List<String> buildConstraints(ColumnName columnNameAnnotation) {
        List<String> constraints = new ArrayList<>();

        if (columnNameAnnotation.nullable()) {
            if (StringUtils.isBlank(columnNameAnnotation.defaultValue())) {
                constraints.add("NULL");
            } else {
                constraints.add("DEFAULT " + columnNameAnnotation.defaultValue());
            }
        } else {
            if (StringUtils.isBlank(columnNameAnnotation.defaultValue())) {
                constraints.add("NOT NULL");
            } else {
                constraints.add("DEFAULT " + columnNameAnnotation.defaultValue());
                constraints.add("NOT NULL");
            }
            if (columnNameAnnotation.unique()) {
                constraints.add("UNIQUE");
            }
        }

        return constraints;
    }

    private void processIndex(String columnName, boolean isUnique) {
        String createIndex = databaseStrategy.generateCreateIndex(columnName, context.getFinalTableName(), isUnique);
        String dropIndex = databaseStrategy.generateDropIndex(columnName, context.getFinalTableName());

        if (StringUtils.isNotBlank(createIndex)) {
            context.getCreateIndexList().add(createIndex);
        }
        if (StringUtils.isNotBlank(dropIndex)) {
            context.getDropIndexList().add(dropIndex);
        }
    }

    private void processForeignKey(Field field, String columnName) {
        ForeignKey foreignKeyAnnotation = field.getAnnotation(ForeignKey.class);
        if (foreignKeyAnnotation != null) {
            String referenceColumn = foreignKeyAnnotation.referenceColumn();
            String referenceTable = foreignKeyAnnotation.referenceTable();
            boolean cascade = foreignKeyAnnotation.cascade();

            String schema = context.getTableNameAnnotation().schema();
            String schemaPrefix = StringUtils.isNotBlank(schema) ? schema + "." : CommonConstant.EMPTY_STRING;

            String foreignKeyStatement = String.format(
                    "ALTER TABLE\n" +
                            "    %1$s\n" +
                            "ADD\n" +
                            "    CONSTRAINT FK_%2$s_%7$s FOREIGN KEY (%3$s) REFERENCES %4$s%5$s(%6$s)",
                    context.getFinalTableName(),
                    String.format("%s_%s", referenceTable.toUpperCase(), referenceColumn.toUpperCase()),
                    columnName,
                    schemaPrefix,
                    referenceTable,
                    referenceColumn,
                    context.getTableNameAnnotation().value()
            );

            foreignKeyStatement += databaseStrategy.generateForeignKeyCascade(cascade) + "\n";
            context.getAddForeignKeyList().add(foreignKeyStatement);
        }
    }

    private void generateAlterStatements(String columnName, String dataType, List<String> constraints) {
        String renameColumn = databaseStrategy.generateRenameColumn(columnName, context.getFinalTableName());
        String alterAddColumn = databaseStrategy.generateAddColumn(columnName, dataType, constraints, context.getFinalTableName());
        String alterDropColumn = databaseStrategy.generateDropColumn(columnName, context.getFinalTableName());
        String alterModifyColumn = databaseStrategy.generateModifyColumn(columnName, dataType, constraints, context.getFinalTableName());

        context.getRenameColumnList().add(renameColumn);
        context.getAlterAddColumnList().add(alterAddColumn);
        context.getAlterDropColumnList().add(alterDropColumn);
        context.getAlterModifyColumnList().add(alterModifyColumn);
    }
} 
