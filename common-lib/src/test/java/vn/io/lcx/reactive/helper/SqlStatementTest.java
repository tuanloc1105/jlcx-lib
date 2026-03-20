package vn.io.lcx.reactive.helper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vn.io.lcx.reactive.exception.EmptyConditionStatementException;
import vn.io.lcx.reactive.exception.EmptyFromStatementException;
import vn.io.lcx.reactive.exception.EmptyGroupByStatementException;
import vn.io.lcx.reactive.exception.EmptyOrderStatementException;
import vn.io.lcx.reactive.exception.EmptySelectStatementException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlStatementTest {

    @Test
    void init_createsNewInstance() {
        SqlStatement stmt = SqlStatement.init();
        assertNotNull(stmt);
    }

    // --- SELECT + FROM basic queries ---

    @Nested
    class SelectFromTests {

        @Test
        void select_from_generatesBasicSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name", "email")
                    .from("users");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("SELECT"));
            assertTrue(query.contains("id"));
            assertTrue(query.contains("name"));
            assertTrue(query.contains("email"));
            assertTrue(query.contains("FROM"));
            assertTrue(query.contains("users"));
        }

        @Test
        void select_singleColumn_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("SELECT"));
            assertTrue(query.contains("id"));
            assertTrue(query.contains("FROM"));
            assertTrue(query.contains("users"));
        }

        @Test
        void select_from_multipleTablesJoin_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("u.id", "u.name")
                    .from("users u", "JOIN orders o ON u.id = o.user_id");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("users u"));
            assertTrue(query.contains("JOIN orders o ON u.id = o.user_id"));
        }

        @Test
        void select_from_countStatementAlsoBuilt() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name")
                    .from("users");

            String countQuery = stmt.finalizeCountStatement();

            assertTrue(countQuery.contains("SELECT"));
            assertTrue(countQuery.contains("COUNT(1)"));
            assertTrue(countQuery.contains("FROM"));
            assertTrue(countQuery.contains("users"));
            // COUNT query should NOT contain column names from SELECT
            assertFalse(countQuery.contains("id"));
            assertFalse(countQuery.contains("name"));
        }
    }

    // --- WHERE clause ---

    @Nested
    class WhereTests {

        @Test
        void select_from_where_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name")
                    .from("users")
                    .where("status = #");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("WHERE"));
            assertTrue(query.contains("status = #"));
        }

        @Test
        void where_noArg_generatesWhereOneEqualsOne() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users")
                    .where();

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("WHERE"));
            assertTrue(query.contains("1 = 1"));
        }

        @Test
        void where_addedToCountStatement() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name")
                    .from("users")
                    .where("active = #");

            String countQuery = stmt.finalizeCountStatement();

            assertTrue(countQuery.contains("WHERE"));
            assertTrue(countQuery.contains("active = #"));
        }
    }

    // --- AND / OR conditions ---

    @Nested
    class ConditionTests {

        @Test
        void select_from_where_and_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name")
                    .from("users")
                    .where("status = #")
                    .and("age > #");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("WHERE"));
            assertTrue(query.contains("status = #"));
            assertTrue(query.contains("AND"));
            assertTrue(query.contains("age > #"));
        }

        @Test
        void select_from_where_or_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name")
                    .from("users")
                    .where("status = #")
                    .or("role = #");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("WHERE"));
            assertTrue(query.contains("status = #"));
            assertTrue(query.contains("OR"));
            assertTrue(query.contains("role = #"));
        }

        @Test
        void and_multipleConditions_groupedCorrectly() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users")
                    .where("1 = 1")
                    .and("a = #", "b = #");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("AND ("));
            assertTrue(query.contains("a = #"));
            assertTrue(query.contains("b = #"));
            assertTrue(query.contains(")"));
        }

        @Test
        void or_multipleConditions_groupedCorrectly() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users")
                    .where("1 = 1")
                    .or("x = #", "y = #");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("OR ("));
            assertTrue(query.contains("x = #"));
            assertTrue(query.contains("y = #"));
        }

        @Test
        void andOr_mixedLogic_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users")
                    .where("1 = 1")
                    .andOr("a = #", "b = #");

            String query = stmt.finalizeQueryStatement();

            // andOr: outer is AND, inner is OR
            assertTrue(query.contains("AND ("));
            assertTrue(query.contains("a = #"));
            // Between the conditions, inner keyword should be OR
            assertTrue(query.contains("OR"));
            assertTrue(query.contains("b = #"));
        }

        @Test
        void orAnd_mixedLogic_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users")
                    .where("1 = 1")
                    .orAnd("a = #", "b = #");

            String query = stmt.finalizeQueryStatement();

            // orAnd: outer is OR, inner is AND
            assertTrue(query.contains("OR ("));
            assertTrue(query.contains("a = #"));
            assertTrue(query.contains("AND"));
            assertTrue(query.contains("b = #"));
        }

        @Test
        void chaining_multipleConditions_works() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name")
                    .from("users")
                    .where("status = #")
                    .and("age > #")
                    .or("vip = #");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("WHERE"));
            assertTrue(query.contains("AND"));
            assertTrue(query.contains("OR"));
        }

        @Test
        void conditions_addedToCountStatement() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name")
                    .from("users")
                    .where("status = #")
                    .and("active = #");

            String countQuery = stmt.finalizeCountStatement();

            assertTrue(countQuery.contains("WHERE"));
            assertTrue(countQuery.contains("AND"));
            assertTrue(countQuery.contains("active = #"));
        }
    }

    // --- GROUP BY + HAVING ---

    @Nested
    class GroupByHavingTests {

        @Test
        void select_from_groupBy_having_generatesSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("department", "COUNT(*)")
                    .from("employees")
                    .groupBy("department")
                    .having("COUNT(*) > 5");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("GROUP BY"));
            assertTrue(query.contains("department"));
            assertTrue(query.contains("HAVING"));
            assertTrue(query.contains("COUNT(*) > 5"));
        }

        @Test
        void groupBy_multipleColumns_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("dept", "role", "COUNT(*)")
                    .from("employees")
                    .groupBy("dept", "role");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("GROUP BY"));
            assertTrue(query.contains("dept"));
            assertTrue(query.contains("role"));
        }

        @Test
        void groupBy_afterWhere_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("dept", "COUNT(*)")
                    .from("employees")
                    .where("active = #")
                    .groupBy("dept");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("WHERE"));
            assertTrue(query.contains("GROUP BY"));
        }

        @Test
        void groupBy_afterCondition_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("dept", "COUNT(*)")
                    .from("employees")
                    .where("active = #")
                    .and("salary > #")
                    .groupBy("dept");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("AND"));
            assertTrue(query.contains("GROUP BY"));
        }

        @Test
        void groupBy_addedToCountStatement() {
            SqlStatement stmt = SqlStatement.init()
                    .select("dept", "COUNT(*)")
                    .from("employees")
                    .groupBy("dept")
                    .having("COUNT(*) > 5");

            String countQuery = stmt.finalizeCountStatement();

            assertTrue(countQuery.contains("GROUP BY"));
            assertTrue(countQuery.contains("HAVING"));
        }
    }

    // --- ORDER BY ---

    @Nested
    class OrderTests {

        @Test
        void select_from_order_generatesSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name")
                    .from("users")
                    .order("name ASC");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("ORDER BY"));
            assertTrue(query.contains("name ASC"));
        }

        @Test
        void order_multipleColumns_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name")
                    .from("users")
                    .order("name ASC", "id DESC");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("ORDER BY"));
            assertTrue(query.contains("name ASC"));
            assertTrue(query.contains("id DESC"));
        }

        @Test
        void order_notInCountStatement() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name")
                    .from("users")
                    .order("name ASC");

            String countQuery = stmt.finalizeCountStatement();

            assertFalse(countQuery.contains("ORDER BY"));
            assertFalse(countQuery.contains("name ASC"));
        }

        @Test
        void order_afterWhere_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name")
                    .from("users")
                    .where("active = #")
                    .order("name ASC");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("WHERE"));
            assertTrue(query.contains("ORDER BY"));
        }

        @Test
        void order_afterGroupByHaving_generatesCorrectSQL() {
            SqlStatement stmt = SqlStatement.init()
                    .select("dept", "COUNT(*)")
                    .from("employees")
                    .groupBy("dept")
                    .having("COUNT(*) > 1")
                    .order("dept ASC");

            String query = stmt.finalizeQueryStatement();

            assertTrue(query.contains("GROUP BY"));
            assertTrue(query.contains("HAVING"));
            assertTrue(query.contains("ORDER BY"));
        }
    }

    // --- finalizeQueryStatement ---

    @Nested
    class FinalizeTests {

        @Test
        void finalizeQueryStatement_withoutPageable_returnsQuery() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users");

            String query = stmt.finalizeQueryStatement(null);

            assertNotNull(query);
            assertTrue(query.contains("SELECT"));
        }

        @Test
        void finalizeQueryStatement_noArgOverload_sameAsNullPageable() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users");

            String queryNoArg = stmt.finalizeQueryStatement();
            String queryNull = stmt.finalizeQueryStatement(null);

            assertEquals(queryNoArg, queryNull);
        }

        @Test
        void finalizeCountStatement_returnsCountQuery() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id", "name")
                    .from("users")
                    .where("active = #");

            String countQuery = stmt.finalizeCountStatement();

            assertTrue(countQuery.startsWith("SELECT"));
            assertTrue(countQuery.contains("COUNT(1)"));
            assertTrue(countQuery.contains("FROM"));
            assertTrue(countQuery.contains("WHERE"));
        }
    }

    // --- Exception / validation tests ---

    @Nested
    class ValidationTests {

        @Test
        void emptySelect_throwsException() {
            SqlStatement stmt = SqlStatement.init();

            assertThrows(EmptySelectStatementException.class, () -> stmt.select());
        }

        @Test
        void emptyFrom_throwsException() {
            SqlStatement stmt = SqlStatement.init().select("id");

            assertThrows(EmptyFromStatementException.class, () -> stmt.from());
        }

        @Test
        void emptyGroupBy_throwsException() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users");

            assertThrows(EmptyGroupByStatementException.class, () -> stmt.groupBy());
        }

        @Test
        void emptyOrder_throwsException() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users");

            assertThrows(EmptyOrderStatementException.class, () -> stmt.order());
        }

        @Test
        void emptyAnd_throwsException() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users")
                    .where("1 = 1");

            assertThrows(EmptyConditionStatementException.class, () -> stmt.and());
        }

        @Test
        void emptyOr_throwsException() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users")
                    .where("1 = 1");

            assertThrows(EmptyConditionStatementException.class, () -> stmt.or());
        }

        @Test
        void emptyAndOr_throwsException() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users")
                    .where("1 = 1");

            assertThrows(EmptyConditionStatementException.class, () -> stmt.andOr());
        }

        @Test
        void emptyOrAnd_throwsException() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users")
                    .where("1 = 1");

            assertThrows(EmptyConditionStatementException.class, () -> stmt.orAnd());
        }

        @Test
        void selectCalledTwice_throwsIllegalState() {
            SqlStatement stmt = SqlStatement.init().select("id");

            assertThrows(IllegalStateException.class, () -> stmt.select("name"));
        }

        @Test
        void fromBeforeSelect_throwsIllegalState() {
            SqlStatement stmt = SqlStatement.init();

            assertThrows(IllegalStateException.class, () -> stmt.from("users"));
        }

        @Test
        void whereBeforeFrom_throwsIllegalState() {
            SqlStatement stmt = SqlStatement.init().select("id");

            assertThrows(IllegalStateException.class, () -> stmt.where("1 = 1"));
        }

        @Test
        void andBeforeWhere_throwsIllegalState() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users");

            assertThrows(IllegalStateException.class, () -> stmt.and("x = 1"));
        }

        @Test
        void orBeforeWhere_throwsIllegalState() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users");

            assertThrows(IllegalStateException.class, () -> stmt.or("x = 1"));
        }

        @Test
        void havingBeforeGroupBy_throwsIllegalState() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users");

            assertThrows(IllegalStateException.class, () -> stmt.having("COUNT(*) > 1"));
        }

        @Test
        void groupByBeforeFrom_throwsIllegalState() {
            SqlStatement stmt = SqlStatement.init().select("id");

            assertThrows(IllegalStateException.class, () -> stmt.groupBy("id"));
        }

        @Test
        void orderBeforeFrom_throwsIllegalState() {
            SqlStatement stmt = SqlStatement.init().select("id");

            assertThrows(IllegalStateException.class, () -> stmt.order("id ASC"));
        }

        @Test
        void orderAfterOrder_throwsIllegalState() {
            SqlStatement stmt = SqlStatement.init()
                    .select("id")
                    .from("users")
                    .order("id ASC");

            assertThrows(IllegalStateException.class, () -> stmt.order("name DESC"));
        }
    }

    // --- toString ---

    @Test
    void toString_containsQueryAndCount() {
        SqlStatement stmt = SqlStatement.init()
                .select("id")
                .from("users");

        String str = stmt.toString();

        assertTrue(str.contains("SqlStatement{"));
        assertTrue(str.contains("query="));
        assertTrue(str.contains("count="));
    }
}
