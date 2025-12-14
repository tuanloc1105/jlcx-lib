package vn.com.lcx.vertx.base.annotation.process;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to bind a method parameter to a web request header.
 * <p>
 * The value of the header will be extracted from the request headers
 * and passed to the method parameter.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestHeader {
    /**
     * The name of the request header.
     * If not specified, the name of the method parameter will be used.
     *
     * @return the name of the request header
     */
    String value() default "";

    /**
     * Whether the header is required.
     * Default is true.
     *
     * @return true if required, false otherwise
     */
    boolean required() default true;

    /**
     * The default value to use as a fallback when the request header is not
     * provided
     * or has an empty value.
     *
     * @return the default value
     */
    String defaultValue() default "";
}
