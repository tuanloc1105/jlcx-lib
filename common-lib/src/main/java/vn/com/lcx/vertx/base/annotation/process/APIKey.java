package vn.com.lcx.vertx.base.annotation.process;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated API method requires an API key for access.
 * <p>
 * Use this annotation on methods to enforce API key authentication.
 * Processors or frameworks can check for this annotation and perform
 * API key validation before allowing access to the endpoint.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface APIKey {
}
