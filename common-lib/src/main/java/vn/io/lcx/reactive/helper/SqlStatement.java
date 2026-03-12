package vn.io.lcx.reactive.helper;

import vn.io.lcx.common.database.pageable.Pageable;
import vn.io.lcx.reactive.exception.EmptyConditionStatementException;
import vn.io.lcx.reactive.exception.EmptyFromStatementException;
import vn.io.lcx.reactive.exception.EmptyGroupByStatementException;
import vn.io.lcx.reactive.exception.EmptyOrderStatementException;
import vn.io.lcx.reactive.exception.EmptySelectStatementException;

/**
 * <p>A fluent SQL builder class for constructing both a main SELECT query and its
 * corresponding COUNT(1) query in parallel.</p>
 *
 * <p>This class is designed for dynamic SQL generation in systems where query logic
 * (especially filtering and pagination) is frequently reused.</p>
 *
 * <h3>Main Features</h3>
 * <ul>
 *     <li>Builds SELECT and COUNT SQL statements simultaneously.</li>
 *     <li>Supports logic chaining: AND, OR, AND(...OR...), OR(...AND...).</li>
 *     <li>Supports GROUP BY and HAVING clauses.</li>
 *     <li>State validation ensures correct clause ordering.</li>
 *     <li>Fluent, chainable interface.</li>
 * </ul>
 *
 * <h3>Important Notes</h3>
 * <ul>
 *     <li>All SQL parameter placeholders must use <strong>#</strong>.
 *         Example: <code>column = #</code></li>
 *     <li>When GROUP BY is used, the COUNT query becomes
 *         <code>SELECT COUNT(1) ... GROUP BY ...</code>, which returns one row per group.
 *         For pagination with GROUP BY, callers should handle the count query accordingly.</li>
 * </ul>
 */
public class SqlStatement {

    /**
     * Tracks the builder's current position in the SQL clause sequence.
     */
    private enum State {
        INIT, SELECT, FROM, WHERE, CONDITION, GROUP_BY, HAVING, ORDER
    }

    /**
     * Builder for the SELECT query.
     */
    private final StringBuilder statement = new StringBuilder();

    /**
     * Builder for the COUNT query.
     */
    private final StringBuilder count = new StringBuilder();

    /**
     * Current state of the builder for call-order validation.
     */
    private State currentState = State.INIT;

    private SqlStatement() {
    }

    /**
     * Creates a new SqlStatement builder.
     *
     * @return a new {@code SqlStatement} instance
     */
    public static SqlStatement init() {
        return new SqlStatement();
    }

    /**
     * Appends a SELECT clause.
     * <p><strong>Note:</strong> All SQL parameter placeholders must use "#".</p>
     *
     * @param columns one or more column names or expressions
     * @return this builder for chaining
     * @throws IllegalStateException       if called out of order
     * @throws EmptySelectStatementException if no columns were supplied
     */
    public SqlStatement select(String... columns) {
        if (currentState != State.INIT) {
            throw new IllegalStateException("select() must be called first and only once");
        }
        if (columns.length == 0) {
            throw new EmptySelectStatementException();
        }
        count.append("SELECT\n    COUNT(1)");
        statement.append("SELECT");
        statement.append("\n    ").append(columns[0]);
        for (int i = 1; i < columns.length; i++) {
            statement.append(",\n    ").append(columns[i]);
        }
        currentState = State.SELECT;
        return this;
    }

    /**
     * Appends a FROM clause to both SELECT and COUNT statements.
     *
     * @param tables one or more table names or join expressions
     * @return this builder for chaining
     * @throws IllegalStateException      if select() was not called first
     * @throws EmptyFromStatementException if no tables were provided
     */
    public SqlStatement from(String... tables) {
        if (currentState != State.SELECT) {
            throw new IllegalStateException("from() must be called after select()");
        }
        if (tables.length == 0) {
            throw new EmptyFromStatementException();
        }
        count.append("\nFROM");
        count.append("\n    ").append(tables[0]);
        statement.append("\nFROM");
        statement.append("\n    ").append(tables[0]);
        for (int i = 1; i < tables.length; i++) {
            count.append("\n    ").append(tables[i]);
            statement.append("\n    ").append(tables[i]);
        }
        currentState = State.FROM;
        return this;
    }

