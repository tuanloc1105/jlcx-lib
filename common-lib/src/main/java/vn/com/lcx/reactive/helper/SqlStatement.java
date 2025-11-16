package vn.com.lcx.reactive.helper;

import vn.com.lcx.common.cache.CacheUtils;
import vn.com.lcx.common.database.pageable.Pageable;
import vn.com.lcx.reactive.exception.EmptyConditionStatementException;
import vn.com.lcx.reactive.exception.EmptyFromStatementException;
import vn.com.lcx.reactive.exception.EmptyOrderStatementException;
import vn.com.lcx.reactive.exception.EmptySelectStatementException;

/**
 * <p>A fluent SQL builder class for constructing both a main SELECT query and its
 * corresponding COUNT(1) query in parallel.</p>
 *
 * <p>This class is designed for dynamic SQL generation in systems where query logic
 * (especially filtering and pagination) is frequently reused.</p>
 *
 * <h3>Main Features</h3>
 * <ul>
 *     <li>Builds SELECT and COUNT SQL statements simultaneously.</li>
 *     <li>Supports logic chaining: AND, OR, AND(...OR...), OR(...AND...).</li>
 *     <li>Optional caching to avoid rebuilding identical SQL structures.</li>
 *     <li>Fluent, chainable interface.</li>
 * </ul>
 *
 * <h3>Important Notes</h3>
 * <ul>
 *     <li>All SQL parameter placeholders must use <strong>#</strong>.
 *         Example: <code>column = #</code></li>
 *     <li>If caching is enabled and SQL exists for the given key, all method calls
 *         become no-op until finalize methods are called.</li>
 *     <li>This class assumes external classes such as CacheUtils, Pageable, and
 *         several custom exceptions.</li>
 * </ul>
 */
public class SqlStatement {

    /**
     * Global SQL cache with max size 1000.
     */
    private static final CacheUtils<String, String> sqlCache = CacheUtils.create(1000);

    /**
     * Builder for the SELECT query.
     */
    private final StringBuilder statement = new StringBuilder();

    /**
     * Builder for the COUNT query.
     */
    private final StringBuilder count = new StringBuilder();

    /**
     * Cache key name (null if cache disabled).
     */
    private final String cacheKeyName;
    /**
     * Whether caching is enabled.
     */
    private final boolean isUseCache;
    /**
     * Tracks whether WHERE has been called. Required before AND/OR.
     */
    private boolean containedWhere = false;

    /**
     * Private constructor used internally.
     *
     * @param cacheKeyName cache key (null if disabled)
     * @param isUseCache   whether this builder uses caching
     */
    private SqlStatement(String cacheKeyName,
                         boolean isUseCache) {
        this.cacheKeyName = cacheKeyName;
        this.isUseCache = isUseCache;
    }

    /**
     * Creates a new SqlStatement builder with caching disabled.
     *
     * @return a new {@code SqlStatement} instance
     */
    public static SqlStatement init() {
        return new SqlStatement(
                null,
                false
        );
    }

    /**
     * Creates a new SqlStatement builder with caching enabled.
     *
     * @param cacheKeyName unique cache key
     * @return a new SqlStatement with caching enabled
     * @throws NullPointerException if the key is null or blank
     */
    public static SqlStatement initWithCache(String cacheKeyName) {
        if (cacheKeyName == null || cacheKeyName.isBlank()) {
            throw new NullPointerException();
        }
        return new SqlStatement(
                cacheKeyName,
                true
        );
    }

    /**
     * Appends a SELECT clause.
     * <p><strong>Note:</strong> All SQL parameter placeholders must use "#".</p>
     *
     * @param columns one or more column names or expressions
     * @return this builder for chaining
     * @throws EmptySelectStatementException if no columns were supplied
     */
    public SqlStatement select(String... columns) {
        if (alreadyCached()) {
            return this;
        }
        if (columns.length == 0) {
            throw new EmptySelectStatementException();
        }
        count.append("SELECT\n    COUNT(1)");
        statement.append("SELECT");
        statement.append("\n    ").append(columns[0]);
        if (columns.length > 1) {
            for (int i = 1; i < columns.length; i++) {
                statement.append(",\n    ").append(columns[i]);
            }
        }
        return this;
    }

