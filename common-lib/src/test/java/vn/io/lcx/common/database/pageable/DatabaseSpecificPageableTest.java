package vn.io.lcx.common.database.pageable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Database-Specific Pageables")
class DatabaseSpecificPageableTest {

    // ========== PostgreSQLPageable ==========

    @Nested
    @DisplayName("PostgreSQLPageable")
    class PostgreSQLPageableTest {

        @Test
        void toSql_withPageAndSize_generatesOffsetLimitClause() {
            PostgreSQLPageable pageable = PostgreSQLPageable.builder()
                    .pageNumber(1)
                    .pageSize(10)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("OFFSET 0 LIMIT 10"));
        }

        @Test
        void toSql_page2_generatesCorrectOffset() {
            PostgreSQLPageable pageable = PostgreSQLPageable.builder()
                    .pageNumber(2)
                    .pageSize(20)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("OFFSET 20 LIMIT 20"));
        }

        @Test
        void toSql_page3_size5_generatesOffset10() {
            PostgreSQLPageable pageable = PostgreSQLPageable.builder()
                    .pageNumber(3)
                    .pageSize(5)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("OFFSET 10 LIMIT 5"));
        }

        @Test
        void toSql_withColumnSort_generatesOrderBy() {
            Map<String, Direction> sortMap = new LinkedHashMap<>();
            sortMap.put("name", Direction.ASC);
            PostgreSQLPageable pageable = PostgreSQLPageable.builder()
                    .pageNumber(1)
                    .pageSize(10)
                    .columnNameAndDirectionMap(sortMap)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("ORDER BY"));
            assertTrue(sql.contains("name ASC"));
            assertTrue(sql.contains("OFFSET 0 LIMIT 10"));
        }

        @Test
        void toSql_withMultipleColumnSort_generatesOrderByWithMultipleColumns() {
            Map<String, Direction> sortMap = new LinkedHashMap<>();
            sortMap.put("name", Direction.ASC);
            sortMap.put("age", Direction.DESC);
            PostgreSQLPageable pageable = PostgreSQLPageable.builder()
                    .pageNumber(1)
                    .pageSize(10)
                    .columnNameAndDirectionMap(sortMap)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("ORDER BY"));
            assertTrue(sql.contains("name ASC"));
            assertTrue(sql.contains("age DESC"));
        }

        @Test
        void toSql_noPageNoSize_noOffsetClause() {
            PostgreSQLPageable pageable = PostgreSQLPageable.builder()
                    .pageNumber(0)
                    .pageSize(0)
                    .build();
            String sql = pageable.toSql();
            assertFalse(sql.contains("OFFSET"));
            assertFalse(sql.contains("LIMIT"));
        }

        @Test
        void builder_setsAllFields() {
            Map<String, Direction> colMap = Map.of("id", Direction.ASC);
            Map<String, Direction> fieldMap = Map.of("name", Direction.DESC);
            PostgreSQLPageable pageable = PostgreSQLPageable.builder()
                    .pageNumber(3)
                    .pageSize(25)
                    .columnNameAndDirectionMap(colMap)
                    .fieldNameAndDirectionMap(fieldMap)
                    .entityClass(String.class)
                    .build();
            assertEquals(3, pageable.getPageNumber());
            assertEquals(25, pageable.getPageSize());
            assertEquals(colMap, pageable.getColumnNameAndDirectionMap());
            assertEquals(fieldMap, pageable.getFieldNameAndDirectionMap());
            assertEquals(String.class, pageable.getEntityClass());
        }

        @Test
        void addNewColumnAndDirectionOrder_initializesMapIfNull() {
            PostgreSQLPageable pageable = new PostgreSQLPageable();
            assertNull(pageable.getColumnNameAndDirectionMap());
            pageable.addNewColumnAndDirectionOrder("col1", Direction.ASC);
            assertNotNull(pageable.getColumnNameAndDirectionMap());
            assertEquals(Direction.ASC, pageable.getColumnNameAndDirectionMap().get("col1"));
        }

        @Test
        void add_initializesFieldMapIfNull() {
            PostgreSQLPageable pageable = new PostgreSQLPageable();
            assertNull(pageable.getFieldNameAndDirectionMap());
            Pageable result = pageable.add("field1", Direction.DESC);
            assertNotNull(pageable.getFieldNameAndDirectionMap());
            assertEquals(Direction.DESC, pageable.getFieldNameAndDirectionMap().get("field1"));
            assertSame(pageable, result);
        }

        @Test
        void setters_workCorrectly() {
            PostgreSQLPageable pageable = new PostgreSQLPageable();
            pageable.setPageNumber(5);
            pageable.setPageSize(50);
            pageable.setEntityClass(Integer.class);
            Map<String, Direction> colMap = Map.of("c1", Direction.ASC);
            pageable.setColumnNameAndDirectionMap(colMap);
            Map<String, Direction> fieldMap = Map.of("f1", Direction.DESC);
            pageable.setFieldNameAndDirectionMap(fieldMap);
            assertEquals(5, pageable.getPageNumber());
            assertEquals(50, pageable.getPageSize());
            assertEquals(Integer.class, pageable.getEntityClass());
            assertEquals(colMap, pageable.getColumnNameAndDirectionMap());
            assertEquals(fieldMap, pageable.getFieldNameAndDirectionMap());
        }
    }

    // ========== MySqlPageable ==========

    @Nested
    @DisplayName("MySqlPageable")
    class MySqlPageableTest {

        @Test
        void toSql_withPageAndSize_generatesLimitOffset() {
            MySqlPageable pageable = MySqlPageable.builder()
                    .pageNumber(1)
                    .pageSize(10)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("LIMIT 10 OFFSET 0"));
        }

        @Test
        void toSql_page2_generatesCorrectOffset() {
            MySqlPageable pageable = MySqlPageable.builder()
                    .pageNumber(2)
                    .pageSize(15)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("LIMIT 15 OFFSET 15"));
        }

        @Test
        void toSql_page0_pageSize10_throwsIllegalArgument() {
            MySqlPageable pageable = MySqlPageable.builder()
                    .pageNumber(0)
                    .pageSize(10)
                    .build();
            assertThrows(IllegalArgumentException.class, pageable::toSql);
        }

        @Test
        void toSql_withColumnSort_generatesOrderBy() {
            Map<String, Direction> sortMap = new LinkedHashMap<>();
            sortMap.put("created_at", Direction.DESC);
            MySqlPageable pageable = MySqlPageable.builder()
                    .pageNumber(1)
                    .pageSize(10)
                    .columnNameAndDirectionMap(sortMap)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("ORDER BY"));
            assertTrue(sql.contains("created_at DESC"));
        }

        @Test
        void toSql_noPaging_noLimitOffset() {
            MySqlPageable pageable = MySqlPageable.builder()
                    .pageNumber(0)
                    .pageSize(0)
                    .build();
            String sql = pageable.toSql();
            assertFalse(sql.contains("LIMIT"));
            assertFalse(sql.contains("OFFSET"));
        }

        @Test
        void addNewColumnAndDirectionOrder_initializesMapIfNull() {
            MySqlPageable pageable = new MySqlPageable();
            pageable.addNewColumnAndDirectionOrder("col1", Direction.ASC);
            assertNotNull(pageable.getColumnNameAndDirectionMap());
            assertEquals(Direction.ASC, pageable.getColumnNameAndDirectionMap().get("col1"));
        }

        @Test
        void add_initializesFieldMapIfNull() {
            MySqlPageable pageable = new MySqlPageable();
            Pageable result = pageable.add("field1", Direction.DESC);
            assertNotNull(pageable.getFieldNameAndDirectionMap());
            assertSame(pageable, result);
        }
    }

    // ========== OraclePageable ==========

    @Nested
    @DisplayName("OraclePageable")
    class OraclePageableTest {

        @Test
        void toSql_withPageAndSize_generatesOffsetFetch() {
            OraclePageable pageable = OraclePageable.builder()
                    .pageNumber(1)
                    .pageSize(10)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY"));
        }

        @Test
        void toSql_page3_generatesCorrectOffset() {
            OraclePageable pageable = OraclePageable.builder()
                    .pageNumber(3)
                    .pageSize(20)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("OFFSET 40 ROWS FETCH NEXT 20 ROWS ONLY"));
        }

        @Test
        void toSql_page0_pageSize5_throwsIllegalArgument() {
            OraclePageable pageable = OraclePageable.builder()
                    .pageNumber(0)
                    .pageSize(5)
                    .build();
            assertThrows(IllegalArgumentException.class, pageable::toSql);
        }

        @Test
        void toSql_withColumnSort_generatesOrderBy() {
            Map<String, Direction> sortMap = new LinkedHashMap<>();
            sortMap.put("id", Direction.ASC);
            OraclePageable pageable = OraclePageable.builder()
                    .pageNumber(1)
                    .pageSize(10)
                    .columnNameAndDirectionMap(sortMap)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("ORDER BY"));
            assertTrue(sql.contains("id ASC"));
        }

        @Test
        void toSql_noPaging_noOffsetFetch() {
            OraclePageable pageable = OraclePageable.builder()
                    .pageNumber(0)
                    .pageSize(0)
                    .build();
            String sql = pageable.toSql();
            assertFalse(sql.contains("OFFSET"));
            assertFalse(sql.contains("FETCH"));
        }

        @Test
        void addNewColumnAndDirectionOrder_initializesMapIfNull() {
            OraclePageable pageable = new OraclePageable();
            pageable.addNewColumnAndDirectionOrder("col1", Direction.DESC);
            assertNotNull(pageable.getColumnNameAndDirectionMap());
            assertEquals(Direction.DESC, pageable.getColumnNameAndDirectionMap().get("col1"));
        }

        @Test
        void add_initializesFieldMapIfNull() {
            OraclePageable pageable = new OraclePageable();
            Pageable result = pageable.add("field1", Direction.ASC);
            assertNotNull(pageable.getFieldNameAndDirectionMap());
            assertSame(pageable, result);
        }
    }

    // ========== MSSQLPageable ==========

    @Nested
    @DisplayName("MSSQLPageable")
    class MSSQLPageableTest {

        @Test
        void toSql_withPageAndSize_generatesOffsetFetch() {
            MSSQLPageable pageable = MSSQLPageable.builder()
                    .pageNumber(1)
                    .pageSize(10)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY"));
        }

        @Test
        void toSql_page2_generatesCorrectOffset() {
            MSSQLPageable pageable = MSSQLPageable.builder()
                    .pageNumber(2)
                    .pageSize(25)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("OFFSET 25 ROWS FETCH NEXT 25 ROWS ONLY"));
        }

        @Test
        void toSql_page0_pageSize10_throwsIllegalArgument() {
            MSSQLPageable pageable = MSSQLPageable.builder()
                    .pageNumber(0)
                    .pageSize(10)
                    .build();
            assertThrows(IllegalArgumentException.class, pageable::toSql);
        }

        @Test
        void toSql_withColumnSort_generatesOrderBy() {
            Map<String, Direction> sortMap = new LinkedHashMap<>();
            sortMap.put("updated_at", Direction.DESC);
            MSSQLPageable pageable = MSSQLPageable.builder()
                    .pageNumber(1)
                    .pageSize(10)
                    .columnNameAndDirectionMap(sortMap)
                    .build();
            String sql = pageable.toSql();
            assertTrue(sql.contains("ORDER BY"));
            assertTrue(sql.contains("updated_at DESC"));
        }

        @Test
        void toSql_noPaging_noOffsetFetch() {
            MSSQLPageable pageable = MSSQLPageable.builder()
                    .pageNumber(0)
                    .pageSize(0)
                    .build();
            String sql = pageable.toSql();
            assertFalse(sql.contains("OFFSET"));
            assertFalse(sql.contains("FETCH"));
        }

        @Test
        void addNewColumnAndDirectionOrder_initializesMapIfNull() {
            MSSQLPageable pageable = new MSSQLPageable();
            pageable.addNewColumnAndDirectionOrder("col1", Direction.ASC);
            assertNotNull(pageable.getColumnNameAndDirectionMap());
            assertEquals(Direction.ASC, pageable.getColumnNameAndDirectionMap().get("col1"));
        }

        @Test
        void add_initializesFieldMapIfNull() {
            MSSQLPageable pageable = new MSSQLPageable();
            Pageable result = pageable.add("field1", Direction.DESC);
            assertNotNull(pageable.getFieldNameAndDirectionMap());
            assertSame(pageable, result);
        }

        @Test
        void builder_setsAllFields() {
            Map<String, Direction> colMap = Map.of("id", Direction.ASC);
            Map<String, Direction> fieldMap = Map.of("name", Direction.DESC);
            MSSQLPageable pageable = MSSQLPageable.builder()
                    .pageNumber(4)
                    .pageSize(30)
                    .columnNameAndDirectionMap(colMap)
                    .fieldNameAndDirectionMap(fieldMap)
                    .entityClass(Long.class)
                    .build();
            assertEquals(4, pageable.getPageNumber());
            assertEquals(30, pageable.getPageSize());
            assertEquals(colMap, pageable.getColumnNameAndDirectionMap());
            assertEquals(fieldMap, pageable.getFieldNameAndDirectionMap());
            assertEquals(Long.class, pageable.getEntityClass());
        }
    }

    // ========== Direction Enum ==========

    @Nested
    @DisplayName("Direction Enum")
    class DirectionTest {

        @Test
        void values_containsAscAndDesc() {
            Direction[] values = Direction.values();
            assertEquals(2, values.length);
            assertEquals(Direction.ASC, Direction.valueOf("ASC"));
            assertEquals(Direction.DESC, Direction.valueOf("DESC"));
        }
    }
}