    /**
     * Adds a WHERE clause with a given condition.
     * <p>Example: <code>column = #</code></p>
     *
     * @param condition SQL condition text
     * @return this builder for chaining
     * @throws IllegalStateException if from() was not called first
     */
    public SqlStatement where(String condition) {
        requireState("where()", State.FROM);
        count.append("\nWHERE");
        count.append("\n    ").append(condition);
        statement.append("\nWHERE");
        statement.append("\n    ").append(condition);
        currentState = State.WHERE;
        return this;
    }

    /**
     * Adds a basic WHERE 1 = 1 clause.
     *
     * @return this builder for chaining
     * @throws IllegalStateException if from() was not called first
     */
    public SqlStatement where() {
        return where("1 = 1");
    }

    /**
     * Adds AND conditions to the WHERE clause.
     * <p>Multiple conditions are grouped: <code>AND (cond1 AND cond2 ...)</code></p>
     *
     * @param conditions one or more SQL conditions
     * @return this builder for chaining
     * @throws IllegalStateException           if where() was not called first
     * @throws EmptyConditionStatementException if no conditions were provided
     */
    public SqlStatement and(String... conditions) {
        return condition("and()", "AND", "AND", conditions);
    }

    /**
     * Adds AND (cond1 OR cond2 ...) logic to the WHERE clause.
     *
     * @param conditions one or more SQL conditions
     * @return this builder for chaining
     * @throws IllegalStateException           if where() was not called first
     * @throws EmptyConditionStatementException if no conditions were provided
     */
    public SqlStatement andOr(String... conditions) {
        return condition("andOr()", "AND", "OR", conditions);
    }

    /**
     * Adds OR conditions to the WHERE clause.
     * <p>Multiple conditions are grouped: <code>OR (cond1 OR cond2 ...)</code></p>
     *
     * @param conditions one or more SQL conditions
     * @return this builder for chaining
     * @throws IllegalStateException           if where() was not called first
     * @throws EmptyConditionStatementException if no conditions were provided
     */
    public SqlStatement or(String... conditions) {
        return condition("or()", "OR", "OR", conditions);
    }

    /**
     * Adds OR (cond1 AND cond2 ...) logic to the WHERE clause.
     *
     * @param conditions SQL conditions
     * @return this builder for chaining
     * @throws IllegalStateException           if where() was not called first
     * @throws EmptyConditionStatementException if no conditions were provided
     */
    public SqlStatement orAnd(String... conditions) {
        return condition("orAnd()", "OR", "AND", conditions);
    }

    /**
     * Appends a GROUP BY clause to both SELECT and COUNT statements.
     *
     * @param columns one or more column names or expressions
     * @return this builder for chaining
     * @throws IllegalStateException          if called before from() or after order()
     * @throws EmptyGroupByStatementException if no columns were provided
     */
    public SqlStatement groupBy(String... columns) {
        requireState("groupBy()", State.FROM, State.WHERE, State.CONDITION);
        if (columns.length == 0) {
            throw new EmptyGroupByStatementException();
        }
        count.append("\nGROUP BY");
        count.append("\n    ").append(columns[0]);
        statement.append("\nGROUP BY");
        statement.append("\n    ").append(columns[0]);
        for (int i = 1; i < columns.length; i++) {
            count.append(",\n    ").append(columns[i]);
            statement.append(",\n    ").append(columns[i]);
        }
        currentState = State.GROUP_BY;
        return this;
    }

