package vn.io.lcx.processor.utility;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ReactiveCodeGenHelper}.
 * <p>
 * ReactiveCodeGenHelper generates code strings for reactive repository implementations.
 * We test the static methods that produce deterministic code output without requiring
 * a compilation environment.
 */
class ReactiveCodeGenHelperTest {

    // ------------------------------------------------------------------ //
    //  addPlaceholderResolution
    // ------------------------------------------------------------------ //

    @Nested
    class AddPlaceholderResolution {

        @Test
        void generatesPlaceholderIfElseChain() {
            List<String> codeLines = new ArrayList<>();

            ReactiveCodeGenHelper.addPlaceholderResolution(codeLines);

            assertFalse(codeLines.isEmpty());
            assertEquals("String placeholder;", codeLines.get(0));
        }

        @Test
        void containsAllDatabaseBranches() {
            List<String> codeLines = new ArrayList<>();

            ReactiveCodeGenHelper.addPlaceholderResolution(codeLines);

            String joined = String.join("\n", codeLines);
            assertTrue(joined.contains("PostgreSQL"), "Should contain PostgreSQL branch");
            assertTrue(joined.contains("MySQL"), "Should contain MySQL branch");
            assertTrue(joined.contains("MariaDB"), "Should contain MariaDB branch");
            assertTrue(joined.contains("Microsoft SQL Server"), "Should contain MSSQL branch");
            assertTrue(joined.contains("Oracle"), "Should contain Oracle branch");
        }

        @Test
        void containsCorrectPlaceholderAssignments() {
            List<String> codeLines = new ArrayList<>();

            ReactiveCodeGenHelper.addPlaceholderResolution(codeLines);

            String joined = String.join("\n", codeLines);
            assertTrue(joined.contains("placeholder = \"$\""), "PostgreSQL uses $");
            assertTrue(joined.contains("placeholder = \"?\""), "MySQL/Oracle use ?");
            assertTrue(joined.contains("placeholder = \"@p\""), "MSSQL uses @p");
        }

        @Test
        void endsWithUnsupportedDatabaseThrow() {
            List<String> codeLines = new ArrayList<>();

            ReactiveCodeGenHelper.addPlaceholderResolution(codeLines);

            String lastLine = codeLines.get(codeLines.size() - 1);
            assertEquals("}", lastLine);

            String joined = String.join("\n", codeLines);
            assertTrue(joined.contains("Unsupported database type"));
        }

        @Test
        void appendsToExistingList() {
            List<String> codeLines = new ArrayList<>();
            codeLines.add("// existing code");

            ReactiveCodeGenHelper.addPlaceholderResolution(codeLines);

            assertEquals("// existing code", codeLines.get(0));
            assertTrue(codeLines.size() > 1);
        }
    }

    // ------------------------------------------------------------------ //
    //  addStartingTimeCode
    // ------------------------------------------------------------------ //

    @Nested
    class AddStartingTimeCode {

        @Test
        void addsSystemCurrentTimeMillisLine() {
            List<String> codeLines = new ArrayList<>();

            ReactiveCodeGenHelper.addStartingTimeCode(codeLines);

            assertEquals(1, codeLines.size());
            assertTrue(codeLines.get(0).contains("System.currentTimeMillis()"));
            assertTrue(codeLines.get(0).contains("startingTime"));
            assertTrue(codeLines.get(0).contains("final double"));
        }
    }

    // ------------------------------------------------------------------ //
    //  addDurationLogging
    // ------------------------------------------------------------------ //

    @Nested
    class AddDurationLogging {

        @Test
        void addsTwoLinesWithoutIndentation() {
            List<String> codeLines = new ArrayList<>();

            ReactiveCodeGenHelper.addDurationLogging(codeLines, "ctx");

            assertEquals(2, codeLines.size());
            assertTrue(codeLines.get(0).contains("duration"));
            assertTrue(codeLines.get(0).contains("startingTime"));
            assertTrue(codeLines.get(1).contains("LogUtils.writeLog"));
            assertTrue(codeLines.get(1).contains("ctx"));
        }

        @Test
        void addsTwoLinesWithCustomIndentation() {
            List<String> codeLines = new ArrayList<>();

            ReactiveCodeGenHelper.addDurationLogging(codeLines, "routingCtx", 8);

            assertEquals(2, codeLines.size());
            // Should start with 8 spaces
            assertTrue(codeLines.get(0).startsWith("        "));
            assertTrue(codeLines.get(1).startsWith("        "));
            assertTrue(codeLines.get(1).contains("routingCtx"));
        }

