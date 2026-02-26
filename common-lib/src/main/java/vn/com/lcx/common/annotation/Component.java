package vn.com.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a managed component to be automatically instantiated and registered
 * in the {@link vn.com.lcx.common.config.ClassPool} during application startup.
 *
 * <p>Classes annotated with {@code @Component} must have exactly one constructor.
 * The container supports two initialization modes:</p>
 * <ul>
 *   <li><b>Simple component</b> - class has no non-static fields: instantiated immediately
 *       via the no-arg constructor.</li>
 *   <li><b>Dependent component</b> - class has non-static fields: instantiated via constructor
 *       injection. Dependencies are resolved iteratively from the pool. Constructor parameters
 *       support {@link Qualifier} for named lookup.</li>
 * </ul>
 *
 * <p>When using Lombok's {@code @AllArgsConstructor}, {@link Qualifier} annotations on fields
 * are used as a fallback to resolve constructor parameters.</p>
 *
 * <h3>Usage examples:</h3>
 * <pre>{@code
 * // Simple component (no dependencies)
 * @Component
 * public class AppConfig {
 *     @Instance
 *     public Gson gson() {
 *         return new GsonBuilder().create();
 *     }
 * }
 *
 * // Component with constructor injection
 * @Component
 * public class UserService {
 *     private final UserRepository userRepository;
 *
 *     public UserService(UserRepository userRepository) {
 *         this.userRepository = userRepository;
 *     }
 * }
 *
 * // Lombok + @Qualifier on fields
 * @Component
 * @AllArgsConstructor
 * public class DataService {
 *     @Qualifier("primaryDataSource")
 *     private final DataSource primaryDs;
 *     @Qualifier("secondaryDataSource")
 *     private final DataSource secondaryDs;
 * }
 * }</pre>
 *
 * @see Instance
 * @see PostConstruct
 * @see Qualifier
 * @see vn.com.lcx.common.config.ClassPool
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
}
