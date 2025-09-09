package vn.com.lcx.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to define an index on one or more columns of a table.
 * <p>
 * This annotation should be used within {@link TableName#indexes()} to declare
 * indexes for the corresponding table.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @TableName(
 *     value = "orders",
 *     indexes = {
 *         @Index(name = "idx_orders_customer", columns = {"customer_id"}),
 *         @Index(name = "idx_orders_date", columns = {"order_date"})
 *     }
 * )
 * public class Order {
 *     private String id;
 *     private String customerId;
 *     private LocalDate orderDate;
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Index {

    /**
     * The name of the index.
     *
     * @return the index name
     */
    String name();

    /**
     * The columns that the index is applied to.
     *
     * @return an array of column names
     */
    String[] columns();
}
