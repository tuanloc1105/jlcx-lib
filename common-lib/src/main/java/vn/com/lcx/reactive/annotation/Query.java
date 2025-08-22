package vn.com.lcx.reactive.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining native SQL queries on repository methods.
 *
 * <p>This annotation is used to specify native SQL queries that will be executed
 * by the underlying database. The query must be written in the native SQL dialect
 * of the target database (e.g., MySQL, PostgreSQL, Oracle, MSSQL).</p>
 *
 * <p><strong>Important:</strong> The {@code value} parameter must contain a valid
 * native SQL query string. This means:</p>
 * <ul>
 *   <li>The query should use the exact SQL syntax supported by your database</li>
 *   <li>Database-specific functions and features can be used</li>
 *   <li>No ORM abstraction layer will process the query</li>
 *   <li>The query is executed directly by the database engine</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // Simple SELECT query
 * @Query("SELECT * FROM users WHERE status = 'ACTIVE'")
 * Future<List<User>> findActiveUsers(RoutingContext context, SqlConnection connection);
 *
 * // Query with parameters
 * @Query("SELECT * FROM orders WHERE customer_id = ?1 AND total_amount > ?2")
 * Future<List<Order>> findOrdersByCustomerAndAmount(RoutingContext context, SqlConnection connection, Long customerId, BigDecimal minAmount);
 *
 * // Complex query with JOINs
 * @Query("SELECT u.name, o.order_date, o.total_amount " +
 *        "FROM users u " +
 *        "JOIN orders o ON u.id = o.user_id " +
 *        "WHERE u.status = 'ACTIVE' " +
 *        "ORDER BY o.order_date DESC")
 * Future<List<UserOrder>> findUserOrderDetails(RoutingContext context, SqlConnection connection);
 *
 * // UPDATE query
 * @Query("UPDATE products SET stock_quantity = stock_quantity - ?1 WHERE id = ?2")
 * Future<Integer> updateProductStock(RoutingContext context, SqlConnection connection, int quantity, Long productId);
 *
 * // DELETE query
 * @Query("DELETE FROM expired_sessions WHERE created_at < ?1")
 * Future<Integer> deleteExpiredSessions(RoutingContext context, SqlConnection connection, LocalDateTime cutoffDate);
 * }</pre>
 *
 * <h3>Security Considerations:</h3>
 * <ul>
 *   <li>Always use parameterized queries to prevent SQL injection</li>
 *   <li>Avoid string concatenation in the query value</li>
 *   <li>Validate and sanitize any dynamic input</li>
 *   <li>Use appropriate database permissions for the executing user</li>
 * </ul>
 *
 * <h3>Performance Notes:</h3>
 * <ul>
 *   <li>Native queries can be more performant for complex operations</li>
 *   <li>Database-specific optimizations can be leveraged</li>
 *   <li>Consider using database indexes for frequently queried columns</li>
 *   <li>Monitor query execution plans for optimization opportunities</li>
 * </ul>
 *
 * <h3>Limitations:</h3>
 * <ul>
 *   <li>Database portability may be limited due to native SQL syntax</li>
 *   <li>No automatic entity mapping for complex result sets</li>
 *   <li>Database-specific features may not work across different databases</li>
 * </ul>
 *
 * @author lcx
 * @see RetentionPolicy#SOURCE
 * @see ElementType#METHOD
 * @since 3.0.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Query {
    /**
     * The native SQL query to be executed.
     *
     * <p>This must be a valid native SQL query string that can be executed
     * directly by the target database. The query should use the exact syntax
     * and features supported by your specific database system.</p>
     *
     * <p>Examples of valid native queries:</p>
     * <ul>
     *   <li>MySQL: {@code "SELECT * FROM users WHERE status = 'ACTIVE'"}
     *   <li>PostgreSQL: {@code "SELECT * FROM users WHERE status = 'ACTIVE'"}
     *   <li>Oracle: {@code "SELECT * FROM users WHERE status = 'ACTIVE'"}
     * </ul>
     *
     * <p><strong>Note:</strong> The query is processed at compile time (SOURCE retention),
     * so any syntax errors will be detected during compilation.</p>
     *
     * @return the native SQL query string
     */
    String value();
}
