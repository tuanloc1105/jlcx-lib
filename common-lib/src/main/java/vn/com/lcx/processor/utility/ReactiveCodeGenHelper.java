package vn.com.lcx.processor.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Helper class for generating reactive repository code.
 * Provides reusable code generation patterns to eliminate duplication.
 */
public final class ReactiveCodeGenHelper {

    private ReactiveCodeGenHelper() {
        // Utility class
    }

    /**
     * Add code to resolve the database placeholder based on database name.
     * Generates the switch statement for placeholder resolution.
     */
    public static void addPlaceholderResolution(List<String> codeLines) {
        codeLines.add("String placeholder;");
        codeLines.add("if (databaseName.equals(\"PostgreSQL\")) {");
        codeLines.add("    placeholder = \"$\";");
        codeLines.add("} else if (databaseName.equals(\"MySQL\") || databaseName.equals(\"MariaDB\")) {");
        codeLines.add("    placeholder = \"?\";");
        codeLines.add("} else if (databaseName.equals(\"Microsoft SQL Server\")) {");
        codeLines.add("    placeholder = \"@p\";");
        codeLines.add("} else if (databaseName.equals(\"Oracle\")) {");
        codeLines.add("    placeholder = \"?\";");
        codeLines.add("} else {");
        codeLines.add("    throw new vn.com.lcx.jpa.exception.CodeGenError(\"Unsupported database type\");");
        codeLines.add("}");
    }

    /**
     * Add starting time recording code for duration measurement.
     */
    public static void addStartingTimeCode(List<String> codeLines) {
        codeLines.add("final double startingTime = (double) java.lang.System.currentTimeMillis();");
    }

    /**
     * Add duration logging code.
     *
     * @param codeLines         the list to add code lines to
     * @param contextVarName    the name of the RoutingContext variable
     * @param indentSpaces      number of spaces for indentation
     */
    public static void addDurationLogging(List<String> codeLines, String contextVarName, int indentSpaces) {
        String indent = " ".repeat(indentSpaces);
        codeLines.add(indent + "final double duration = ((double) java.lang.System.currentTimeMillis()) - startingTime;");
        codeLines.add(indent + "vn.com.lcx.common.utils.LogUtils.writeLog(this.getClass(), " + contextVarName +
                ", vn.com.lcx.common.utils.LogUtils.Level.TRACE, \"Executed SQL in {} ms\", duration);");
    }

    /**
     * Add duration logging code with no indentation.
     */
    public static void addDurationLogging(List<String> codeLines, String contextVarName) {
        addDurationLogging(codeLines, contextVarName, 0);
    }

    /**
     * Get placeholder string for a specific database.
     */
    public static String getPlaceholderForDatabase(String databaseName) {
        return DatabasePlaceholder.fromDatabaseName(databaseName).getPlaceholder();
    }

    /**
     * Generate database-specific operation code for save/update/delete.
     *
     * @param sqlConnectionVar the SqlConnection variable name
     * @param contextVar       the RoutingContext variable name
     * @param entityType       the entity type mirror as string
     * @param operationType    "insert", "update", or "delete"
     * @return list of code lines for the database-specific operation
     */
    public static List<String> generateDatabaseSpecificCrudCode(
            String sqlConnectionVar,
            String contextVar,
            String entityType,
            CrudOperationType operationType) {

        List<String> codeLines = new ArrayList<>();

        switch (operationType) {
            case INSERT:
                generateInsertCode(codeLines, sqlConnectionVar, contextVar, entityType);
                break;
            case UPDATE:
                generateUpdateCode(codeLines, sqlConnectionVar, contextVar, entityType);
                break;
            case DELETE:
                generateDeleteCode(codeLines, sqlConnectionVar, contextVar, entityType);
                break;
        }

        return codeLines;
    }

