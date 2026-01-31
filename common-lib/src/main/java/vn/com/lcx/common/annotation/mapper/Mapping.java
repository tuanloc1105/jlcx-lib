package vn.com.lcx.common.annotation.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a field mapping rule for mapper methods.
 * Can be used to specify custom mappings, skip fields, or provide custom code.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@Repeatable(Mappings.class)
public @interface Mapping {

    /**
     * Source field name to map from
     */
    String fromField() default "";

    /**
     * Target field name to map to
     */
    String toField() default "";

    /**
     * Custom Java code expression for mapping.
     * When provided, this code will be used directly to set the target field.
     */
    String code() default "";

    /**
     * If true, skip this field during mapping
     */
    boolean skip() default false;

    /**
     * Mapper method name for nested object mapping.
     * Use when the source and target fields are complex objects
     * that require their own mapping.
     */
    String nestedMapper() default "";

    /**
     * If true, generates null-safe code for this mapping.
     * The target field will only be set if the source value is not null.
     */
    boolean nullSafe() default false;
}
