package vn.com.lcx.vertx.base.annotation.process;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a controller that handles API endpoints.
 * <p>
 * Use this annotation on classes to indicate that they serve as controllers in the application.
 * The optional {@code path} element specifies the base path for all endpoints in the controller.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Controller {
    String path() default "";
}
