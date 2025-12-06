package vn.com.lcx.vertx.base.annotation.process;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to bind a method parameter to a web request parameter.
 * <p>
 * The value of the query parameter will be extracted from the request query string
 * and passed to the method parameter.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParam {
    /**
     * The name of the request parameter.
     * If not specified, the name of the method parameter will be used.
     * @return the name of the request parameter
     */
    String value() default "";

    /**
     * Whether the parameter is required.
     * Default is true.
     * @return true if required, false otherwise
     */
    boolean required() default true;

    /**
     * The default value to use as a fallback when the request parameter is not provided
     * or has an empty value.
     * @return the default value
     */
    String defaultValue() default "";
}
