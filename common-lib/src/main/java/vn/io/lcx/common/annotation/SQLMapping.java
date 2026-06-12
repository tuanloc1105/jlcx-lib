package vn.io.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a writable SQL entity mapping.
 * <p>
 * The annotation processor generates {@code {ClassName}Utils} and
 * {@code {ClassName}MappingImpl} classes with JDBC/Vert.x row mapping,
 * insert, update, delete, parameter, and id helper methods.
 * </p>
 *
 * <p>
 * Classes annotated with {@code @SQLMapping} must declare entity metadata such
 * as {@link TableName} and exactly one {@link IdColumn}. Use
 * {@link SQLProjection} instead for read-only DTOs that only need SQL result
 * row mapping.
 * </p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SQLMapping {
}
