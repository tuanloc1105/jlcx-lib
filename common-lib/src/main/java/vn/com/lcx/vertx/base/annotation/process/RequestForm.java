package vn.com.lcx.vertx.base.annotation.process;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to bind a method parameter to a form attribute.
 * <p>
 * The value of the form attribute will be extracted from the request body (form-urlencoded or multipart)
 * and passed to the method parameter.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestForm {
    /**
     * The name of the form attribute.
     * If not specified, the name of the method parameter will be used.
     * @return the name of the form attribute
     */
    String value() default "";
}
