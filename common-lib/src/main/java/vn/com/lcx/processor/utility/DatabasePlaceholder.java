package vn.com.lcx.processor.utility;

import java.util.Arrays;

/**
 * Enum representing database-specific placeholder patterns for reactive SQL queries.
 * Eliminates repeated if-else chains for database type checking across the codebase.
 */
public enum DatabasePlaceholder {
    POSTGRESQL("PostgreSQL", "$", true),
    MYSQL("MySQL", "?", false),
    MARIADB("MariaDB", "?", false),
    MSSQL("Microsoft SQL Server", "@p", true),
    ORACLE("Oracle", "?", false);

    private final String databaseName;
    private final String placeholder;
    private final boolean requiresIndex;

    DatabasePlaceholder(String databaseName, String placeholder, boolean requiresIndex) {
        this.databaseName = databaseName;
        this.placeholder = placeholder;
        this.requiresIndex = requiresIndex;
    }

    /**
     * Get the database product name as returned by JDBC/Vert.x.
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Get the placeholder prefix for this database.
     * For PostgreSQL: "$", MySQL/MariaDB/Oracle: "?", MSSQL: "@p"
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * Whether this database requires numbered placeholders (e.g., $1, $2 or @p1, @p2).
     */
    public boolean requiresIndex() {
        return requiresIndex;
    }

    /**
     * Find DatabasePlaceholder by database name.
     *
     * @param databaseName the database product name
     * @return the matching DatabasePlaceholder
     * @throws IllegalArgumentException if no match found
     */
    public static DatabasePlaceholder fromDatabaseName(String databaseName) {
        return Arrays.stream(values())
                .filter(dp -> dp.databaseName.equals(databaseName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported database: " + databaseName));
    }

    /**
     * Check if the given database name is supported.
     */
    public static boolean isSupported(String databaseName) {
        return Arrays.stream(values())
                .anyMatch(dp -> dp.databaseName.equals(databaseName));
    }

    /**
     * Get placeholder with optional index suffix.
     *
     * @param index the parameter index (1-based)
     * @return formatted placeholder like "$1", "?", or "@p1"
     */
    public String getPlaceholderWithIndex(int index) {
        if (requiresIndex) {
            return placeholder + index;
        }
        return placeholder;
    }
}
