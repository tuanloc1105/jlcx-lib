package vn.com.lcx.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark an interface as a Repository interface for which the implementation
 * will be generated at compile time. The generated implementation will provide
 * basic CRUD operations and custom query methods for database interaction.
 *
 * <p>This annotation is processed by the annotation processor to generate the
 * concrete implementation of the repository interface at compile time.
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * @Repository
 * public interface UserRepository extends JpaRepository<User, Long> {
 *     // Custom query methods can be declared here
 *     // But this feature is not yet implemented
 *     List<User> findByUsername(String username);
 *
 *     @Query("SELECT u FROM User u WHERE u.status = :status")
 *     List<User> findByStatus(@Param("status") String status);
 * }
 * }</pre>
 *
 * <p>The annotation processor will generate an implementation of this interface
 * with all the necessary database operations. The generated class will be placed
 * in the same package as the interface with 'Impl' suffix by default.
 *
 * <p><b>Note:</b> The annotated interface should extend {@code JpaRepository} or one of its
 * sub-interfaces to inherit common database operations.
 *
 * @see vn.com.lcx.jpa.respository.JpaRepository
 * @see vn.com.lcx.jpa.processor.RepositoryProcessor
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Repository {
}