    /**
     * Appends a FROM clause to both SELECT and COUNT statements.
     *
     * @param tables one or more table names
     * @return this builder for chaining
     * @throws EmptyFromStatementException if no tables were provided
     */
    public SqlStatement from(String... tables) {
        if (alreadyCached()) {
            return this;
        }
        if (tables.length == 0) {
            throw new EmptyFromStatementException();
        }
        count.append("\nFROM");
        count.append("\n    ").append(tables[0]);
        statement.append("\nFROM");
        statement.append("\n    ").append(tables[0]);
        if (tables.length > 1) {
            for (int i = 1; i < tables.length; i++) {
                count.append("\n    ").append(tables[i]);
                statement.append("\n    ").append(tables[i]);
            }
        }
        return this;
    }

    /**
     * Adds a WHERE clause with a given condition.
     * <p>Example: <code>column = #</code></p>
     *
     * @param condition SQL condition text
     * @return this builder for chaining
     */
    public SqlStatement where(String condition) {
        if (alreadyCached()) {
            return this;
        }
        count.append("\nWHERE");
        count.append("\n    ").append(condition);
        statement.append("\nWHERE");
        statement.append("\n    ").append(condition);
        containedWhere = true;
        return this;
    }

    /**
     * Adds a basic WHERE 1 = 1 clause.
     *
     * @return this builder for chaining
     */
    public SqlStatement where() {
        if (alreadyCached()) {
            return this;
        }
        count.append("\nWHERE");
        count.append("\n    ").append("1 = 1");
        statement.append("\nWHERE");
        statement.append("\n    1 = 1");
        containedWhere = true;
        return this;
    }

    /**
     * Adds AND conditions to the WHERE clause.
     *
     * @param conditions one or more SQL conditions
     * @return this builder for chaining
     * @throws RuntimeException                 if WHERE was not called first
     * @throws EmptyConditionStatementException if no conditions were provided
     */
    public SqlStatement and(String... conditions) {
        if (alreadyCached()) {
            return this;
        }
        if (!containedWhere) {
            throw new RuntimeException("The `where` method has not been called yet");
        }
        if (conditions.length == 0) {
            throw new EmptyConditionStatementException();
        }
        if (conditions.length > 1) {
            count.append("\n    AND (").append("\n        ").append(conditions[0]);
            statement.append("\n    AND (").append("\n        ").append(conditions[0]);
            for (int i = 1; i < conditions.length; i++) {
                count.append("\n        AND ").append(conditions[i]);
                statement.append("\n        AND ").append(conditions[i]);
            }
            count.append("\n    )");
            statement.append("\n    )");
        } else {
            count.append("\n    AND ").append(conditions[0]);
            statement.append("\n    AND ").append(conditions[0]);
        }
        return this;
    }

    /**
     * Adds AND (cond1 OR cond2 ...) logic to the WHERE clause.
     *
     * @param conditions one or more SQL conditions
     * @return this builder for chaining
     * @throws RuntimeException                 if WHERE was not called first
     * @throws EmptyConditionStatementException if no conditions were provided
     */
    public SqlStatement andOr(String... conditions) {
        if (alreadyCached()) {
            return this;
        }
        if (!containedWhere) {
            throw new RuntimeException("The `where` method has not been called yet");
        }
        if (conditions.length == 0) {
            throw new EmptyConditionStatementException();
        }
        if (conditions.length > 1) {
            count.append("\n    AND (").append("\n        ").append(conditions[0]);
            statement.append("\n    AND (").append("\n        ").append(conditions[0]);
            for (int i = 1; i < conditions.length; i++) {
                count.append("\n        OR ").append(conditions[i]);
                statement.append("\n        OR ").append(conditions[i]);
            }
            count.append("\n    )");
            statement.append("\n    )");
        } else {
            count.append("\n    AND ").append(conditions[0]);
            statement.append("\n    AND ").append(conditions[0]);
        }
        return this;
    }

