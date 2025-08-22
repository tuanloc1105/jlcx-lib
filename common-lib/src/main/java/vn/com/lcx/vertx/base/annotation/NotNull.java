package vn.com.lcx.vertx.base.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to mark that a field must not be {@code null} or empty.
 * <p>
 * Supported types:
 * <ul>
 *   <li>{@link String} - must not be null or empty string</li>
 *   <li>{@link java.util.Collection} - must not be null or empty</li>
 *   <li>{@link java.util.Map} - must not be null or empty</li>
 *   <li>Other objects - must not be null</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 * public class User {
 *     &#64;NotNull
 *     private String username;
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface NotNull {
}
