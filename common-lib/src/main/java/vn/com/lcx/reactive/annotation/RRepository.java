package vn.com.lcx.reactive.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a repository interface for automatic code generation.
 * <p>
 * <b>Note:</b> All methods in an interface annotated with {@code @RRepository}
 * <b>must always return</b> {@code io.vertx.core.Future}. This ensures asynchronous behavior
 * and aligns with the reactive programming model of Vert.x.
 * <p>
 * This annotation is intended for interfaces only and has {@code SOURCE} retention,
 * meaning it is present only in the source code and used by code generation tools during compilation.
 * <p>
 * If a method does not return {@code Future}, the code generation process may report an error or ignore that method.
 *
 * <pre>
 * {@code
 * @RRepository
 * public interface UserRepository extends vn.com.lcx.reactive.repository.ReactiveRepository<User> {
 *     @Query("SQL statement here")
 *     Future<User> findById(RoutingContext context, SqlConnection connection, String id);
 *     @Query("SQL statement here")
 *     Future<List<User>> findAll(RoutingContext context, SqlConnection connection);
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RRepository {
}
