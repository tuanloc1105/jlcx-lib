package vn.io.lcx.common.database.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MSSQLStrategyTest {

    private MSSQLStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MSSQLStrategy();
    }

    @Nested
    class GenerateIdColumnDefinition {

        @Test
        void generatesIdentityPrimaryKey() {
            String result = strategy.generateIdColumnDefinition("users", "id", "INT");
            assertEquals("id INT IDENTITY(1,1) PRIMARY KEY", result);
        }

        @Test
        void ignoresTableNameAndDataTypeInOutput() {
            String result = strategy.generateIdColumnDefinition("orders", "order_id", "BIGINT");
            assertEquals("order_id INT IDENTITY(1,1) PRIMARY KEY", result);
        }
    }

    @Nested
    class GenerateRenameColumn {

        @Test
        void generatesSpRenameStatement() {
            String result = strategy.generateRenameColumn("old_col", "dbo.users");
            assertEquals("EXEC sp_rename 'dbo.users.old_col', 'old_col_new', 'COLUMN';\n", result);
        }

        @Test
        void generatesSpRenameForSimpleTableName() {
            String result = strategy.generateRenameColumn("status", "orders");
            assertEquals("EXEC sp_rename 'orders.status', 'status_new', 'COLUMN';\n", result);
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
    class GenerateDropIndex {

        @Test
        void generatesDropIndexWithTableName() {
            String result = strategy.generateDropIndex("idx_name", "users");
            assertEquals("DROP INDEX idx_name ON users;\n", result);
        }
    }

    @Nested
    class GenerateAddColumn {

        @Test
        void generatesAddColumnWithInlineUnique() {
            ColumnDefinition colDef = new ColumnDefinition("email", "NVARCHAR(255)", false, null, true);
            String result = strategy.generateAddColumn(colDef, "users");
            assertEquals("ALTER TABLE users\n  ADD email NVARCHAR(255) NOT NULL UNIQUE;\n", result);
        }

        @Test
        void generatesAddColumnNullableNoConstraints() {
            ColumnDefinition colDef = new ColumnDefinition("nickname", "NVARCHAR(100)", true, null, false);
            String result = strategy.generateAddColumn(colDef, "users");
            assertEquals("ALTER TABLE users\n  ADD nickname NVARCHAR(100);\n", result);
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
        void generatesAlterColumnWithNotNull() {
            ColumnDefinition colDef = new ColumnDefinition("name", "NVARCHAR(500)", false, null, false);
            String result = strategy.generateModifyColumn(colDef, "users");
            assertEquals("ALTER TABLE users ALTER COLUMN name NVARCHAR(500) NOT NULL;\n", result);
        }

        @Test
        void generatesAlterColumnWithNull() {
            ColumnDefinition colDef = new ColumnDefinition("status", "NVARCHAR(50)", true, null, false);
            String result = strategy.generateModifyColumn(colDef, "users");
            assertEquals("ALTER TABLE users ALTER COLUMN status NVARCHAR(50) NULL;\n", result);
        }
    }

    @Nested
    class GenerateSequenceStatement {

        @Test
        void returnsEmptyStringForMSSQL() {
            String result = strategy.generateSequenceStatement("users");
            assertEquals("", result);
        }
    }

    @Nested
    class GenerateForeignKeyCascade {

        @Test
        void generatesCascadeDeleteAndUpdate() {
            String result = strategy.generateForeignKeyCascade(true);
            assertEquals("\nON DELETE CASCADE\nON UPDATE CASCADE;", result);
        }

        @Test
        void generatesNoCascade() {
            String result = strategy.generateForeignKeyCascade(false);
            assertEquals(";", result);
        }
    }
}
