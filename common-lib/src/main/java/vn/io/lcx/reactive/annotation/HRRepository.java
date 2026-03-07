package vn.io.lcx.reactive.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as a Reactive Hibernate Repository.
 * <p>
 * This annotation triggers the annotation processor to generate an
 * implementation for the
 * interface. The interface must extend
 * {@link vn.io.lcx.reactive.repository.HReactiveRepository}.
 * </p>
 * <p>
 * Example usage:
 * 
 * <pre>
 * {@code
 * @HRRepository
 * public interface BookRepository extends HReactiveRepository<Book> {
 *     // custom query methods
 * }
 * }
 * </pre>
 * </p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface HRRepository {
}
