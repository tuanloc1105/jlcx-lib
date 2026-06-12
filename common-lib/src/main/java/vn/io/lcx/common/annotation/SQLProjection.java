package vn.io.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a read-only SQL result projection.
 * <p>
 * The annotation processor generates a {@code {ClassName}Utils} class with
 * {@code resultSetMapping(java.sql.ResultSet)} and
 * {@code vertxRowMapping(io.vertx.sqlclient.Row)} methods only.
 * </p>
 *
 * <p>
 * Projection classes are intended for custom SELECT results, joined queries,
 * database views, and other DTO-shaped result sets. They may use
 * {@link ColumnName} to bind fields to SQL column aliases, but they do not
 * require {@link TableName} or {@link IdColumn}, and no insert/update/delete
 * helpers are generated.
 * </p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SQLProjection {
}
