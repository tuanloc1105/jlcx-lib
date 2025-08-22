package vn.com.lcx.vertx.base.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to validate that a numeric field is strictly less than
 * the specified {@link #value()}.
 * <p>
 * Supported types:
 * <ul>
 *   <li>Primitive numbers (int, long, double, etc.)</li>
 *   <li>{@link java.lang.Number} subclasses</li>
 *   <li>{@link java.math.BigDecimal}</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 * public class Discount {
 *     &#64;LessThan(100)
 *     private double percentage;
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface LessThan {
    double value();
}
