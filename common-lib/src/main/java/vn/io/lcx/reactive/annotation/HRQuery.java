package vn.io.lcx.reactive.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a Hibernate Reactive HQL or native query on an HR repository method.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface HRQuery {

    String value();

    String countQuery() default "";

    boolean isNative() default false;
}
