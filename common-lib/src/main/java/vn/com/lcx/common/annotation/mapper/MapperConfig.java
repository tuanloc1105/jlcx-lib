package vn.com.lcx.common.annotation.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configuration options for a mapper class.
 * Apply to the mapper interface alongside {@link MapperClass} to customize
 * the generated implementation behavior.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface MapperConfig {

    /**
     * If true, generates null-safe code for all mappings by default.
     * Individual mappings can override this with {@link Mapping#nullSafe()}.
     */
    boolean nullSafeByDefault() default true;

    /**
     * If true, adds @Generated annotation to the output class
     * with generation timestamp.
     */
    boolean addGeneratedAnnotation() default true;

    /**
     * Custom component name for dependency injection.
     * If empty, uses the default generated class name.
     */
    String componentName() default "";

    /**
     * If true, unmapped target fields will cause a compilation warning.
     */
    boolean warnOnUnmappedTargetFields() default false;

    /**
     * If true, type mismatches between source and target fields
     * will cause a compilation error.
     */
    boolean strictTypeChecking() default false;
}
