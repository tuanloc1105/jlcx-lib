package vn.io.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a Vert.x verticle to be collected and deployed during application startup.
 *
 * <p>Classes annotated with {@code @Verticle} are discovered by the
 * {@link vn.io.lcx.common.config.ClassPool} during package scanning and added to the
 * verticle deployment list. A class can be annotated with both {@code @Verticle} and
 * {@link Component} to also be registered as a managed bean.</p>
 *
 * @see Component
 * @see vn.io.lcx.common.config.ClassPool
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Verticle {
}
