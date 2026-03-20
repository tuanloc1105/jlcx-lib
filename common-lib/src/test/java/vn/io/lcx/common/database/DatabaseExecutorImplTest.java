package vn.io.lcx.common.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseExecutorImplTest {

    private Connection connection;
    private DatabaseExecutorImpl executor;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        connection.setAutoCommit(true);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS test_users");
            stmt.execute("CREATE TABLE test_users (id INT PRIMARY KEY, name VARCHAR(100), email VARCHAR(200))");
            stmt.execute("INSERT INTO test_users VALUES (1, 'Alice', 'alice@test.com')");
            stmt.execute("INSERT INTO test_users VALUES (2, 'Bob', 'bob@test.com')");
        }
        executor = DatabaseExecutorImpl.getInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS test_users");
            }
            connection.close();
        }
    }

    // --- Singleton ---

    @Test
    void getInstance_returnsSameInstance() {
        DatabaseExecutorImpl instance1 = DatabaseExecutorImpl.getInstance();
        DatabaseExecutorImpl instance2 = DatabaseExecutorImpl.getInstance();
        assertEquals(instance1, instance2);
    }

    // --- executeQuery ---

    @Nested
    class ExecuteQueryTests {

        @Test
        void executeQuery_selectAll_returnsList() {
            String sql = "SELECT id, name, email FROM test_users ORDER BY id";

            List<String> result = executor.executeQuery(connection, sql, null,
                    (ResultSetHandler<String>) resultSet -> {
                        try {
                            return resultSet.getString("name");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Alice", result.get(0));
            assertEquals("Bob", result.get(1));
        }

        @Test
        void executeQuery_withParams_filtersCorrectly() {
            String sql = "SELECT id, name, email FROM test_users WHERE id = ?";
            Map<Integer, Object> params = new HashMap<>();
            params.put(1, 1);

            List<String> result = executor.executeQuery(connection, sql, params,
                    (ResultSetHandler<String>) resultSet -> {
                        try {
                            return resultSet.getString("name");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Alice", result.get(0));
        }

        @Test
        void executeQuery_withStringParam_filtersCorrectly() {
            String sql = "SELECT id, name, email FROM test_users WHERE name = ?";
            Map<Integer, Object> params = new HashMap<>();
            params.put(1, "Bob");

            List<String> result = executor.executeQuery(connection, sql, params,
                    (ResultSetHandler<String>) resultSet -> {
                        try {
                            return resultSet.getString("email");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("bob@test.com", result.get(0));
        }

        @Test
        void executeQuery_noResults_returnsEmptyList() {
            String sql = "SELECT id, name, email FROM test_users WHERE id = ?";
            Map<Integer, Object> params = new HashMap<>();
            params.put(1, 999);

            List<String> result = executor.executeQuery(connection, sql, params,
                    (ResultSetHandler<String>) resultSet -> {
                        try {
                            return resultSet.getString("name");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void executeQuery_invalidSql_returnsNull() {
            String sql = "SELECT * FROM non_existent_table";

            List<String> result = executor.executeQuery(connection, sql, null,
                    (ResultSetHandler<String>) resultSet -> {
                        try {
                            return resultSet.getString("name");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            assertNull(result);
        }

        @Test
        void executeQuery_withEmptyParams_treatedAsNoParams() {
            String sql = "SELECT id, name, email FROM test_users ORDER BY id";
            Map<Integer, Object> emptyParams = new HashMap<>();

            List<String> result = executor.executeQuery(connection, sql, emptyParams,
                    (ResultSetHandler<String>) resultSet -> {
                        try {
                            return resultSet.getString("name");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            assertNotNull(result);
            assertEquals(2, result.size());
        }
    }

    // --- executeMutation ---

    @Nested
    class ExecuteMutationTests {

        @Test
        void executeMutation_insert_returns1() {
            String sql = "INSERT INTO test_users (id, name, email) VALUES (?, ?, ?)";
            Map<Integer, Object> params = new HashMap<>();
            params.put(1, 3);
            params.put(2, "Charlie");
            params.put(3, "charlie@test.com");

            int rowsAffected = executor.executeMutation(connection, sql, params);

            assertEquals(1, rowsAffected);
        }

        @Test
        void executeMutation_update_returnsAffectedRows() {
            String sql = "UPDATE test_users SET name = ? WHERE id = ?";
            Map<Integer, Object> params = new HashMap<>();
            params.put(1, "Alice Updated");
            params.put(2, 1);

            int rowsAffected = executor.executeMutation(connection, sql, params);

            assertEquals(1, rowsAffected);

            // Verify the update took effect
            List<String> result = executor.executeQuery(connection,
                    "SELECT name FROM test_users WHERE id = ?",
                    Map.of(1, 1),
                    (ResultSetHandler<String>) rs -> {
                        try {
                            return rs.getString("name");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Alice Updated", result.get(0));
        }

        @Test
        void executeMutation_updateAll_returnsMultipleAffectedRows() {
            String sql = "UPDATE test_users SET email = ?";
            Map<Integer, Object> params = new HashMap<>();
            params.put(1, "updated@test.com");

            int rowsAffected = executor.executeMutation(connection, sql, params);

            assertEquals(2, rowsAffected);
        }

        @Test
        void executeMutation_delete_returnsAffectedRows() {
            String sql = "DELETE FROM test_users WHERE id = ?";
            Map<Integer, Object> params = new HashMap<>();
            params.put(1, 1);

            int rowsAffected = executor.executeMutation(connection, sql, params);

            assertEquals(1, rowsAffected);
        }

        @Test
        void executeMutation_deleteAll_returnsAllRows() {
            String sql = "DELETE FROM test_users";

            int rowsAffected = executor.executeMutation(connection, sql, null);

            assertEquals(2, rowsAffected);
        }

        @Test
        void executeMutation_noRowsMatched_returnsZero() {
            String sql = "DELETE FROM test_users WHERE id = ?";
            Map<Integer, Object> params = new HashMap<>();
            params.put(1, 999);

            int rowsAffected = executor.executeMutation(connection, sql, params);

            assertEquals(0, rowsAffected);
        }

        @Test
        void executeMutation_invalidSql_returnsZero() {
            String sql = "DELETE FROM non_existent_table";

            int rowsAffected = executor.executeMutation(connection, sql, null);

            assertEquals(0, rowsAffected);
        }
    }

    // --- executeMutationBatch ---

    @Nested
    class ExecuteMutationBatchTests {

        @Test
        void executeMutationBatch_multipleInserts_returnsSuccessCounts() {
            String sql = "INSERT INTO test_users (id, name, email) VALUES (?, ?, ?)";
            List<Map<Integer, Object>> paramList = List.of(
                    Map.of(1, 3, 2, "Charlie", 3, "charlie@test.com"),
                    Map.of(1, 4, 2, "Diana", 3, "diana@test.com")
            );

            Map<String, Integer> result = executor.executeMutationBatch(connection, sql, paramList);

            assertNotNull(result);
            assertEquals(2, result.get(DatabaseExecutor.SUCCESS_KEY_NAME));
            assertEquals(0, result.get(DatabaseExecutor.SUCCESS_BUT_NO_INFO_KEY_NAME));
            assertEquals(0, result.get(DatabaseExecutor.FAILED_KEY_NAME));
        }

        @Test
        void executeMutationBatch_invalidSql_returnsNull() {
            String sql = "INSERT INTO non_existent_table (id) VALUES (?)";
            List<Map<Integer, Object>> paramList = List.of(
                    Map.of(1, 1)
            );

            Map<String, Integer> result = executor.executeMutationBatch(connection, sql, paramList);

            assertNull(result);
        }
    }

    // --- Constants ---

    @Test
    void constants_haveExpectedValues() {
        assertEquals("success", DatabaseExecutor.SUCCESS_KEY_NAME);
        assertEquals("success.but.no.info", DatabaseExecutor.SUCCESS_BUT_NO_INFO_KEY_NAME);
        assertEquals("failed", DatabaseExecutor.FAILED_KEY_NAME);
    }
}
