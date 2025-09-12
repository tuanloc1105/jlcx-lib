package vn.com.lcx.common.database.utils;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.TableName;
import vn.com.lcx.common.constant.CommonConstant;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static vn.com.lcx.common.utils.WordCaseUtils.convertCamelToConstant;

public final class EntityUtils {

    private EntityUtils() {
    }

    public static String getColumnNameFromFieldName(String fieldName, Class<?> entityClass) {
        TableName tableNameAnnotation = entityClass.getAnnotation(TableName.class);
        if (tableNameAnnotation == null) {
            throw new IllegalArgumentException(entityClass.getName() + " must has @TableName annotation");
        }
        List<Field> entityClassFields = new ArrayList<>(Arrays.asList(entityClass.getDeclaredFields()));
        for (Field field : entityClassFields) {
            if (field.getName().equals(fieldName)) {
                ColumnName columnNameAnnotation = field.getAnnotation(ColumnName.class);
                if (columnNameAnnotation != null && StringUtils.isNotBlank(columnNameAnnotation.name())) {
                    return columnNameAnnotation.name();
                } else {
                    return convertCamelToConstant(field.getName());
                }
            }
        }
        return null;
    }

    public static String getTableShortenedName(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            throw new IllegalArgumentException("table name is empty");
        }
        if (tableName.contains(" ")) {
            throw new IllegalArgumentException("table name contains spaces");
        }
        final var finalTableName = (tableName.contains(".") ? tableName.substring(tableName.lastIndexOf(".") + 1) : tableName).toLowerCase();
        final var firstCharArr = new ArrayList<String>();

        final var tableNameArr = Arrays.asList(finalTableName.split("_"));
        tableNameArr.forEach(word -> firstCharArr.add(String.valueOf(word.charAt(0))));
        return String.join(CommonConstant.EMPTY_STRING, firstCharArr);
    }

    public static String getTableShortenedName(Class<?> entityClass) {
        final var tableNameAnnotation = entityClass.getAnnotation(TableName.class);

        if (tableNameAnnotation == null) {
            throw new IllegalArgumentException(String.format("%s must be annotated with @TableName", entityClass.getName()));
        }
        return getTableShortenedName(tableNameAnnotation.value());
    }

    public static void analyzeEntityClass(Class<?> entityClass, String databaseType, String folderPath) {
        try {
            EntityAnalysisContext context = new EntityAnalysisContext(entityClass, databaseType, folderPath);
            EntityAnalyzer analyzer = new EntityAnalyzer(context);
            analyzer.analyze();
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze entity class: " + entityClass.getName(), e);
        }
    }

}