    /**
     * Appends a HAVING clause to both SELECT and COUNT statements.
     *
     * @param condition SQL condition for HAVING
     * @return this builder for chaining
     * @throws IllegalStateException if groupBy() was not called first
     */
    public SqlStatement having(String condition) {
        requireState("having()", State.GROUP_BY);
        count.append("\nHAVING");
        count.append("\n    ").append(condition);
        statement.append("\nHAVING");
        statement.append("\n    ").append(condition);
        currentState = State.HAVING;
        return this;
    }

    /**
     * Appends ORDER BY to the SELECT query only.
     *
     * @param orders columns or expressions used for ordering
     * @return this builder for chaining
     * @throws IllegalStateException       if called before from()
     * @throws EmptyOrderStatementException if no columns were provided
     */
    public SqlStatement order(String... orders) {
        requireState("order()", State.FROM, State.WHERE, State.CONDITION, State.GROUP_BY, State.HAVING);
        if (orders.length == 0) {
            throw new EmptyOrderStatementException();
        }
        statement.append("\nORDER BY");
        statement.append("\n    ").append(orders[0]);
        for (int i = 1; i < orders.length; i++) {
            statement.append(",\n    ").append(orders[i]);
        }
        currentState = State.ORDER;
        return this;
    }

    /**
     * Finalizes the SELECT query and returns the SQL string.
     *
     * @param pageable pagination object (nullable)
     * @return the final SELECT SQL
     */
    public String finalizeQueryStatement(Pageable pageable) {
        var sqlStatement = statement.toString();
        if (pageable != null) {
            return sqlStatement + "\n" + pageable.toSql();
        }
        return sqlStatement;
    }

    /**
     * Finalizes the SELECT query without pagination.
     *
     * @return generated SQL SELECT query
     */
    public String finalizeQueryStatement() {
        return finalizeQueryStatement(null);
    }

    /**
     * Finalizes the COUNT query and returns the SQL.
     *
     * @return generated SQL COUNT query
     */
    public String finalizeCountStatement() {
        return count.toString();
    }

    @Override
    public String toString() {
        return "SqlStatement{query=" + statement + ", count=" + count + '}';
    }

    /**
     * Shared implementation for {@code and}, {@code andOr}, {@code or}, {@code orAnd}.
     *
     * @param methodName   name of the calling method (for error messages)
     * @param outerKeyword AND or OR — the keyword joining this block to the WHERE clause
     * @param innerKeyword AND or OR — the keyword joining conditions within the block
     * @param conditions   one or more SQL conditions
     * @return this builder for chaining
     */
    private SqlStatement condition(String methodName, String outerKeyword, String innerKeyword, String... conditions) {
        requireState(methodName, State.WHERE, State.CONDITION);
        if (conditions.length == 0) {
            throw new EmptyConditionStatementException();
        }
        if (conditions.length > 1) {
            var open = "\n    " + outerKeyword + " (\n        ";
            count.append(open).append(conditions[0]);
            statement.append(open).append(conditions[0]);
            for (int i = 1; i < conditions.length; i++) {
                var inner = "\n        " + innerKeyword + " ";
                count.append(inner).append(conditions[i]);
                statement.append(inner).append(conditions[i]);
            }
            count.append("\n    )");
            statement.append("\n    )");
        } else {
            var single = "\n    " + outerKeyword + " ";
            count.append(single).append(conditions[0]);
            statement.append(single).append(conditions[0]);
        }
        currentState = State.CONDITION;
        return this;
    }

    /**
     * Validates that the current state is one of the allowed states.
     *
     * @param methodName   name of the calling method (for error messages)
     * @param allowedStates states in which the method may be called
     * @throws IllegalStateException if the current state is not allowed
     */
    private void requireState(String methodName, State... allowedStates) {
        for (var allowed : allowedStates) {
            if (currentState == allowed) {
                return;
            }
        }
        throw new IllegalStateException(methodName + " cannot be called in state " + currentState);
    }

}