        @Test
        void zeroIndentationProducesNoLeadingSpaces() {
            List<String> codeLines = new ArrayList<>();

            ReactiveCodeGenHelper.addDurationLogging(codeLines, "ctx", 0);

            assertFalse(codeLines.get(0).startsWith(" "));
        }

        @Test
        void containsTraceLogLevel() {
            List<String> codeLines = new ArrayList<>();

            ReactiveCodeGenHelper.addDurationLogging(codeLines, "ctx");

            String logLine = codeLines.get(1);
            assertTrue(logLine.contains("LogUtils.Level.TRACE"));
        }

        @Test
        void containsSqlDurationMessage() {
            List<String> codeLines = new ArrayList<>();

            ReactiveCodeGenHelper.addDurationLogging(codeLines, "ctx");

            String logLine = codeLines.get(1);
            assertTrue(logLine.contains("Executed SQL in {} ms"));
        }
    }

    // ------------------------------------------------------------------ //
    //  getPlaceholderForDatabase
    // ------------------------------------------------------------------ //

    @Nested
    class GetPlaceholderForDatabase {

        @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
        @CsvSource({
                "PostgreSQL,           $",
                "MySQL,                ?",
                "MariaDB,              ?",
                "Microsoft SQL Server, @p",
                "Oracle,               ?"
        })
        void returnsCorrectPlaceholder(String databaseName, String expectedPlaceholder) {
            assertEquals(expectedPlaceholder,
                    ReactiveCodeGenHelper.getPlaceholderForDatabase(databaseName));
        }

        @Test
        void throwsForUnknownDatabase() {
            assertThrows(IllegalArgumentException.class,
                    () -> ReactiveCodeGenHelper.getPlaceholderForDatabase("H2"));
        }
    }

    // ------------------------------------------------------------------ //
    //  generateDatabaseSpecificCrudCode
    // ------------------------------------------------------------------ //

    @Nested
    class GenerateDatabaseSpecificCrudCode {

        @Test
        void insertCodeContainsStartingTimeAndDatabaseBranches() {
            List<String> code = ReactiveCodeGenHelper.generateDatabaseSpecificCrudCode(
                    "conn", "ctx", "MyEntity",
                    ReactiveCodeGenHelper.CrudOperationType.INSERT);

            assertNotNull(code);
            assertFalse(code.isEmpty());

            String joined = String.join("\n", code);
            assertTrue(joined.contains("startingTime"));
            assertTrue(joined.contains("PostgreSQL"));
            assertTrue(joined.contains("MySQL"));
            assertTrue(joined.contains("Microsoft SQL Server"));
            assertTrue(joined.contains("Oracle"));
            assertTrue(joined.contains("MyEntityUtils"));
        }

        @Test
        void updateCodeContainsUpdateStatementReferences() {
            List<String> code = ReactiveCodeGenHelper.generateDatabaseSpecificCrudCode(
                    "conn", "ctx", "User",
                    ReactiveCodeGenHelper.CrudOperationType.UPDATE);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("reactiveUpdateStatement"));
            assertTrue(joined.contains("updateTupleParam"));
            assertTrue(joined.contains("UserUtils"));
        }

