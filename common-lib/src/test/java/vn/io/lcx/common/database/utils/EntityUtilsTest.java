package vn.io.lcx.common.database.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vn.io.lcx.common.annotation.ColumnName;
import vn.io.lcx.common.annotation.TableName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityUtilsTest {

    // Test entity with @TableName and @ColumnName annotations
    @TableName("user_account")
    static class TestUserEntity {
        private Long id;

        @ColumnName(name = "USER_NAME")
        private String userName;

        private String emailAddress;

        @ColumnName(name = "")
        private String emptyColumnName;
    }

    // Test entity without @TableName annotation
    static class NoTableNameEntity {
        private String name;
    }

    @Nested
    class GetColumnNameFromFieldName {

        @Test
        void returnsAnnotatedColumnName() {
            String result = EntityUtils.getColumnNameFromFieldName("userName", TestUserEntity.class);
            assertEquals("USER_NAME", result);
        }

        @Test
        void convertsCamelCaseWhenNoAnnotation() {
            String result = EntityUtils.getColumnNameFromFieldName("emailAddress", TestUserEntity.class);
            assertEquals("EMAIL_ADDRESS", result);
        }

        @Test
        void convertsCamelCaseForId() {
            String result = EntityUtils.getColumnNameFromFieldName("id", TestUserEntity.class);
            assertEquals("ID", result);
        }

        @Test
        void returnsNullForNonExistentField() {
            String result = EntityUtils.getColumnNameFromFieldName("nonExistent", TestUserEntity.class);
            assertNull(result);
        }

        @Test
        void throwsForClassWithoutTableNameAnnotation() {
            assertThrows(IllegalArgumentException.class,
                    () -> EntityUtils.getColumnNameFromFieldName("name", NoTableNameEntity.class));
        }

        @Test
        void convertsCamelCaseWhenColumnNameAnnotationIsEmptyString() {
            String result = EntityUtils.getColumnNameFromFieldName("emptyColumnName", TestUserEntity.class);
            assertEquals("EMPTY_COLUMN_NAME", result);
        }
    }

    @Nested
    class GetTableShortenedNameFromString {

        @Test
        void extractsFirstLettersFromUnderscoreSeparatedParts() {
            String result = EntityUtils.getTableShortenedName("user_account");
            assertEquals("ua", result);
        }

        @Test
        void handlesThreePartTableName() {
            String result = EntityUtils.getTableShortenedName("app_user_role");
            assertEquals("aur", result);
        }

        @Test
        void handlesSingleWordTableName() {
            String result = EntityUtils.getTableShortenedName("users");
            assertEquals("u", result);
        }

        @Test
        void handlesSchemaQualifiedTableName() {
            String result = EntityUtils.getTableShortenedName("public.user_account");
            assertEquals("ua", result);
        }

        @Test
        void throwsForBlankTableName() {
            assertThrows(IllegalArgumentException.class,
                    () -> EntityUtils.getTableShortenedName(""));
        }

        @Test
        void throwsForNullTableName() {
            assertThrows(IllegalArgumentException.class,
                    () -> EntityUtils.getTableShortenedName((String) null));
        }

        @Test
        void throwsForTableNameWithSpaces() {
            assertThrows(IllegalArgumentException.class,
                    () -> EntityUtils.getTableShortenedName("user account"));
        }

        @Test
        void convertsToLowerCase() {
            String result = EntityUtils.getTableShortenedName("USER_ACCOUNT");
            assertEquals("ua", result);
        }
    }

    @Nested
    class GetTableShortenedNameFromClass {

        @Test
        void extractsFromAnnotatedClass() {
            String result = EntityUtils.getTableShortenedName(TestUserEntity.class);
            assertEquals("ua", result);
        }

        @Test
        void throwsForClassWithoutTableNameAnnotation() {
            assertThrows(IllegalArgumentException.class,
                    () -> EntityUtils.getTableShortenedName(NoTableNameEntity.class));
        }
    }
}
