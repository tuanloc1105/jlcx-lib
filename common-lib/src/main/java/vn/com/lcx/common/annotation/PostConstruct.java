package vn.com.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be invoked after the {@link Component} instance is created and
 * its {@link Instance} factory methods are processed.
 *
 * <p>Constraints:</p>
 * <ul>
 *   <li>The method must return {@code void}.</li>
 *   <li>The method must have no parameters.</li>
 *   <li>At most one method per class can be annotated with {@code @PostConstruct}.</li>
 * </ul>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * @Component
 * public class CacheManager {
 *
 *     @PostConstruct
 *     public void init() {
 *         // warm up cache after instance creation
 *     }
 * }
 * }</pre>
 *
 * @see Component
 * @see vn.com.lcx.common.config.ClassPool
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostConstruct {
}