    private static void generateInsertCode(List<String> codeLines, String sqlConnectionVar, String contextVar, String entityType) {
        addStartingTimeCode(codeLines);
        codeLines.add("if (databaseName.equals(\"PostgreSQL\")) {");
        codeLines.add(String.format("    return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveInsertStatement(model, \"$\") + \" returning \" + %3$sUtils.idColumnName())",
                sqlConnectionVar, contextVar, entityType));
        codeLines.add(String.format("            .execute(%sUtils.insertTupleParam(model))", entityType));
        codeLines.add("            .map(rowSet -> {");
        codeLines.add("                for (io.vertx.sqlclient.Row row : rowSet) {");
        codeLines.add("                    " + entityType + "Utils.idRowExtract(row, model);");
        codeLines.add("                }");
        addDurationLogging(codeLines, contextVar, 16);
        codeLines.add("                return model;");
        codeLines.add("            });");

        // MySQL/MariaDB
        codeLines.add("} else if (databaseName.equals(\"MySQL\") || databaseName.equals(\"MariaDB\")) {");
        codeLines.add(String.format("    return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveInsertStatement(model, \"?\"))",
                sqlConnectionVar, contextVar, entityType));
        codeLines.add(String.format("            .execute(%sUtils.insertTupleParam(model))", entityType));
        codeLines.add("            .map(rowSet -> {");
        codeLines.add("                " + entityType + "Utils.mySqlIdRowExtract(rowSet, model);");
        addDurationLogging(codeLines, contextVar, 16);
        codeLines.add("                return model;");
        codeLines.add("            });");

        // MSSQL
        codeLines.add("} else if (databaseName.equals(\"Microsoft SQL Server\")) {");
        codeLines.add(String.format("    return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveInsertStatement(model, \"@p\"))",
                sqlConnectionVar, contextVar, entityType));
        codeLines.add(String.format("            .execute(%sUtils.insertTupleParam(model))", entityType));
        codeLines.add("            .map(rowSet -> {");
        codeLines.add("                " + entityType + "Utils.idRowExtract(rowSet.iterator().next(), model);");
        addDurationLogging(codeLines, contextVar, 16);
        codeLines.add("                return model;");
        codeLines.add("            });");

        // Oracle
        codeLines.add("} else if (databaseName.equals(\"Oracle\")) {");
        codeLines.add(String.format("    return vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveInsertStatement(model, \"?\"), new io.vertx.oracleclient.OraclePrepareOptions().setAutoGeneratedKeysIndexes(new io.vertx.core.json.JsonArray().add(%3$sUtils.idColumnName())))",
                sqlConnectionVar, contextVar, entityType));
        codeLines.add(String.format("            .execute(%sUtils.insertTupleParam(model))", entityType));
        codeLines.add("            .map(rowSet -> {");
        codeLines.add("                io.vertx.sqlclient.Row row = rowSet.property(io.vertx.oracleclient.OracleClient.GENERATED_KEYS);");
        codeLines.add("                " + entityType + "Utils.idRowExtract(row, model);");
        addDurationLogging(codeLines, contextVar, 16);
        codeLines.add("                return model;");
        codeLines.add("            });");

        codeLines.add("} else {");
        codeLines.add("    throw new vn.com.lcx.jpa.exception.CodeGenError(\"Unsupported database type\");");
        codeLines.add("}");
    }

