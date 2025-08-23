package vn.com.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a class as being mapped to a specific database table.
 * <p>
 * This annotation is typically applied to entity classes in order to specify
 * the table name, schema, and any indexes associated with the table.
 * </p>
 * Example usage:
 * <pre>
 * {@code
 * @TableName(
 *     value = "users",
 *     schema = "public",
 *     indexes = {
 *         @Index(name = "idx_users_email", columns = {"email"}),
 *         @Index(name = "idx_users_username", columns = {"username"})
 *     }
 * )
 * public class User {
 *     private String id;
 *     private String username;
 *     private String email;
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableName {

    /**
     * The name of the database table.
     *
     * @return the table name
     */
    String value();

    /**
     * The schema that the table belongs to.
     * <p>
     * Defaults to an empty string, meaning the default schema will be used.
     * </p>
     *
     * @return the schema name
     */
    String schema() default "";

    /**
     * The list of indexes defined for the table.
     *
     * @return an array of {@link Index} annotations
     */
    Index[] indexes() default {};
}
