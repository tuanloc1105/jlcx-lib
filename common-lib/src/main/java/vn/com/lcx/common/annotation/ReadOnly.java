package vn.com.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity as read-only, indicating that it can only be used
 * for SELECT operations and must not be modified through INSERT,
 * UPDATE, or DELETE statements.
 * <p>
 * This annotation is typically applied to entities representing
 * database views or immutable data structures where write operations
 * are not allowed or not meaningful.
 * </p>
 *
 * <p><strong>Usage:</strong><br>
 * Apply this annotation at the class level to indicate that the entity
 * should be treated as read-only by the SQL generation or persistence layer.
 * </p>
 *
 * @since 3.4.5.lcx
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ReadOnly {
}
