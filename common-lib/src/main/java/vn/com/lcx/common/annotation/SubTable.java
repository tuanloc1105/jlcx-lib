package vn.com.lcx.common.annotation;

import vn.com.lcx.common.database.type.JoinType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is under developing
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubTable {
    String columnName();

    String mapField();

    JoinType joinType() default JoinType.INNER_JOIN;
}