    private static void generateUpdateCode(List<String> codeLines, String sqlConnectionVar, String contextVar, String entityType) {
        addStartingTimeCode(codeLines);
        codeLines.add("if (databaseName.equals(\"PostgreSQL\")) {");
        codeLines.add(String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveUpdateStatement(model, \"$\"))",
                sqlConnectionVar, contextVar, entityType));
        codeLines.add(String.format("            .execute(%sUtils.updateTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityType));

        codeLines.add("} else if (databaseName.equals(\"MySQL\") || databaseName.equals(\"MariaDB\")) {");
        codeLines.add(String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveUpdateStatement(model, \"?\"))",
                sqlConnectionVar, contextVar, entityType));
        codeLines.add(String.format("            .execute(%sUtils.updateTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityType));

        codeLines.add("} else if (databaseName.equals(\"Microsoft SQL Server\")) {");
        codeLines.add(String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveUpdateStatement(model, \"@p\"))",
                sqlConnectionVar, contextVar, entityType));
        codeLines.add(String.format("            .execute(%sUtils.updateTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityType));

        codeLines.add("} else if (databaseName.equals(\"Oracle\")) {");
        codeLines.add(String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveUpdateStatement(model, \"?\"))",
                sqlConnectionVar, contextVar, entityType));
        codeLines.add(String.format("            .execute(%sUtils.updateTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityType));

        codeLines.add("} else {");
        codeLines.add("    throw new vn.com.lcx.jpa.exception.CodeGenError(\"Unsupported database type\");");
        codeLines.add("}");

        // Add return with logging
        codeLines.add("return future.map(it -> {");
        addDurationLogging(codeLines, contextVar, 4);
        codeLines.add("    return it;");
        codeLines.add("});");
    }

    private static void generateDeleteCode(List<String> codeLines, String sqlConnectionVar, String contextVar, String entityType) {
        addStartingTimeCode(codeLines);
        codeLines.add("if (databaseName.equals(\"PostgreSQL\")) {");
        codeLines.add(String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveDeleteStatement(model, \"$\") + \" returning \" + %3$sUtils.idColumnName())",
                sqlConnectionVar, contextVar, entityType));
        codeLines.add(String.format("            .execute(%sUtils.deleteTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityType));

        codeLines.add("} else if (databaseName.equals(\"MySQL\") || databaseName.equals(\"MariaDB\")) {");
        codeLines.add(String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveDeleteStatement(model, \"?\"))",
                sqlConnectionVar, contextVar, entityType));
        codeLines.add(String.format("            .execute(%sUtils.deleteTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityType));

        codeLines.add("} else if (databaseName.equals(\"Microsoft SQL Server\")) {");
        codeLines.add(String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveDeleteStatement(model, \"@p\"))",
                sqlConnectionVar, contextVar, entityType));
        codeLines.add(String.format("            .execute(%sUtils.deleteTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityType));

        codeLines.add("} else if (databaseName.equals(\"Oracle\")) {");
        codeLines.add(String.format("    future = vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(%1$s, %2$s).preparedQuery(%3$sUtils.reactiveDeleteStatement(model, \"?\"))",
                sqlConnectionVar, contextVar, entityType));
        codeLines.add(String.format("            .execute(%sUtils.deleteTupleParam(model)).map(io.vertx.sqlclient.SqlResult::rowCount);", entityType));

        codeLines.add("} else {");
        codeLines.add("    throw new vn.com.lcx.jpa.exception.CodeGenError(\"Unsupported database type\");");
        codeLines.add("}");

        // Add return with logging
        codeLines.add("return future.map(it -> {");
        addDurationLogging(codeLines, contextVar, 4);
        codeLines.add("    return it;");
        codeLines.add("});");
    }

    /**
     * Returns a sublist starting from the first element
     * that contains the keyword (case-insensitive) until the end of the list.
     *
     * @param keywords the list of keywords to search in
     * @param keyword  the keyword to search for (case-insensitive)
     * @return a sublist of keywords from the found index to the end,
     *         or an empty list if no match is found
     */
    public static List<String> subListFromKeyword(List<String> keywords, String keyword) {
        if (keywords == null || keyword == null) {
            return Collections.emptyList();
        }

        int index = IntStream.range(0, keywords.size())
                .filter(i -> keywords.get(i).toLowerCase().contains(keyword.toLowerCase()))
                .findFirst()
                .orElse(-1);

        return index != -1
                ? keywords.subList(index, keywords.size())
                : Collections.emptyList();
    }

    /**
     * Enum for CRUD operation types.
     */
    public enum CrudOperationType {
        INSERT,
        UPDATE,
        DELETE
    }
}
