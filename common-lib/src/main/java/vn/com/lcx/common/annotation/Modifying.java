package vn.com.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated This annotation is deprecated. Please use {@link vn.com.lcx.jpa.annotation.Modifying} instead.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Modifying {
}
