package vn.com.lcx.vertx.base.annotation.process;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to bind a method parameter to a file upload.
 * <p>
 * The file upload object will be extracted from the multipart request
 * and passed to the method parameter.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestFile {
    /**
     * The name of the file parameter.
     * If not specified, the name of the method parameter will be used.
     * @return the name of the file parameter
     */
    String value() default "";
}
