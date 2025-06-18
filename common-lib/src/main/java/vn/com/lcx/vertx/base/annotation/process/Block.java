package vn.com.lcx.vertx.base.annotation.process;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller whose APIs perform blocking operations.
 * <p>
 * This annotation should be used on controller classes where APIs may execute blocking tasks,
 * such as synchronous database queries, large file processing, or other time-consuming operations.
 * <p>
 * When a controller is annotated with {@code @Block}, processors or frameworks can detect this and handle accordingly
 * (e.g., offloading to worker threads, issuing deployment warnings, etc.).
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Block {
}