    /**
     * Adds OR conditions to the WHERE clause.
     *
     * @param conditions one or more SQL conditions
     * @return this builder for chaining
     * @throws RuntimeException                 if WHERE was not called first
     * @throws EmptyConditionStatementException if no conditions were provided
     */
    public SqlStatement or(String... conditions) {
        if (alreadyCached()) {
            return this;
        }
        if (!containedWhere) {
            throw new RuntimeException("The `where` method has not been called yet");
        }
        if (conditions.length == 0) {
            throw new EmptyConditionStatementException();
        }
        if (conditions.length > 1) {
            count.append("\n    OR (").append("\n        ").append(conditions[0]);
            statement.append("\n    OR (").append("\n        ").append(conditions[0]);
            for (int i = 1; i < conditions.length; i++) {
                count.append("\n        OR ").append(conditions[i]);
                statement.append("\n        OR ").append(conditions[i]);
            }
            count.append("\n    )");
            statement.append("\n    )");
        } else {
            count.append("\n    OR ").append(conditions[0]);
            statement.append("\n    OR ").append(conditions[0]);
        }
        return this;
    }

    /**
     * Adds OR (cond1 AND cond2 ...) logic to the WHERE clause.
     *
     * @param conditions SQL conditions
     * @return this builder for chaining
     * @throws RuntimeException                 if WHERE was not called first
     * @throws EmptyConditionStatementException if no conditions were provided
     */
    public SqlStatement orAnd(String... conditions) {
        if (alreadyCached()) {
            return this;
        }
        if (!containedWhere) {
            throw new RuntimeException("The `where` method has not been called yet");
        }
        if (conditions.length == 0) {
            throw new EmptyConditionStatementException();
        }
        if (conditions.length > 1) {
            count.append("\n    OR (").append("\n        ").append(conditions[0]);
            statement.append("\n    OR (").append("\n        ").append(conditions[0]);
            for (int i = 1; i < conditions.length; i++) {
                count.append("\n        AND ").append(conditions[i]);
                statement.append("\n        AND ").append(conditions[i]);
            }
            count.append("\n    )");
            statement.append("\n    )");
        } else {
            count.append("\n    OR ").append(conditions[0]);
            statement.append("\n    OR ").append(conditions[0]);
        }
        return this;
    }

    /**
     * Appends ORDER BY to the SELECT query.
     *
     * @param orders columns or expressions used for ordering
     * @return this builder for chaining
     * @throws EmptyOrderStatementException if no columns were provided
     */
    public SqlStatement order(String... orders) {
        if (alreadyCached()) {
            return this;
        }
        if (orders.length == 0) {
            throw new EmptyOrderStatementException();
        }
        statement.append("\nORDER BY");
        statement.append("\n    ").append(orders[0]);
        if (orders.length > 1) {
            for (int i = 1; i < orders.length; i++) {
                statement.append(",\n    ").append(orders[i]);
            }
        }
        return this;
    }

    /**
     * Finalizes the SELECT query and returns the SQL string.
     *
     * @param pageable pagination object (nullable)
     * @return the final SELECT SQL
     */
    public String finalizeQueryStatement(Pageable pageable) {
        final String sqlStatement;
        if (alreadyCached()) {
            sqlStatement = sqlCache.get(cacheKeyName);
        } else {
            sqlStatement = statement.toString();
            if (isUseCache) {
                sqlCache.put(cacheKeyName, sqlStatement);
                sqlCache.put(cacheKeyName + "_count", count.toString());
            }
        }
        if (pageable != null) {
            return sqlStatement + "\n" + pageable.toSql();
        } else {
            return sqlStatement;
        }
    }

    /**
     * Finalizes the SELECT query without pagination.
     *
     * @return generated SQL SELECT query
     */
    public String finalizeQueryStatement() {
        return finalizeQueryStatement(null);
    }

    /**
     * Finalizes the COUNT query and returns the SQL.
     *
     * @return generated SQL COUNT query
     */
    public String finalizeCountStatement() {
        final String sqlStatement;
        final var cacheKey = cacheKeyName + "_count";
        if (alreadyCached()) {
            sqlStatement = sqlCache.get(cacheKey);
        } else {
            sqlStatement = count.toString();
            if (isUseCache) {
                sqlCache.put(cacheKeyName, statement.toString());
                sqlCache.put(cacheKey, sqlStatement);
            }
        }
        return sqlStatement;
    }

    /**
     * Checks if this SQL has already been generated and cached.
     * If so, all builder methods become no-op.
     *
     * @return true if SQL is cached; false otherwise
     */
    private boolean alreadyCached() {
        if (isUseCache) {
            return sqlCache.containsKey(cacheKeyName);
        }
        return false;
    }

}
