package vn.com.lcx.common.database.utils;

/**
 * Factory for creating database strategies
 */
public class DatabaseStrategyFactory {
    
    /**
     * Create a database strategy based on the database type
     * 
     * @param databaseType the type of database (postgresql, mysql, mssql, oracle)
     * @return the appropriate database strategy
     */
    public static DatabaseStrategy createStrategy(String databaseType) {
        switch (databaseType.toLowerCase()) {
            case "postgresql":
                return new PostgreSQLStrategy();
            case "mysql":
                return new MySQLStrategy();
            case "mssql":
                return new MSSQLStrategy();
            case "oracle":
            default:
                return new OracleStrategy();
        }
    }
} 
