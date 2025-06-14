package vn.com.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated This annotation is deprecated. Please use {@link vn.com.lcx.jpa.annotation.Repository} instead.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Repository {
    String connectionInstanceName() default "";
}
