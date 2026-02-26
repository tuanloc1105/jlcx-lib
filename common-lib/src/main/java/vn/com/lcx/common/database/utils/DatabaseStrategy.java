package vn.com.lcx.common.database.utils;

import java.util.List;

/**
 * Strategy interface for database-specific operations
 */
public interface DatabaseStrategy {

    /**
     * Generate ID column definition for the specified database
     *
     * @param columnName the column name
     * @param dataType   the data type
     * @return the ID column definition
     */
    String generateIdColumnDefinition(String tableName, String columnName, String dataType);

    /**
     * Generate CREATE INDEX statement
     *
     * @param indexName         the full index name (e.g. "COLUMN_INDEX" or "my_composite_INDEX")
     * @param tableName         the fully-qualified table name
     * @param columnExpression  the column(s) to index, comma-separated (e.g. "col1" or "col1, col2")
     * @param isUnique          whether the index should be unique
     * @return the CREATE INDEX statement
     */
    String generateCreateIndex(String indexName, String tableName, String columnExpression, boolean isUnique);

    /**
     * Generate DROP INDEX statement
     *
     * @param indexName  the full index name
     * @param tableName  the table name (needed by MySQL and MSSQL syntax)
     * @return the DROP INDEX statement
     */
    String generateDropIndex(String indexName, String tableName);

    /**
     * Generate RENAME COLUMN statement
     *
     * @param columnName the column name
     * @param tableName  the table name
     * @return the RENAME COLUMN statement
     */
    String generateRenameColumn(String columnName, String tableName);

    /**
     * Generate ADD COLUMN statement
     *
     * @param columnDefinition the column definition
     * @param tableName        the table name
     * @return the ADD COLUMN statement
     */
    String generateAddColumn(ColumnDefinition columnDefinition, String tableName);

    /**
     * Generate DROP COLUMN statement
     *
     * @param columnName the column name
     * @param tableName  the table name
     * @return the DROP COLUMN statement
     */
    String generateDropColumn(String columnName, String tableName);

    /**
     * Generate MODIFY COLUMN statement
     *
     * @param columnDefinition the column definition
     * @param tableName        the table name
     * @return the MODIFY COLUMN statement
     */
    String generateModifyColumn(ColumnDefinition columnDefinition, String tableName);

    /**
     * Generate sequence statement (if applicable)
     *
     * @param tableName the table name
     * @return the sequence statement
     */
    String generateSequenceStatement(String tableName);

    /**
     * Generate foreign key cascade options
     *
     * @param cascade whether cascade should be enabled
     * @return the cascade options
     */
    String generateForeignKeyCascade(boolean cascade);
}