        @Test
        void deleteCodeContainsDeleteStatementReferences() {
            List<String> code = ReactiveCodeGenHelper.generateDatabaseSpecificCrudCode(
                    "conn", "ctx", "Order",
                    ReactiveCodeGenHelper.CrudOperationType.DELETE);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("reactiveDeleteStatement"));
            assertTrue(joined.contains("deleteTupleParam"));
            assertTrue(joined.contains("OrderUtils"));
        }

        @Test
        void throwsForBatchOperationType() {
            assertThrows(IllegalArgumentException.class,
                    () -> ReactiveCodeGenHelper.generateDatabaseSpecificCrudCode(
                            "conn", "ctx", "Entity",
                            ReactiveCodeGenHelper.CrudOperationType.BATCH_INSERT));
        }

        @Test
        void insertCodeContainsReturningClauseForPostgres() {
            List<String> code = ReactiveCodeGenHelper.generateDatabaseSpecificCrudCode(
                    "conn", "ctx", "MyEntity",
                    ReactiveCodeGenHelper.CrudOperationType.INSERT);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("returning"));
        }

        @Test
        void insertCodeContainsMySqlIdRowExtract() {
            List<String> code = ReactiveCodeGenHelper.generateDatabaseSpecificCrudCode(
                    "conn", "ctx", "MyEntity",
                    ReactiveCodeGenHelper.CrudOperationType.INSERT);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("mySqlIdRowExtract"));
        }

        @Test
        void insertCodeContainsOracleAutoGeneratedKeys() {
            List<String> code = ReactiveCodeGenHelper.generateDatabaseSpecificCrudCode(
                    "conn", "ctx", "MyEntity",
                    ReactiveCodeGenHelper.CrudOperationType.INSERT);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("OraclePrepareOptions"));
            assertTrue(joined.contains("AutoGeneratedKeysIndexes"));
        }

        @Test
        void updateCodeEndsWithReturnAndDurationLogging() {
            List<String> code = ReactiveCodeGenHelper.generateDatabaseSpecificCrudCode(
                    "conn", "ctx", "Item",
                    ReactiveCodeGenHelper.CrudOperationType.UPDATE);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("return future.map(it -> {"));
            assertTrue(joined.contains("duration"));
        }

        @Test
        void usesCorrectConnectionAndContextVarNames() {
            List<String> code = ReactiveCodeGenHelper.generateDatabaseSpecificCrudCode(
                    "sqlConn", "routingContext", "Product",
                    ReactiveCodeGenHelper.CrudOperationType.INSERT);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("sqlConn"));
            assertTrue(joined.contains("routingContext"));
        }
    }

    // ------------------------------------------------------------------ //
    //  generateBatchCrudCode
    // ------------------------------------------------------------------ //

    @Nested
    class GenerateBatchCrudCode {

        @Test
        void batchInsertCodeContainsSequentialCompose() {
            List<String> code = ReactiveCodeGenHelper.generateBatchCrudCode(
                    "conn", "ctx", "MyEntity",
                    ReactiveCodeGenHelper.CrudOperationType.BATCH_INSERT);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("chain = chain.compose"));
            assertTrue(joined.contains("save("));
            assertTrue(joined.contains("results.add"));
        }

        @Test
        void batchInsertHandlesEmptyEntities() {
            List<String> code = ReactiveCodeGenHelper.generateBatchCrudCode(
                    "conn", "ctx", "MyEntity",
                    ReactiveCodeGenHelper.CrudOperationType.BATCH_INSERT);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("entities == null || entities.isEmpty()"));
            assertTrue(joined.contains("emptyList()"));
        }

        @Test
        void batchUpdateCodeContainsExecuteBatch() {
            List<String> code = ReactiveCodeGenHelper.generateBatchCrudCode(
                    "conn", "ctx", "User",
                    ReactiveCodeGenHelper.CrudOperationType.BATCH_UPDATE);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("executeBatch(batch)"));
            assertTrue(joined.contains("reactiveUpdateStatement"));
            assertTrue(joined.contains("UserUtils"));
        }

        @Test
        void batchDeleteCodeContainsExecuteBatch() {
            List<String> code = ReactiveCodeGenHelper.generateBatchCrudCode(
                    "conn", "ctx", "Order",
                    ReactiveCodeGenHelper.CrudOperationType.BATCH_DELETE);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("executeBatch(batch)"));
            assertTrue(joined.contains("reactiveDeleteStatement"));
            assertTrue(joined.contains("deleteTupleParam"));
        }

        @Test
        void throwsForNonBatchOperationType() {
            assertThrows(IllegalArgumentException.class,
                    () -> ReactiveCodeGenHelper.generateBatchCrudCode(
                            "conn", "ctx", "Entity",
                            ReactiveCodeGenHelper.CrudOperationType.INSERT));
        }

        @Test
        void batchUpdateContainsPlaceholderResolution() {
            List<String> code = ReactiveCodeGenHelper.generateBatchCrudCode(
                    "conn", "ctx", "MyEntity",
                    ReactiveCodeGenHelper.CrudOperationType.BATCH_UPDATE);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("String placeholder;"));
            assertTrue(joined.contains("PostgreSQL"));
        }

        @Test
        void batchUpdateContainsRowCountAggregation() {
            List<String> code = ReactiveCodeGenHelper.generateBatchCrudCode(
                    "conn", "ctx", "MyEntity",
                    ReactiveCodeGenHelper.CrudOperationType.BATCH_UPDATE);

            String joined = String.join("\n", code);
            assertTrue(joined.contains("totalCount"));
            assertTrue(joined.contains("current.rowCount()"));
            assertTrue(joined.contains("current = current.next()"));
        }
    }

    // ------------------------------------------------------------------ //
    //  subListFromKeyword
    // ------------------------------------------------------------------ //

    @Nested
    class SubListFromKeyword {

        @Test
        void returnsSublistStartingFromMatchingElement() {
            List<String> keywords = Arrays.asList("SELECT", "FROM", "WHERE", "ORDER BY");

            List<String> result = ReactiveCodeGenHelper.subListFromKeyword(keywords, "where");

            assertEquals(2, result.size());
            assertEquals("WHERE", result.get(0));
            assertEquals("ORDER BY", result.get(1));
        }

        @Test
        void isCaseInsensitiveMatch() {
            List<String> keywords = Arrays.asList("select", "from", "WHERE");

            List<String> result = ReactiveCodeGenHelper.subListFromKeyword(keywords, "FROM");

            assertEquals(2, result.size());
            assertEquals("from", result.get(0));
        }

        @Test
        void returnsEmptyListWhenNoMatch() {
            List<String> keywords = Arrays.asList("SELECT", "FROM");

            List<String> result = ReactiveCodeGenHelper.subListFromKeyword(keywords, "HAVING");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void returnsEmptyListForNullKeywords() {
            List<String> result = ReactiveCodeGenHelper.subListFromKeyword(null, "test");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void returnsEmptyListForNullKeyword() {
            List<String> keywords = Arrays.asList("SELECT", "FROM");

            List<String> result = ReactiveCodeGenHelper.subListFromKeyword(keywords, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void returnsEntireListWhenFirstElementMatches() {
            List<String> keywords = Arrays.asList("SELECT", "FROM", "WHERE");

            List<String> result = ReactiveCodeGenHelper.subListFromKeyword(keywords, "select");

            assertEquals(3, result.size());
            assertEquals("SELECT", result.get(0));
        }

        @Test
        void returnsSingleElementWhenLastElementMatches() {
            List<String> keywords = Arrays.asList("SELECT", "FROM", "WHERE");

            List<String> result = ReactiveCodeGenHelper.subListFromKeyword(keywords, "where");

            assertEquals(1, result.size());
            assertEquals("WHERE", result.get(0));
        }

        @Test
        void matchesPartialKeyword() {
            // The method uses .contains() so partial matches work
            List<String> keywords = Arrays.asList("SELECT id", "FROM users", "WHERE active");

            List<String> result = ReactiveCodeGenHelper.subListFromKeyword(keywords, "users");

            assertEquals(2, result.size());
            assertEquals("FROM users", result.get(0));
        }

        @Test
        void returnsEmptyListForEmptyKeywordsList() {
            List<String> result = ReactiveCodeGenHelper.subListFromKeyword(Collections.emptyList(), "test");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void returnedListIsIndependentCopy() {
            List<String> keywords = new ArrayList<>(Arrays.asList("A", "B", "C"));

            List<String> result = ReactiveCodeGenHelper.subListFromKeyword(keywords, "B");
            keywords.set(1, "MODIFIED");

            assertEquals("B", result.get(0));
        }
    }

    // ------------------------------------------------------------------ //
    //  CrudOperationType enum
    // ------------------------------------------------------------------ //

    @Nested
    class CrudOperationTypeEnum {

        @Test
        void hasSixValues() {
            assertEquals(6, ReactiveCodeGenHelper.CrudOperationType.values().length);
        }

        @Test
        void containsExpectedValues() {
            assertNotNull(ReactiveCodeGenHelper.CrudOperationType.valueOf("INSERT"));
            assertNotNull(ReactiveCodeGenHelper.CrudOperationType.valueOf("UPDATE"));
            assertNotNull(ReactiveCodeGenHelper.CrudOperationType.valueOf("DELETE"));
            assertNotNull(ReactiveCodeGenHelper.CrudOperationType.valueOf("BATCH_INSERT"));
            assertNotNull(ReactiveCodeGenHelper.CrudOperationType.valueOf("BATCH_UPDATE"));
            assertNotNull(ReactiveCodeGenHelper.CrudOperationType.valueOf("BATCH_DELETE"));
        }
    }
}
