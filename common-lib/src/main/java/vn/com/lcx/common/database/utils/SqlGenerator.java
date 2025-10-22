package vn.com.lcx.common.database.utils;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.common.utils.MyStringUtils;

import java.time.format.DateTimeFormatter;

import static vn.com.lcx.common.database.utils.DBEntityAnalysis.CREATE_TABLE_TEMPLATE_NO_PRIMARY_KEY;

/**
 * SQL generator for creating the final SQL file
 */
public class SqlGenerator {
    private final EntityAnalysisContext context;

    public SqlGenerator(EntityAnalysisContext context) {
        this.context = context;
    }

    public String generate() {
        String generatedTime = DateTimeUtils.generateCurrentTimeDefault()
                .format(DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN));

        return String.format(
                "-- GENERATED AT %s BY LCX+LIB V2\n" +
                        "\n" +
                        "-- ################# CREATE INDEX ####################### --\n" +
                        "\n" +
                        "%s\n" +
                        "-- ################# DROP INDEX ####################### --\n" +
                        "\n" +
                        "%s\n" +
                        "-- ################# ADD COLUMN ####################### --\n" +
                        "\n" +
                        "%s\n" +
                        "-- ################# DROP COLUMN ####################### --\n" +
                        "\n" +
                        "%s\n" +
                        "-- ################# MODIFY COLUMN ####################### --\n" +
                        "\n" +
                        "%s\n" +
                        "-- ################# RENAME COLUMN ####################### --\n" +
                        "\n" +
                        "%s\n" +
                        "-- ################# CREATE TABLE ####################### --\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "-- ################# FOREIGN KEY ####################### --\n" +
                        "\n" +
                        "%s",
                generatedTime,
                String.join(System.lineSeparator(), context.getCreateIndexList()),
                String.join(System.lineSeparator(), context.getDropIndexList()),
                String.join(System.lineSeparator(), context.getAlterAddColumnList()),
                String.join(System.lineSeparator(), context.getAlterDropColumnList()),
                String.join(System.lineSeparator(), context.getAlterModifyColumnList()),
                String.join(System.lineSeparator(), context.getRenameColumnList()),
                (StringUtils.isBlank(context.getCreateSequenceStatement()) ? "" : "\n" + context.getCreateSequenceStatement() + "\n"),
                String.format(
                        CREATE_TABLE_TEMPLATE_NO_PRIMARY_KEY,
                        context.getFinalTableName(),
                        MyStringUtils.formatStringSpace2(context.getColumnDefinitionLines(), ",\n    ")
                ),
                String.format(
                        DBEntityAnalysis.DROP_TABLE_TEMPLATE_NO_PRIMARY_KEY,
                        context.getFinalTableName()
                ),
                String.format(
                        DBEntityAnalysis.TRUNCATE_TABLE_TEMPLATE_NO_PRIMARY_KEY,
                        context.getFinalTableName()
                ),
                String.join(System.lineSeparator(), context.getAddForeignKeyList())
        );
    }
} 
