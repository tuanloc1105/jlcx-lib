package vn.com.lcx.vertx.base.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to validate that a field's value must be one of the
 * specified string values in {@link #value()}.
 * <p>
 * Supported types:
 * <ul>
 *   <li>{@link String}</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 * public class Payment {
 *     &#64;Values({"CASH", "CARD", "TRANSFER"})
 *     private String method;
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Values {
    String[] value();
}
