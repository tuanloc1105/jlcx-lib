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
            processIdField(columnName, field.getType().getName(), field.getType());
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

    private void processIdField(String columnName, String fieldTypeName, Class<?> fieldType) {
        if (fieldType == Long.class || fieldType == long.class ||
                java.math.BigDecimal.class.isAssignableFrom(fieldType) ||
                java.math.BigInteger.class.isAssignableFrom(fieldType)) {

            String idDefinition = databaseStrategy.generateIdColumnDefinition(context.getFinalTableName(), columnName,
                    fieldTypeName);
            final var definitionLineParts = new ArrayList<String>();
            definitionLineParts.add(columnName);
            definitionLineParts.addAll(
                    Arrays.stream(idDefinition.split(" ")).collect(Collectors.toCollection(ArrayList::new)));
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
        ColumnDefinition columnDefinition = buildColumnDefinition(columnName, dataType, columnNameAnnotation);

        // Add column definition for CREATE TABLE (still using string list for now to
        // minimize scope)
        List<String> columnDefinitionList = new ArrayList<>();
        columnDefinitionList.add(columnName);
        columnDefinitionList.add(dataType);
        if (columnDefinition.getDefaultValue() != null && !columnDefinition.getDefaultValue().isEmpty()) {
            columnDefinitionList.add("DEFAULT " + columnDefinition.getDefaultValue());
        }
        if (!columnDefinition.isNullable())
            columnDefinitionList.add("NOT NULL");
        else
            columnDefinitionList.add("NULL");
        if (columnDefinition.isUnique())
            columnDefinitionList.add("UNIQUE");

        context.getColumnDefinitionLines().add(columnDefinitionList);

        // Process index
        if (columnNameAnnotation.index()) {
            processIndex(columnName, columnNameAnnotation.unique());
        }

        // Process foreign key
        processForeignKey(field, columnName);

        // Generate ALTER statements
        generateAlterStatements(columnDefinition);
    }

    private ColumnDefinition buildColumnDefinition(String columnName, String dataType, ColumnName annotation) {
        boolean isNullable = annotation.nullable();
        String defaultValue = StringUtils.isNotBlank(annotation.defaultValue()) ? annotation.defaultValue() : null;
        boolean isUnique = annotation.unique();

        // Handle constraint logic from annotation
        if (!isNullable && defaultValue != null) {
            // Logic: if not null and has default
        }

        return new ColumnDefinition(columnName, dataType, isNullable, defaultValue, isUnique);
    }

    private void processIndex(String columnName, boolean isUnique) {
        String indexName = columnName + "_INDEX";
        String createIndex = databaseStrategy.generateCreateIndex(indexName, context.getFinalTableName(), columnName, isUnique);
        String dropIndex = databaseStrategy.generateDropIndex(indexName, context.getFinalTableName());

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
                    context.getTableNameAnnotation().value());

            foreignKeyStatement += databaseStrategy.generateForeignKeyCascade(cascade) + "\n";
            context.getAddForeignKeyList().add(foreignKeyStatement);
        }
    }

    private void generateAlterStatements(ColumnDefinition columnDefinition) {
        String renameColumn = databaseStrategy.generateRenameColumn(columnDefinition.getColumnName(),
                context.getFinalTableName());
        String alterAddColumn = databaseStrategy.generateAddColumn(columnDefinition, context.getFinalTableName());
        String alterDropColumn = databaseStrategy.generateDropColumn(columnDefinition.getColumnName(),
                context.getFinalTableName());
        String alterModifyColumn = databaseStrategy.generateModifyColumn(columnDefinition, context.getFinalTableName());

        context.getRenameColumnList().add(renameColumn);
        context.getAlterAddColumnList().add(alterAddColumn);
        context.getAlterDropColumnList().add(alterDropColumn);
        context.getAlterModifyColumnList().add(alterModifyColumn);
    }
}
