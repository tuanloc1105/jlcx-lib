package vn.com.lcx.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a method as a modifying database operation.
 * This annotation should be used in conjunction with {@link Query} to indicate that a query method modifies the database.
 * It is typically used for operations like INSERT, UPDATE, DELETE, etc.
 *
 * @author tuanloc1105
 * @since 2.0.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Modifying {
}
