package vn.com.lcx.vertx.base.annotation.process;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a REST controller.
 * <p>
 * This annotation is used to define a controller that handles RESTful requests.
 * It combines the functionality of {@link vn.com.lcx.common.annotation.Component} and {@link vn.com.lcx.vertx.base.annotation.process.Controller}.
 * The processor will generate a reactive wrapper for the annotated class.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestController {
    /**
     * The base path for the controller.
     * @return the path
     */
    String path() default "";
}
