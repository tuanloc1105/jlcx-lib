package vn.com.lcx.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define a custom Result Set Mapping for native queries.
 * This annotation allows mapping the result of a native query to a specific
 * entity or DTO
 * using a custom mapping mechanism.
 *
 * @author tuanloc1105
 * @since 2.0.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ResultSetMapping {
    /**
     * The name of the result set mapping.
     */
    String name();
}
