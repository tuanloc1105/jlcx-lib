package vn.com.lcx.vertx.base.annotation.process;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to bind a method parameter to a URI template variable.
 * <p>
 * The value of the path variable will be extracted from the request path
 * and passed to the method parameter.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PathVariable {
    /**
     * The name of the path variable.
     * If not specified, the name of the method parameter will be used.
     * @return the name of the path variable
     */
    String value() default "";
}
