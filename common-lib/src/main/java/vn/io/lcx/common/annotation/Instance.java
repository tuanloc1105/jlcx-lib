package vn.io.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method inside a {@link Component} class as a factory method whose return value
 * will be registered as a bean in the {@link vn.io.lcx.common.config.ClassPool}.
 *
 * <p>The method must have a non-void return type. The returned object is registered under
 * its class name, superclass names, and interface names for polymorphic lookup.</p>
 *
 * <p>Method parameters are supported and resolved from the pool as beans. If a parameter
 * cannot be resolved immediately, the method invocation is deferred until all dependencies
 * are available. Parameters support {@link Qualifier} for named lookup.</p>
 *
 * <h3>Usage examples:</h3>
 * <pre>{@code
 * @Component
 * public class AppConfig {
 *
 *     // No-arg factory method
 *     @Instance
 *     public Gson gson() {
 *         return new GsonBuilder().create();
 *     }
 *
 *     // Named instance
 *     @Instance("primaryDataSource")
 *     public DataSource primaryDs() {
 *         return DataSourceBuilder.create().url("jdbc:...").build();
 *     }
 *
 *     // Factory method with bean parameters
 *     @Instance
 *     public UserService userService(UserRepository repo) {
 *         return new UserService(repo);
 *     }
 *
 *     // Factory method with @Qualifier parameter
 *     @Instance
 *     public ReportService reportService(@Qualifier("secondaryDataSource") DataSource ds) {
 *         return new ReportService(ds);
 *     }
 * }
 * }</pre>
 *
 * @see Component
 * @see Qualifier
 * @see vn.io.lcx.common.config.ClassPool
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Instance {
    /**
     * Optional name for the registered instance. If blank, the method name is used instead.
     *
     * @return the instance name, or empty string to use the method name
     */
    String value() default "";
}
