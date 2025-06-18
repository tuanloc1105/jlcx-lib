package vn.com.lcx.vertx.base.annotation.process;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated API method requires user authentication.
 * <p>
 * Use this annotation on methods to enforce authentication checks before allowing access.
 * Processors or frameworks can detect this annotation and ensure that only authenticated users
 * can access the endpoint.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Auth {
}
