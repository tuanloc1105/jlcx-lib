package vn.io.lcx.common.database.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLStrategyTest {

    private MySQLStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MySQLStrategy();
    }

    @Nested
    class GenerateIdColumnDefinition {

        @Test
        void generatesAutoIncrementPrimaryKey() {
            String result = strategy.generateIdColumnDefinition("users", "id", "INT");
            assertEquals("id INT AUTO_INCREMENT PRIMARY KEY", result);
        }

        @Test
        void ignoresTableNameAndDataTypeInOutput() {
            String result = strategy.generateIdColumnDefinition("orders", "order_id", "BIGINT");
            assertEquals("order_id INT AUTO_INCREMENT PRIMARY KEY", result);
        }
    }

    @Nested
    class GenerateDropIndex {

        @Test
        void generatesDropIndexWithTableName() {
            String result = strategy.generateDropIndex("idx_users_name", "users");
            assertEquals("DROP INDEX idx_users_name ON users;\n", result);
        }
    }

    @Nested
    class GenerateCreateIndex {

        @Test
        void generatesNonUniqueIndex() {
            String result = strategy.generateCreateIndex("idx_name", "users", "name", false);
            assertEquals("CREATE INDEX idx_name\nON users (name);\n", result);
        }

        @Test
        void generatesUniqueIndex() {
            String result = strategy.generateCreateIndex("idx_email", "users", "email", true);
            assertEquals("CREATE UNIQUE INDEX idx_email\nON users (email);\n", result);
        }
    }

    @Nested
    class GenerateAddColumn {

        @Test
        void generatesAddColumnWithInlineUnique() {
            ColumnDefinition colDef = new ColumnDefinition("email", "VARCHAR(255)", false, null, true);
            String result = strategy.generateAddColumn(colDef, "users");
            assertEquals("ALTER TABLE users\n  ADD email VARCHAR(255) NOT NULL UNIQUE;\n", result);
        }

        @Test
        void generatesAddColumnNullableNoConstraints() {
            ColumnDefinition colDef = new ColumnDefinition("nickname", "VARCHAR(100)", true, null, false);
            String result = strategy.generateAddColumn(colDef, "users");
            assertEquals("ALTER TABLE users\n  ADD nickname VARCHAR(100);\n", result);
        }

        @Test
        void generatesAddColumnWithDefault() {
            ColumnDefinition colDef = new ColumnDefinition("status", "VARCHAR(50)", false, "'active'", false);
            String result = strategy.generateAddColumn(colDef, "users");
            assertEquals("ALTER TABLE users\n  ADD status VARCHAR(50) DEFAULT 'active' NOT NULL;\n", result);
        }
    }

    @Nested
    class GenerateDropColumn {

        @Test
        void generatesSimpleDropColumn() {
            String result = strategy.generateDropColumn("email", "users");
            assertEquals("ALTER TABLE users\n  DROP COLUMN email;\n", result);
        }
    }

    @Nested
    class GenerateModifyColumn {

        @Test
        void generatesModifyWithNotNull() {
            ColumnDefinition colDef = new ColumnDefinition("name", "VARCHAR(500)", false, null, false);
            String result = strategy.generateModifyColumn(colDef, "users");
            assertEquals("ALTER TABLE users\n  MODIFY name VARCHAR(500) NOT NULL;\n", result);
        }

        @Test
        void generatesModifyWithNullAndDefault() {
            ColumnDefinition colDef = new ColumnDefinition("status", "VARCHAR(50)", true, "'active'", false);
            String result = strategy.generateModifyColumn(colDef, "users");
            assertEquals("ALTER TABLE users\n  MODIFY status VARCHAR(50) DEFAULT 'active' NULL;\n", result);
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
        void returnsEmptyStringForMySQL() {
            String result = strategy.generateSequenceStatement("users");
            assertEquals("", result);
        }
    }

    @Nested
    class GenerateForeignKeyCascade {

        @Test
        void generatesCascadeDeleteAndUpdateRestrict() {
            String result = strategy.generateForeignKeyCascade(true);
            assertEquals("\nON DELETE CASCADE\nON UPDATE RESTRICT;", result);
        }

        @Test
        void generatesNoCascade() {
            String result = strategy.generateForeignKeyCascade(false);
            assertEquals(";", result);
        }
    }
}
