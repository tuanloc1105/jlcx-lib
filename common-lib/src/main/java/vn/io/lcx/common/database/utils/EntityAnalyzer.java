package vn.io.lcx.common.database.utils;

import org.apache.commons.lang3.StringUtils;
import vn.io.lcx.common.annotation.ColumnName;
import vn.io.lcx.common.utils.FileUtils;
import vn.io.lcx.common.utils.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static vn.io.lcx.common.utils.FileUtils.writeContentToFile;

/**
 * Main analyzer class using Strategy Pattern
 */
public class EntityAnalyzer {
    private final EntityAnalysisContext context;
    private final DatabaseStrategy databaseStrategy;
    private boolean generatedTableIndex = false;

    public EntityAnalyzer(EntityAnalysisContext context) {
        this.context = context;
        this.databaseStrategy = DatabaseStrategyFactory.createStrategy(context.getDatabaseType());
    }

    public void analyze() {
        processFields();
        generateSqlFile();
    }

    private void processFields() {
        for (Field field : context.getEntityFields()) {
            if (isValidField(field)) {
                FieldProcessor processor = new FieldProcessor(context, databaseStrategy);
                processor.processField(field);
            }
        }
        if (!generatedTableIndex) {
            for (IndexInfo indexInfo : context.getTableIndexes()) {
                final var columnExpression = String.join(", ", indexInfo.getColumns());
                final var fullIndexName = indexInfo.getName() + "_INDEX";
                String createIndexTable = databaseStrategy.generateCreateIndex(
                        fullIndexName, context.getFinalTableName(), columnExpression, indexInfo.isUnique());
                String dropIndexTable = databaseStrategy.generateDropIndex(
                        fullIndexName, context.getFinalTableName());
                if (StringUtils.isNotBlank(createIndexTable)) {
                    context.getCreateIndexList().add(createIndexTable);
                }
                if (StringUtils.isNotBlank(dropIndexTable)) {
                    context.getDropIndexList().add(dropIndexTable);
                }
            }
            generatedTableIndex = true;
        }
    }

    private boolean isValidField(Field field) {
        return !Modifier.isFinal(field.getModifiers()) &&
                !Modifier.isStatic(field.getModifiers()) &&
                field.getAnnotation(ColumnName.class) != null &&
                StringUtils.isNotBlank(field.getAnnotation(ColumnName.class).name()) &&
                !field.getType().isPrimitive();
    }

    private void generateSqlFile() {
        String sqlContent = new SqlGenerator(context).generate();
        String fileName = buildFileName();
        final var exportSuccess = writeContentToFile(FileUtils.pathJoining(context.getFolderPath(), fileName), sqlContent);
        if (exportSuccess) {
            LogUtils.writeLog(this.getClass(), LogUtils.Level.DEBUG, "Exported sql script at path {}", fileName);
        }
    }

    private String buildFileName() {
        String schema = context.getTableNameAnnotation().schema();
        String tableName = context.getTableNameAnnotation().value();
        String prefix = StringUtils.isNotBlank(schema) ? schema.toLowerCase() + '-' : "";
        return prefix + tableName.toLowerCase() + ".sql";
    }
}
