package vn.com.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to map a Java field to a database column with additional metadata.
 * <p>
 * This annotation can be applied to class fields to provide details about the
 * database column name, constraints, default values, and column behavior during
 * insert or update operations.
 * </p>
 *
 * <h3>Usage example:</h3>
 * <pre>
 * {@code
 * public class User {
 *
 *     @ColumnName(
 *         name = "USER_ID",
 *         nullable = false,
 *         unique = true,
 *         index = true,
 *         columnDataTypeDefinition = "BIGINT",
 *         insertable = true,
 *         updatable = false
 *     )
 *     private Long id;
 *
 *     @ColumnName(name = "USERNAME", nullable = false, unique = true)
 *     private String username;
 * }
 * }
 * </pre>
 *
 * <h3>Attributes:</h3>
 * <ul>
 *   <li><b>name</b> - Defines the column name in the database. Defaults to an empty string.</li>
 *   <li><b>nullable</b> - Indicates whether the column can contain {@code NULL} values.
 *       Defaults to {@code true}.</li>
 *   <li><b>defaultValue</b> - Specifies a default value for the column when none is provided.
 *       Defaults to an empty string.</li>
 *   <li><b>unique</b> - Indicates whether the column must have unique values.
 *       Defaults to {@code false}.</li>
 *   <li><b>index</b> - Indicates whether the column should be indexed for faster queries.
 *       Defaults to {@code false}.</li>
 *   <li><b>columnDataTypeDefinition</b> - Allows explicit definition of the column data type
 *       (e.g., {@code VARCHAR(255)}, {@code BIGINT}). Defaults to an empty string.</li>
 *   <li><b>insertable</b> - Indicates whether the column is included in SQL {@code INSERT}
 *       statements. Defaults to {@code true}.</li>
 *   <li><b>updatable</b> - Indicates whether the column is included in SQL {@code UPDATE}
 *       statements. Defaults to {@code true}.</li>
 * </ul>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnName {

    /**
     * The name of the column in the database.
     */
    String name() default "";

    /**
     * Whether the column can accept {@code NULL} values.
     */
    boolean nullable() default true;

    /**
     * The default value for the column.
     */
    String defaultValue() default "";

    /**
     * Whether the column must contain unique values.
     */
    boolean unique() default false;

    /**
     * Whether the column should be indexed.
     */
    boolean index() default false;

    /**
     * Explicit data type definition for the column
     * (e.g., VARCHAR(255), BIGINT).
     */
    String columnDataTypeDefinition() default "";

    /**
     * Whether the column should be included in INSERT statements.
     */
    boolean insertable() default true;

    /**
     * Whether the column should be included in UPDATE statements.
     */
    boolean updatable() default true;
}
