package vn.com.lcx.common.database.utils;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.utils.FileUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static vn.com.lcx.common.utils.FileUtils.writeContentToFile;

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
            context.getIndexMap().forEach(
                    (indexName, columnList) -> {
                        final var columnListJoin = String.join(", ", columnList);
                        String createIndexTable = databaseStrategy.generateCreateIndex(columnListJoin, context.getFinalTableName(), false);
                        String dropIndexTable = databaseStrategy.generateDropIndex(indexName, context.getFinalTableName());
                        if (StringUtils.isNotBlank(createIndexTable)) {
                            context.getCreateIndexList().add(
                                    createIndexTable
                                            .replace(columnListJoin + "_INDEX", indexName)
                            );
                        }
                        if (StringUtils.isNotBlank(dropIndexTable)) {
                            context.getDropIndexList().add(dropIndexTable);
                        }
                    }
            );
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
        writeContentToFile(FileUtils.pathJoining(context.getFolderPath(), fileName), sqlContent);
    }

    private String buildFileName() {
        String schema = context.getTableNameAnnotation().schema();
        String tableName = context.getTableNameAnnotation().value();
        String prefix = StringUtils.isNotBlank(schema) ? schema.toLowerCase() + '-' : "";
        return prefix + tableName.toLowerCase() + ".sql";
    }
} 
