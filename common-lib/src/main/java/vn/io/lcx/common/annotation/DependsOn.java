package vn.io.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Prevents a {@link Component} from being instantiated or an {@link Instance} method
 * from being invoked until all specified prerequisite beans exist in the
 * {@link vn.io.lcx.common.config.ClassPool}.
 *
 * <p>Prerequisites can be declared by bean name ({@link #value()}) or by bean class
 * ({@link #classes()}), or both. All specified beans must be present before the
 * annotated element is processed.</p>
 *
 * <h3>Usage examples:</h3>
 * <pre>{@code
 * // Wait for a named bean
 * @Component
 * @DependsOn("appConfig")
 * public class MyService { ... }
 *
 * // Wait for multiple beans by class
 * @Component
 * @DependsOn(classes = {DataSource.class, CacheManager.class})
 * public class ReportService { ... }
 *
 * // Mix names and classes
 * @Component
 * @DependsOn(value = "primaryDataSource", classes = CacheManager.class)
 * public class OrderService { ... }
 *
 * // On an @Instance factory method
 * @Component
 * public class AppConfig {
 *     @Instance
 *     @DependsOn("vertx")
 *     public JWTAuth jwtAuth() {
 *         var vertx = ClassPool.getInstance("vertx", Vertx.class);
 *         return JWTAuth.create(vertx, ...);
 *     }
 * }
 * }</pre>
 *
 * @see Component
 * @see Instance
 * @see vn.io.lcx.common.config.ClassPool
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DependsOn {
    /**
     * Bean names that must exist before this component/instance is created.
     *
     * @return array of bean names
     */
    String[] value() default {};

    /**
     * Bean classes that must exist before this component/instance is created.
     * Looked up by fully qualified class name.
     *
     * @return array of bean classes
     */
    Class<?>[] classes() default {};
}
