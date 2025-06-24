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
     * @param dataType the data type
     * @return the ID column definition
     */
    String generateIdColumnDefinition(String columnName, String dataType);
    
    /**
     * Generate CREATE INDEX statement
     * 
     * @param columnName the column name
     * @param tableName the table name
     * @param isUnique whether the index should be unique
     * @return the CREATE INDEX statement
     */
    String generateCreateIndex(String columnName, String tableName, boolean isUnique);
    
    /**
     * Generate DROP INDEX statement
     * 
     * @param columnName the column name
     * @param tableName the table name
     * @return the DROP INDEX statement
     */
    String generateDropIndex(String columnName, String tableName);
    
    /**
     * Generate RENAME COLUMN statement
     * 
     * @param columnName the column name
     * @param tableName the table name
     * @return the RENAME COLUMN statement
     */
    String generateRenameColumn(String columnName, String tableName);
    
    /**
     * Generate ADD COLUMN statement
     * 
     * @param columnName the column name
     * @param dataType the data type
     * @param constraints the column constraints
     * @param tableName the table name
     * @return the ADD COLUMN statement
     */
    String generateAddColumn(String columnName, String dataType, List<String> constraints, String tableName);
    
    /**
     * Generate DROP COLUMN statement
     * 
     * @param columnName the column name
     * @param tableName the table name
     * @return the DROP COLUMN statement
     */
    String generateDropColumn(String columnName, String tableName);
    
    /**
     * Generate MODIFY COLUMN statement
     * 
     * @param columnName the column name
     * @param dataType the data type
     * @param constraints the column constraints
     * @param tableName the table name
     * @return the MODIFY COLUMN statement
     */
    String generateModifyColumn(String columnName, String dataType, List<String> constraints, String tableName);
    
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
