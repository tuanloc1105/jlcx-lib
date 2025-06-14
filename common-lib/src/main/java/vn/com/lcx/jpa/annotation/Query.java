package vn.com.lcx.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a method as a database query operation.
 * This annotation allows you to define custom SQL queries for repository methods.
 *
 * @author tuanloc1105
 * @since 2.0.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Query {
    /**
     * The query string to be executed.
     * This can be either a JPQL (Java Persistence Query Language) query or a native SQL query.
     * The type of query is determined by the {@link #isNative()} parameter.
     */
    String value();

    /**
     * Indicates whether the query is a native SQL query.
     */
    boolean isNative() default false;
}
