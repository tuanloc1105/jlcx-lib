package vn.com.lcx.vertx.base.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to mark that a field must match a specific regex pattern.
 * <p>
 * Supported types:
 * <ul>
 * <li>{@link String} - must match the provided regex pattern</li>
 * </ul>
 * <p>
 * Example:
 *
 * <pre>
 * public class User {
 *     &#64;Regex(value = "^[a-zA-Z0-9]+$")
 *     private String username;
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Regex {
    /**
     * The regex pattern that the field must match.
     *
     * @return the regex pattern
     */
    String value();
}
