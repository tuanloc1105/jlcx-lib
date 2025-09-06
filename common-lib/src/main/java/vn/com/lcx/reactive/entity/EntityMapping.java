package vn.com.lcx.reactive.entity;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.Map;

/**
 * Defines a contract for mapping between database records and Java entities.
 * <p>
 * Implementations of this interface provide:
 * <ul>
 *     <li>Conversion from JDBC {@link java.sql.ResultSet} or reactive {@link Row} into entity objects</li>
 *     <li>SQL statements (INSERT, UPDATE, DELETE) generation for both JDBC and reactive drivers</li>
 *     <li>Binding parameters as {@link Map} (for JDBC) or {@link Tuple} (for Vert.x reactive SQL client)</li>
 *     <li>Extraction of entity identifiers from database rows</li>
 *     <li>Utility method to map entity field names to database column names</li>
 * </ul>
 *
 * @param <T> the type of entity this mapping is responsible for
 */
public interface EntityMapping<T> {

    /**
     * Map a JDBC {@link java.sql.ResultSet} row into an entity instance.
     *
     * @param resultSet the JDBC result set positioned at the current row
     * @return a populated entity instance
     */
    T resultSetMapping(java.sql.ResultSet resultSet);

    /**
     * Generate a SQL INSERT statement for the given entity.
     *
     * @param model the entity model to insert
     * @return the INSERT SQL string
     */
    String insertStatement(T model);

    /**
     * Generate a SQL UPDATE statement for the given entity.
     *
     * @param model the entity model to update
     * @return the UPDATE SQL string
     */
    String updateStatement(T model);

    /**
     * Generate a SQL DELETE statement for the given entity.
     *
     * @param model the entity model to delete
     * @return the DELETE SQL string
     */
    String deleteStatement(T model);

    /**
     * Generate a reactive SQL INSERT statement with placeholder customization.
     * <p>
     * Examples of placeholders:
     * <ul>
     *     <li>{@code "?"} for JDBC-style parameters</li>
     *     <li>{@code "$1"}, {@code $2}, ... for PostgreSQL</li>
     *     <li>{@code @p1}, {@code @p2}, ... for SQL Server</li>
     * </ul>
     *
     * @param model       the entity model to insert
     * @param placeHolder the placeholder format
     * @return the INSERT SQL string
     */
    String reactiveInsertStatement(T model, String placeHolder);

    /**
     * Generate a reactive SQL UPDATE statement with placeholder customization.
     *
     * @param model       the entity model to update
     * @param placeHolder the placeholder format
     * @return the UPDATE SQL string
     */
    String reactiveUpdateStatement(T model, String placeHolder);

    /**
     * Generate a reactive SQL DELETE statement with placeholder customization.
     *
     * @param model       the entity model to delete
     * @param placeHolder the placeholder format
     * @return the DELETE SQL string
     */
    String reactiveDeleteStatement(T model, String placeHolder);

    /**
     * Bind parameters for JDBC INSERT operation.
     * <p>Key is the 1-based parameter index, value is the bound value.</p>
     *
     * @param model the entity model
     * @return a parameter map
     */
    Map<Integer, Object> insertJDBCParams(T model);

    /**
     * Bind parameters for JDBC UPDATE operation.
     *
     * @param model the entity model
     * @return a parameter map
     */
    Map<Integer, Object> updateJDBCParams(T model);

    /**
     * Bind parameters for JDBC DELETE operation.
     *
     * @param model the entity model
     * @return a parameter map
     */
    Map<Integer, Object> deleteJDBCParams(T model);

    /**
     * Map a Vert.x reactive {@link Row} into an entity instance.
     *
     * @param row the reactive row
     * @return a populated entity instance
     */
    T vertxRowMapping(Row row);

    /**
     * Bind parameters for reactive INSERT operation.
     *
     * @param model the entity model
     * @return a {@link Tuple} with bound values
     */
    Tuple insertTupleParam(T model);

    /**
     * Bind parameters for reactive UPDATE operation.
     *
     * @param model the entity model
     * @return a {@link Tuple} with bound values
     */
    Tuple updateTupleParam(T model);

    /**
     * Bind parameters for reactive DELETE operation.
     *
     * @param model the entity model
     * @return a {@link Tuple} with bound values
     */
    Tuple deleteTupleParam(T model);

    /**
     * Map a Java field name to its corresponding database column name.
     *
     * @param fieldName the Java field name
     * @return the corresponding database column name
     * @throws IllegalArgumentException if the mapping is not found
     */
    String getColumnNameFromFieldName(String fieldName);

}
