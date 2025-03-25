package vn.com.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnName {
    String name() default "";

    boolean nullable() default true;

    String defaultValue() default "";

    boolean unique() default false;

    boolean index() default false;

    String columnDataTypeDefinition() default "";
    // String columnComment() default "";
}
