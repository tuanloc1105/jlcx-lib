package vn.com.lcx.vertx.base.annotation.process;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps an API method to handle HTTP PUT requests.
 * <p>
 * Use this annotation on methods to indicate that they should be invoked for PUT requests
 * to the specified path.
 * The optional {@code path} element defines the endpoint's path.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Put {
    String path() default "";
}
