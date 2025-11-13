package vn.com.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method should be executed before an SQL INSERT operation.
 * <p>
 * This annotation is typically used to perform pre-processing logic such as
 * initializing default values, generating IDs, or validating data before
 * inserting an object into the database.
 * </p>
 *
 * <p><strong>Note:</strong> For this annotation to take effect, the containing
 * class must also be annotated with {@link vn.com.lcx.common.annotation.SQLMapping}.
 * Otherwise, methods annotated with {@code @PreInsert} will be ignored during processing.
 * </p>
 *
 * @since 3.4.5.lcx
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface PreInsert {
}
