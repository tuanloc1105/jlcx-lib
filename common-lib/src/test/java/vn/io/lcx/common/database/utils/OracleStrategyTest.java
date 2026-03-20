package vn.io.lcx.common.database.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleStrategyTest {

    private OracleStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new OracleStrategy();
    }

    @Nested
    class GenerateIdColumnDefinition {

        @Test
        void generatesOracleSequenceBasedId() {
            String result = strategy.generateIdColumnDefinition("USERS", "ID", "NUMBER");
            assertEquals("ID NUMBER(18) DEFAULT USERS_SEQ.nextval NOT NULL PRIMARY KEY", result);
        }

        @Test
        void usesTableNameForSequenceReference() {
            String result = strategy.generateIdColumnDefinition("ORDERS", "ORDER_ID", "NUMBER");
            assertEquals("ORDER_ID NUMBER(18) DEFAULT ORDERS_SEQ.nextval NOT NULL PRIMARY KEY", result);
        }
    }

    @Nested
    class GenerateSequenceStatement {

        @Test
        void generatesCreateSequenceWithComments() {
            String result = strategy.generateSequenceStatement("USERS");
            assertTrue(result.contains("CREATE SEQUENCE USERS_SEQ START WITH 1 INCREMENT BY 1 CACHE 20;"));
            assertTrue(result.contains("SELECT USERS_SEQ.NEXTVAL FROM dual;"));
            assertTrue(result.contains("SELECT USERS_SEQ.CURRVAL FROM dual;"));
            assertTrue(result.contains("DROP SEQUENCE USERS_SEQ;"));
        }

        @Test
        void sequenceNameIsTableNamePlusSEQ() {
            String result = strategy.generateSequenceStatement("MY_TABLE");
            assertTrue(result.startsWith("CREATE SEQUENCE MY_TABLE_SEQ"));
        }
    }

    @Nested
    class GenerateModifyColumn {

        @Test
        void generatesModifyWithNotNullAndNoDefault() {
            ColumnDefinition colDef = new ColumnDefinition("NAME", "VARCHAR2(500)", false, null, false);
            String result = strategy.generateModifyColumn(colDef, "USERS");
            assertTrue(result.contains("ALTER TABLE USERS\n  MODIFY (NAME VARCHAR2(500));"));
            assertTrue(result.contains("ALTER TABLE USERS\n  MODIFY (NAME NOT NULL);"));
        }

        @Test
        void generatesModifyWithNullableAndDefault() {
            ColumnDefinition colDef = new ColumnDefinition("STATUS", "VARCHAR2(50)", true, "'ACTIVE'", false);
            String result = strategy.generateModifyColumn(colDef, "USERS");
            assertTrue(result.contains("ALTER TABLE USERS\n  MODIFY (STATUS VARCHAR2(50) DEFAULT 'ACTIVE');"));
            assertTrue(result.contains("ALTER TABLE USERS\n  MODIFY (STATUS NULL);"));
        }

        @Test
        void generatesModifyWithUniqueConstraint() {
            ColumnDefinition colDef = new ColumnDefinition("CODE", "VARCHAR2(20)", false, null, true);
            String result = strategy.generateModifyColumn(colDef, "USERS");
            assertTrue(result.contains("ADD CONSTRAINT CODE_unique UNIQUE (CODE)"));
        }
    }

    @Nested
    class GenerateCreateIndex {

        @Test
        void generatesNonUniqueIndex() {
            String result = strategy.generateCreateIndex("IDX_NAME", "USERS", "NAME", false);
            assertEquals("CREATE INDEX IDX_NAME\nON USERS (NAME);\n", result);
        }

        @Test
        void generatesUniqueIndex() {
            String result = strategy.generateCreateIndex("IDX_EMAIL", "USERS", "EMAIL", true);
            assertEquals("CREATE UNIQUE INDEX IDX_EMAIL\nON USERS (EMAIL);\n", result);
        }
    }

    @Nested
    class GenerateDropIndex {

        @Test
        void generatesDropIndexIgnoringTableName() {
            String result = strategy.generateDropIndex("IDX_NAME", "USERS");
            assertEquals("DROP INDEX IDX_NAME;\n", result);
        }
    }

    @Nested
    class GenerateAddColumn {

        @Test
        void generatesOracleAddColumnSyntaxWithParens() {
            ColumnDefinition colDef = new ColumnDefinition("AGE", "NUMBER(3)", true, null, false);
            String result = strategy.generateAddColumn(colDef, "USERS");
            assertEquals("ALTER TABLE USERS\n  ADD (AGE NUMBER(3));\n", result);
        }

        @Test
        void generatesAddColumnWithDefaultAndNotNull() {
            ColumnDefinition colDef = new ColumnDefinition("STATUS", "VARCHAR2(50)", false, "'ACTIVE'", false);
            String result = strategy.generateAddColumn(colDef, "USERS");
            assertEquals("ALTER TABLE USERS\n  ADD (STATUS VARCHAR2(50) DEFAULT 'ACTIVE' NOT NULL);\n", result);
        }

        @Test
        void generatesAddColumnWithUniqueConstraint() {
            ColumnDefinition colDef = new ColumnDefinition("EMAIL", "VARCHAR2(255)", true, null, true);
            String result = strategy.generateAddColumn(colDef, "USERS");
            assertTrue(result.contains("ADD CONSTRAINT EMAIL_unique UNIQUE (EMAIL)"));
        }
    }

    @Nested
    class GenerateDropColumn {

        @Test
        void generatesDropColumnAndConstraint() {
            String result = strategy.generateDropColumn("EMAIL", "USERS");
            String expected = "ALTER TABLE USERS\n  DROP COLUMN EMAIL;\n" +
                    "ALTER TABLE USERS\n  DROP CONSTRAINT EMAIL_unique;\n";
            assertEquals(expected, result);
        }
    }

    @Nested
    class GenerateRenameColumn {

        @Test
        void generatesRenameColumnStatement() {
            String result = strategy.generateRenameColumn("OLD_COL", "USERS");
            assertEquals("ALTER TABLE USERS RENAME COLUMN OLD_COL TO OLD_COL_new;\n", result);
        }
    }

    @Nested
    class GenerateForeignKeyCascade {

        @Test
        void generatesCascadeDelete() {
            String result = strategy.generateForeignKeyCascade(true);
            assertEquals("\nON DELETE CASCADE;", result);
        }

        @Test
        void generatesNoCascade() {
            String result = strategy.generateForeignKeyCascade(false);
            assertEquals(";", result);
        }
    }
}
