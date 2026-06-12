package vn.io.lcx.reactive.annotation;

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
 * Generated custom query methods are compile-time validated. They must use this
 * shape:
 * <ol>
 *     <li>Return {@code io.vertx.core.Future<X>}</li>
 *     <li>Declare {@code io.vertx.ext.web.RoutingContext} as the first parameter</li>
 *     <li>Declare {@code io.vertx.sqlclient.SqlConnection} as the second parameter</li>
 *     <li>Declare {@code vn.io.lcx.common.database.pageable.Pageable}, when present, as the final parameter</li>
 *     <li>Use {@link Query} for non-CRUD custom methods</li>
 * </ol>
 * Invalid signatures fail annotation processing with diagnostics instead of
 * generating a repository method that throws at runtime.
 * <p>
 * This annotation is intended for interfaces only and has {@code SOURCE} retention,
 * meaning it is present only in the source code and used by code generation tools during compilation.
 *
 * <pre>
 * {@code
 * @RRepository
 * public interface UserRepository extends vn.io.lcx.reactive.repository.ReactiveRepository<User> {
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
