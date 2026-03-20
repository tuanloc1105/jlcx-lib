package vn.io.lcx.common.database.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLStrategyTest {

    private PostgreSQLStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PostgreSQLStrategy();
    }

    @Nested
    class GenerateIdColumnDefinition {

        @Test
        void generatesSerialPrimaryKey() {
            String result = strategy.generateIdColumnDefinition("users", "id", "INTEGER");
            assertEquals("id SERIAL PRIMARY KEY", result);
        }

        @Test
        void ignoresTableNameAndDataTypeInOutput() {
            String result = strategy.generateIdColumnDefinition("orders", "order_id", "BIGINT");
            assertEquals("order_id SERIAL PRIMARY KEY", result);
        }
    }

    @Nested
    class GenerateCreateIndex {

        @Test
        void generatesNonUniqueIndex() {
            String result = strategy.generateCreateIndex("idx_users_name", "users", "name", false);
            assertEquals("CREATE INDEX idx_users_name\nON users (name);\n", result);
        }

        @Test
        void generatesUniqueIndex() {
            String result = strategy.generateCreateIndex("idx_users_email", "users", "email", true);
            assertEquals("CREATE UNIQUE INDEX idx_users_email\nON users (email);\n", result);
        }

        @Test
        void generatesCompositeIndex() {
            String result = strategy.generateCreateIndex("idx_composite", "orders", "customer_id, order_date", false);
            assertEquals("CREATE INDEX idx_composite\nON orders (customer_id, order_date);\n", result);
        }
    }

    @Nested
    class GenerateDropIndex {

        @Test
        void generatesDropIndexIgnoringTableName() {
            String result = strategy.generateDropIndex("idx_users_name", "users");
            assertEquals("DROP INDEX idx_users_name;\n", result);
        }
    }

    @Nested
    class GenerateAddColumn {

        @Test
        void generatesAddColumnWithNotNullAndDefault() {
            ColumnDefinition colDef = new ColumnDefinition("email", "VARCHAR(255)", false, "'unknown'", false);
            String result = strategy.generateAddColumn(colDef, "users");
            assertEquals("ALTER TABLE users\n  ADD COLUMN email VARCHAR(255) DEFAULT 'unknown' NOT NULL;\n", result);
        }

        @Test
        void generatesAddColumnNullableNoDefault() {
            ColumnDefinition colDef = new ColumnDefinition("nickname", "VARCHAR(100)", true, null, false);
            String result = strategy.generateAddColumn(colDef, "users");
            assertEquals("ALTER TABLE users\n  ADD COLUMN nickname VARCHAR(100);\n", result);
        }

        @Test
        void generatesAddColumnWithUniqueConstraint() {
            ColumnDefinition colDef = new ColumnDefinition("email", "VARCHAR(255)", true, null, true);
            String result = strategy.generateAddColumn(colDef, "users");
            String expected = "ALTER TABLE users\n  ADD COLUMN email VARCHAR(255);\n" +
                    "ALTER TABLE users\n  ADD CONSTRAINT email_unique UNIQUE (email);\n";
            assertEquals(expected, result);
        }

        @Test
        void generatesAddColumnWithEmptyDefaultTreatedAsNoDefault() {
            ColumnDefinition colDef = new ColumnDefinition("status", "VARCHAR(50)", true, "", false);
            String result = strategy.generateAddColumn(colDef, "users");
            assertEquals("ALTER TABLE users\n  ADD COLUMN status VARCHAR(50);\n", result);
        }
    }

    @Nested
    class GenerateDropColumn {

        @Test
        void generatesDropColumnWithConstraintDrop() {
            String result = strategy.generateDropColumn("email", "users");
            String expected = "ALTER TABLE users\n  DROP COLUMN email;\n" +
                    "ALTER TABLE users\n  DROP CONSTRAINT IF EXISTS email_unique;\n";
            assertEquals(expected, result);
        }
    }

    @Nested
    class GenerateModifyColumn {

        @Test
        void generatesModifyColumnNotNullableNoDefault() {
            ColumnDefinition colDef = new ColumnDefinition("name", "VARCHAR(500)", false, null, false);
            String result = strategy.generateModifyColumn(colDef, "users");
            String expected = "ALTER TABLE users\n  ALTER COLUMN name TYPE VARCHAR(500)," +
                    "\n  ALTER COLUMN name SET NOT NULL," +
                    "\n  ALTER COLUMN name DROP DEFAULT;\n";
            assertEquals(expected, result);
        }

        @Test
        void generatesModifyColumnNullableWithDefault() {
            ColumnDefinition colDef = new ColumnDefinition("status", "VARCHAR(50)", true, "'active'", false);
            String result = strategy.generateModifyColumn(colDef, "users");
            String expected = "ALTER TABLE users\n  ALTER COLUMN status TYPE VARCHAR(50)," +
                    "\n  ALTER COLUMN status DROP NOT NULL," +
                    "\n  ALTER COLUMN status SET DEFAULT 'active';\n";
            assertEquals(expected, result);
        }

        @Test
        void generatesModifyColumnWithUniqueConstraint() {
            ColumnDefinition colDef = new ColumnDefinition("code", "VARCHAR(20)", false, null, true);
            String result = strategy.generateModifyColumn(colDef, "users");
            assertTrue(result.contains("ADD CONSTRAINT code_unique UNIQUE (code)"));
        }
    }

    @Nested
    class GenerateRenameColumn {

        @Test
        void generatesRenameColumnStatement() {
            String result = strategy.generateRenameColumn("old_name", "users");
            assertEquals("ALTER TABLE users RENAME COLUMN old_name TO old_name_new;\n", result);
        }
    }

    @Nested
    class GenerateSequenceStatement {

        @Test
        void returnsEmptyStringForPostgreSQL() {
            String result = strategy.generateSequenceStatement("users");
            assertEquals("", result);
        }
    }

    @Nested
    class GenerateForeignKeyCascade {

        @Test
        void generatesCascadeOptions() {
            String result = strategy.generateForeignKeyCascade(true);
            assertEquals("\nON DELETE SET NULL\nON UPDATE CASCADE;", result);
        }

        @Test
        void generatesNoCascadeOptions() {
            String result = strategy.generateForeignKeyCascade(false);
            assertEquals(";", result);
        }
    }
}
