package vn.com.lcx.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark method parameters in repository query methods.
 * This annotation allows you to bind method parameters to named parameters in your query.
 * The value of the annotation specifies the name that will be used in the query.
 *
 * @author tuanloc1105
 * @since 2.0.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface Param {
    String value();
}
