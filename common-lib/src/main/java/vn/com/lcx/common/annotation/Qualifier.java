package vn.com.lcx.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the name of the bean to inject when multiple instances of the same type
 * exist in the {@link vn.com.lcx.common.config.ClassPool}.
 *
 * <p>Can be used on:</p>
 * <ul>
 *   <li><b>Fields</b> - used during constructor injection as a fallback when the constructor
 *       parameter itself has no {@code @Qualifier} (e.g. Lombok-generated constructors).</li>
 *   <li><b>Constructor parameters</b> - directly specifies which named bean to inject.</li>
 *   <li><b>{@link Instance} method parameters</b> - specifies which named bean to pass
 *       to the factory method.</li>
 * </ul>
 *
 * <h3>Usage examples:</h3>
 * <pre>{@code
 * // On constructor parameter
 * @Component
 * public class OrderService {
 *     private final DataSource ds;
 *
 *     public OrderService(@Qualifier("primaryDataSource") DataSource ds) {
 *         this.ds = ds;
 *     }
 * }
 *
 * // On field (Lombok)
 * @Component
 * @AllArgsConstructor
 * public class OrderService {
 *     @Qualifier("primaryDataSource")
 *     private final DataSource ds;
 * }
 *
 * // On @Instance method parameter
 * @Instance
 * public JdbcTemplate jdbcTemplate(@Qualifier("primaryDataSource") DataSource ds) {
 *     return new JdbcTemplate(ds);
 * }
 * }</pre>
 *
 * @see Component
 * @see Instance
 * @see vn.com.lcx.common.config.ClassPool
 */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface Qualifier {
    /**
     * The name of the bean to inject.
     *
     * @return the bean name
     */
    String value();
}
