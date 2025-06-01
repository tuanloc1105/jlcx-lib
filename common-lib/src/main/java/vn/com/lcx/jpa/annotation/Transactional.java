package vn.com.lcx.jpa.annotation;

import vn.com.lcx.jpa.context.JpaConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Transactional {
    int isolation() default Connection.TRANSACTION_READ_COMMITTED;
    int mode() default JpaConstant.USE_EXISTING_TRANSACTION_MODE;
    Class<? extends Throwable>[] onRollback() default {};
}
