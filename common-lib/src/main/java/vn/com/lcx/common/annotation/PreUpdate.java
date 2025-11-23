package vn.com.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method should be executed before an SQL UPDATE operation.
 * <p>
 * This annotation is typically used to handle pre-update logic such as
 * auditing (e.g., updating "lastModified" timestamps), validating data consistency,
 * or refreshing computed fields before updating an object in the database.
 * </p>
 *
 * <p><strong>Note:</strong> For this annotation to take effect, the containing
 * class must also be annotated with {@link vn.com.lcx.common.annotation.SQLMapping}.
 * Otherwise, methods annotated with {@code @PreUpdate} will be ignored during processing.
 * </p>
 *
 * @since 3.4.5.lcx
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface PreUpdate {
}
