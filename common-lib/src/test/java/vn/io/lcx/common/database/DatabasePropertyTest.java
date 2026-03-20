package vn.io.lcx.common.database;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabasePropertyTest {

    private DatabaseProperty createFullyPopulated() {
        return new DatabaseProperty(
                "jdbc:postgresql://localhost:5432/testdb",
                "admin",
                "secret",
                "org.postgresql.Driver",
                5,
                20,
                30,
                true,
                false
        );
    }

    @Nested
    class PropertiesIsAllSet {

        @Test
        void returnsTrueWhenAllPropertiesAreSet() {
            DatabaseProperty property = createFullyPopulated();
            assertTrue(property.propertiesIsAllSet());
        }

        @Test
        void returnsFalseWhenConnectionStringIsNull() {
            DatabaseProperty property = createFullyPopulated();
            property.setConnectionString(null);
            assertFalse(property.propertiesIsAllSet());
        }

        @Test
        void returnsFalseWhenConnectionStringIsBlank() {
            DatabaseProperty property = createFullyPopulated();
            property.setConnectionString("   ");
            assertFalse(property.propertiesIsAllSet());
        }

        @Test
        void returnsFalseWhenConnectionStringIsEmpty() {
            DatabaseProperty property = createFullyPopulated();
            property.setConnectionString("");
            assertFalse(property.propertiesIsAllSet());
        }

        @Test
        void returnsFalseWhenUsernameIsNull() {
            DatabaseProperty property = createFullyPopulated();
            property.setUsername(null);
            assertFalse(property.propertiesIsAllSet());
        }

        @Test
        void returnsFalseWhenUsernameIsBlank() {
            DatabaseProperty property = createFullyPopulated();
            property.setUsername("");
            assertFalse(property.propertiesIsAllSet());
        }

        @Test
        void returnsFalseWhenPasswordIsNull() {
            DatabaseProperty property = createFullyPopulated();
            property.setPassword(null);
            assertFalse(property.propertiesIsAllSet());
        }

        @Test
        void returnsFalseWhenPasswordIsBlank() {
            DatabaseProperty property = createFullyPopulated();
            property.setPassword("");
            assertFalse(property.propertiesIsAllSet());
        }

        @Test
        void returnsFalseWhenInitialPoolSizeIsZero() {
            DatabaseProperty property = createFullyPopulated();
            property.setInitialPoolSize(0);
            assertFalse(property.propertiesIsAllSet());
        }

        @Test
        void returnsFalseWhenMaxPoolSizeIsZero() {
            DatabaseProperty property = createFullyPopulated();
            property.setMaxPoolSize(0);
            assertFalse(property.propertiesIsAllSet());
        }

        @Test
        void returnsFalseWhenMaxTimeoutIsZero() {
            DatabaseProperty property = createFullyPopulated();
            property.setMaxTimeout(0);
            assertFalse(property.propertiesIsAllSet());
        }

        @Test
        void returnsFalseForDefaultConstructor() {
            DatabaseProperty property = new DatabaseProperty();
            assertFalse(property.propertiesIsAllSet());
        }

        @Test
        void driverClassNameDoesNotAffectValidation() {
            DatabaseProperty property = createFullyPopulated();
            property.setDriverClassName(null);
            // driverClassName is NOT checked by propertiesIsAllSet
            assertTrue(property.propertiesIsAllSet());
        }
    }

    @Nested
    class GettersAndSetters {

        @Test
        void connectionStringGetterSetter() {
            DatabaseProperty property = new DatabaseProperty();
            property.setConnectionString("jdbc:test://host/db");
            assertEquals("jdbc:test://host/db", property.getConnectionString());
        }

        @Test
        void usernameGetterSetter() {
            DatabaseProperty property = new DatabaseProperty();
            property.setUsername("user1");
            assertEquals("user1", property.getUsername());
        }

        @Test
        void passwordGetterSetter() {
            DatabaseProperty property = new DatabaseProperty();
            property.setPassword("pass1");
            assertEquals("pass1", property.getPassword());
        }

        @Test
        void driverClassNameGetterSetter() {
            DatabaseProperty property = new DatabaseProperty();
            property.setDriverClassName("com.mysql.cj.jdbc.Driver");
            assertEquals("com.mysql.cj.jdbc.Driver", property.getDriverClassName());
        }

        @Test
        void initialPoolSizeGetterSetter() {
            DatabaseProperty property = new DatabaseProperty();
            property.setInitialPoolSize(5);
            assertEquals(5, property.getInitialPoolSize());
        }

        @Test
        void maxPoolSizeGetterSetter() {
            DatabaseProperty property = new DatabaseProperty();
            property.setMaxPoolSize(20);
            assertEquals(20, property.getMaxPoolSize());
        }

        @Test
        void maxTimeoutGetterSetter() {
            DatabaseProperty property = new DatabaseProperty();
            property.setMaxTimeout(60);
            assertEquals(60, property.getMaxTimeout());
        }

        @Test
        void showSqlGetterSetter() {
            DatabaseProperty property = new DatabaseProperty();
            property.setShowSql(true);
            assertTrue(property.isShowSql());
        }

        @Test
        void showSqlParameterGetterSetter() {
            DatabaseProperty property = new DatabaseProperty();
            property.setShowSqlParameter(true);
            assertTrue(property.isShowSqlParameter());
        }

        @Test
        void allArgsConstructorSetsAllFields() {
            DatabaseProperty property = createFullyPopulated();
            assertEquals("jdbc:postgresql://localhost:5432/testdb", property.getConnectionString());
            assertEquals("admin", property.getUsername());
            assertEquals("secret", property.getPassword());
            assertEquals("org.postgresql.Driver", property.getDriverClassName());
            assertEquals(5, property.getInitialPoolSize());
            assertEquals(20, property.getMaxPoolSize());
            assertEquals(30, property.getMaxTimeout());
            assertTrue(property.isShowSql());
            assertFalse(property.isShowSqlParameter());
        }
    }
}
