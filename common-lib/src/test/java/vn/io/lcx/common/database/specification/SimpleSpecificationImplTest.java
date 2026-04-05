package vn.io.lcx.common.database.specification;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vn.io.lcx.common.annotation.ColumnName;
import vn.io.lcx.common.annotation.TableName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleSpecificationImplTest {

    /**
     * Test entity: table name "test_entity" -> shortened to "te"
     * Fields:
     *   - "id" -> no @ColumnName -> camelToConstant -> "ID"
     *   - "userName" -> @ColumnName(name="USER_NAME") -> "USER_NAME"
     *   - "age" -> no @ColumnName -> camelToConstant -> "AGE"
     *   - "email" -> @ColumnName(name="EMAIL_ADDR") -> "EMAIL_ADDR"
     *   - "status" -> no @ColumnName -> camelToConstant -> "STATUS"
     *   - "createdDate" -> no @ColumnName -> camelToConstant -> "CREATED_DATE"
     */
    @TableName("test_entity")
    static class TestEntity {
        private Long id;

        @ColumnName(name = "USER_NAME")
        private String userName;

        private Integer age;

        @ColumnName(name = "EMAIL_ADDR")
        private String email;

        private String status;

        private String createdDate;
    }

    // Table shortened name for "test_entity" is "te" (first letter of each underscore-separated part)

    @Nested
    class Equal {

        @Test
        void generatesEqualClause() {
            Specification spec = Specification.create(TestEntity.class);
            spec.equal("userName", "Alice");

            assertEquals("te.USER_NAME = ?", spec.getFinalSQL().toString());
            assertEquals(1, spec.getParameters().size());
            assertEquals("Alice", spec.getParameters().get(0));
        }

        @Test
        void throwsForNonExistentField() {
            Specification spec = Specification.create(TestEntity.class);
            assertThrows(IllegalArgumentException.class,
                    () -> spec.equal("nonExistent", "value"));
        }
    }

    @Nested
    class NotEqual {

        @Test
        void generatesNotEqualClause() {
            Specification spec = Specification.create(TestEntity.class);
            spec.notEqual("status", "INACTIVE");

            assertEquals("te.STATUS <> ?", spec.getFinalSQL().toString());
            assertEquals(1, spec.getParameters().size());
            assertEquals("INACTIVE", spec.getParameters().get(0));
        }
    }

    @Nested
    class Like {

        @Test
        void generatesLikeClause() {
            Specification spec = Specification.create(TestEntity.class);
            spec.like("userName", "%Alice%");

            assertEquals("te.USER_NAME LIKE ?", spec.getFinalSQL().toString());
            assertEquals(1, spec.getParameters().size());
            assertEquals("%Alice%", spec.getParameters().get(0));
        }
    }

    @Nested
    class In {

        @Test
        void generatesInClause() {
            Specification spec = Specification.create(TestEntity.class);
            List<Object> values = Arrays.asList("ACTIVE", "PENDING", "APPROVED");
            spec.in("status", values);

            assertEquals("te.STATUS IN (?, ?, ?)", spec.getFinalSQL().toString());
            assertEquals(3, spec.getParameters().size());
            assertEquals("ACTIVE", spec.getParameters().get(0));
            assertEquals("PENDING", spec.getParameters().get(1));
            assertEquals("APPROVED", spec.getParameters().get(2));
        }

        @Test
        void generatesSingleValueInClause() {
            Specification spec = Specification.create(TestEntity.class);
            List<Object> values = Arrays.asList("ACTIVE");
            spec.in("status", values);

            assertEquals("te.STATUS IN (?)", spec.getFinalSQL().toString());
            assertEquals(1, spec.getParameters().size());
        }
    }

    @Nested
    class Between {

        @Test
        void generatesBetweenClause() {
            Specification spec = Specification.create(TestEntity.class);
            spec.between("age", 18, 65);

            assertEquals("te.AGE BETWEEN ? AND ?", spec.getFinalSQL().toString());
            assertEquals(2, spec.getParameters().size());
            assertEquals(18, spec.getParameters().get(0));
            assertEquals(65, spec.getParameters().get(1));
        }
    }

    @Nested
    class IsNull {

        @Test
        void generatesIsNullClause() {
            Specification spec = Specification.create(TestEntity.class);
            spec.isNull("email");

            assertEquals("te.EMAIL_ADDR IS NULL", spec.getFinalSQL().toString());
            assertTrue(spec.getParameters().isEmpty());
        }
    }

    @Nested
    class IsNotNull {

        @Test
        void generatesIsNotNullClause() {
            Specification spec = Specification.create(TestEntity.class);
            spec.isNotNull("email");

            assertEquals("te.EMAIL_ADDR IS NOT NULL", spec.getFinalSQL().toString());
            assertTrue(spec.getParameters().isEmpty());
        }
    }

    @Nested
    class GreaterThan {

        @Test
        void generatesGreaterThanClause() {
            Specification spec = Specification.create(TestEntity.class);
            spec.greaterThan("age", 18);

            assertEquals("te.AGE > ?", spec.getFinalSQL().toString());
            assertEquals(1, spec.getParameters().size());
            assertEquals(18, spec.getParameters().get(0));
        }
    }

    @Nested
    class GreaterThanOrEqual {

        @Test
        void generatesGreaterThanOrEqualClause() {
            Specification spec = Specification.create(TestEntity.class);
            spec.greaterThanOrEqual("age", 21);

            assertEquals("te.AGE >= ?", spec.getFinalSQL().toString());
            assertEquals(1, spec.getParameters().size());
            assertEquals(21, spec.getParameters().get(0));
        }
    }

    @Nested
    class LessThan {

        @Test
        void generatesLessThanClause() {
            Specification spec = Specification.create(TestEntity.class);
            spec.lessThan("age", 30);

            assertEquals("te.AGE < ?", spec.getFinalSQL().toString());
            assertEquals(1, spec.getParameters().size());
            assertEquals(30, spec.getParameters().get(0));
        }
    }

    @Nested
    class LessThanOrEqual {

        @Test
        void generatesLessThanOrEqualClause() {
            Specification spec = Specification.create(TestEntity.class);
            spec.lessThanOrEqual("age", 50);

            assertEquals("te.AGE <= ?", spec.getFinalSQL().toString());
            assertEquals(1, spec.getParameters().size());
            assertEquals(50, spec.getParameters().get(0));
        }
    }

    @Nested
    class AndChaining {

        @Test
        void chainsWithAndUsingFieldNameAndValue() {
            Specification spec = Specification.create(TestEntity.class);
            spec.where("userName", "Alice")
                    .and("status", "ACTIVE");

            String sql = spec.getFinalSQL().toString();
            assertTrue(sql.contains("te.USER_NAME = ?"));
            assertTrue(sql.contains(" AND "));
            assertTrue(sql.contains("te.STATUS = ?"));
            assertEquals(2, spec.getParameters().size());
        }

        @Test
        void chainsWithAndUsingSpecification() {
            Specification spec1 = Specification.create(TestEntity.class);
            spec1.equal("userName", "Alice");

            Specification spec2 = Specification.create(TestEntity.class);
            spec2.equal("status", "ACTIVE");

            Specification combined = Specification.create(TestEntity.class);
            combined.where(spec1).and(spec2);

            String sql = combined.getFinalSQL().toString();
            assertTrue(sql.contains("te.USER_NAME = ?"));
            assertTrue(sql.contains("te.STATUS = ?"));
            assertEquals(2, combined.getParameters().size());
        }
    }

    @Nested
    class OrChaining {

        @Test
        void chainsWithOrUsingFieldNameAndValue() {
            Specification spec = Specification.create(TestEntity.class);
            spec.where("status", "ACTIVE")
                    .or("status", "PENDING");

            String sql = spec.getFinalSQL().toString();
            assertTrue(sql.contains("te.STATUS = ?"));
            assertTrue(sql.contains(" OR "));
            assertEquals(2, spec.getParameters().size());
        }

        @Test
        void chainsWithOrUsingSpecification() {
            Specification spec1 = Specification.create(TestEntity.class);
            spec1.equal("userName", "Alice");

            Specification spec2 = Specification.create(TestEntity.class);
            spec2.equal("userName", "Bob");

            Specification combined = Specification.create(TestEntity.class);
            combined.where(spec1).or(spec2);

            String sql = combined.getFinalSQL().toString();
            assertTrue(sql.contains("te.USER_NAME = ?"));
            assertTrue(sql.contains(" OR "));
            assertEquals(2, combined.getParameters().size());
        }
    }

    @Nested
    class GetFinalSQLAndGetParameters {

        @Test
        void initiallyEmpty() {
            Specification spec = Specification.create(TestEntity.class);
            assertEquals("", spec.getFinalSQL().toString());
            assertTrue(spec.getParameters().isEmpty());
            assertEquals(0, spec.getTimes());
        }

        @Test
        void timesIncrementWithEachOperation() {
            Specification spec = Specification.create(TestEntity.class);
            spec.equal("userName", "Alice");
            assertEquals(1, spec.getTimes());
        }

        @Test
        void whereIncrementsTimes() {
            Specification spec = Specification.create(TestEntity.class);
            spec.where("userName", "Alice");
            // where calls equal internally which increments once, plus where increments once
            assertTrue(spec.getTimes() >= 1);
        }

        @Test
        void complexQueryBuildsCorrectly() {
            Specification spec = Specification.create(TestEntity.class);
            spec.equal("userName", "Alice");
            spec.and("age", 25);

            String sql = spec.getFinalSQL().toString();
            assertTrue(sql.contains("te.USER_NAME = ?"));
            assertTrue(sql.contains("AND"));
            assertTrue(sql.contains("te.AGE = ?"));

            List<Object> params = spec.getParameters();
            assertEquals(2, params.size());
            assertEquals("Alice", params.get(0));
            assertEquals(25, params.get(1));
        }
    }

    @Nested
    class WhereWithSpecification {

        @Test
        void wrapsInParenthesesWhenFinalSqlNotEmpty() {
            Specification inner = Specification.create(TestEntity.class);
            inner.equal("status", "ACTIVE");

            Specification outer = Specification.create(TestEntity.class);
            outer.equal("userName", "Alice");
            outer.where(inner);

            String sql = outer.getFinalSQL().toString();
            // When finalSQL is not empty, where(spec) wraps input in parens
            assertTrue(sql.contains("(te.STATUS = ?)"));
        }

        @Test
        void doesNotWrapWhenFinalSqlIsEmpty() {
            Specification inner = Specification.create(TestEntity.class);
            inner.equal("status", "ACTIVE");

            Specification outer = Specification.create(TestEntity.class);
            outer.where(inner);

            String sql = outer.getFinalSQL().toString();
            assertEquals("te.STATUS = ?", sql);
        }
    }

    @Nested
    class AndOrWithSpecificationGrouping {

        @Test
        void andGroupsMultipleTimesSpecInParentheses() {
            // If specification has times >= 2, it gets wrapped in ( )
            Specification inner = Specification.create(TestEntity.class);
            inner.equal("userName", "Alice");
            inner.equal("status", "ACTIVE"); // times == 2

            Specification outer = Specification.create(TestEntity.class);
            outer.equal("age", 25);
            outer.and(inner);

            String sql = outer.getFinalSQL().toString();
            assertTrue(sql.contains("( te.USER_NAME = ?te.STATUS = ? )"));
        }

        @Test
        void orGroupsMultipleTimesSpecInParentheses() {
            Specification inner = Specification.create(TestEntity.class);
            inner.equal("userName", "Alice");
            inner.equal("status", "ACTIVE"); // times == 2

            Specification outer = Specification.create(TestEntity.class);
            outer.equal("age", 25);
            outer.or(inner);

            String sql = outer.getFinalSQL().toString();
            assertTrue(sql.contains("( te.USER_NAME = ?te.STATUS = ? )"));
        }
    }

    @Nested
    class UsingAnnotatedColumnName {

        @Test
        void usesAnnotatedColumnNameForEmail() {
            Specification spec = Specification.create(TestEntity.class);
            spec.equal("email", "alice@example.com");

            assertEquals("te.EMAIL_ADDR = ?", spec.getFinalSQL().toString());
        }

        @Test
        void usesCamelToConstantForNonAnnotatedField() {
            Specification spec = Specification.create(TestEntity.class);
            spec.equal("createdDate", "2024-01-01");

            assertEquals("te.CREATED_DATE = ?", spec.getFinalSQL().toString());
        }
    }
}
