package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordCaseUtilsTest {

    // ------------------------------------------------------------------ //
    //  toCamelCase
    // ------------------------------------------------------------------ //

    @Nested
    class ToCamelCase {

        @Test
        void fromNormalText() {
            // toCamelCase expects space-separated words
            assertEquals("helloWorld", WordCaseUtils.toCamelCase("hello world"));
        }

        @Test
        void fromSnakeCase_viaNormalization() {
            // fromSnakeCase converts to space-separated, then toCamelCase
            String normalized = WordCaseUtils.fromSnakeCase("hello_world");
            assertEquals("helloWorld", WordCaseUtils.toCamelCase(normalized));
        }

        @Test
        void singleWord() {
            assertEquals("hello", WordCaseUtils.toCamelCase("hello"));
        }

        @Test
        void threeWords() {
            assertEquals("myVariableName", WordCaseUtils.toCamelCase("my variable name"));
        }
    }

    // ------------------------------------------------------------------ //
    //  toPascalCase
    // ------------------------------------------------------------------ //

    @Nested
    class ToPascalCase {

        @Test
        void fromNormalText() {
            assertEquals("HelloWorld", WordCaseUtils.toPascalCase("hello world"));
        }

        @Test
        void fromCamelCase_viaNormalization() {
            String normalized = WordCaseUtils.fromCamelCase("helloWorld");
            assertEquals("HelloWorld", WordCaseUtils.toPascalCase(normalized));
        }

        @Test
        void singleWord() {
            assertEquals("Hello", WordCaseUtils.toPascalCase("hello"));
        }
    }

    // ------------------------------------------------------------------ //
    //  toSnakeCase
    // ------------------------------------------------------------------ //

    @Nested
    class ToSnakeCase {

        @Test
        void fromNormalText() {
            assertEquals("hello_world", WordCaseUtils.toSnakeCase("hello world"));
        }

        @Test
        void fromCamelCase_viaNormalization() {
            String normalized = WordCaseUtils.fromCamelCase("helloWorld");
            assertEquals("hello_world", WordCaseUtils.toSnakeCase(normalized));
        }

        @Test
        void singleWord() {
            assertEquals("hello", WordCaseUtils.toSnakeCase("hello"));
        }
    }

    // ------------------------------------------------------------------ //
    //  toKebabCase
    // ------------------------------------------------------------------ //

    @Nested
    class ToKebabCase {

        @Test
        void fromNormalText() {
            assertEquals("hello-world", WordCaseUtils.toKebabCase("hello world"));
        }

        @Test
        void fromCamelCase_viaNormalization() {
            String normalized = WordCaseUtils.fromCamelCase("helloWorld");
            assertEquals("hello-world", WordCaseUtils.toKebabCase(normalized));
        }

        @Test
        void singleWord() {
            assertEquals("hello", WordCaseUtils.toKebabCase("hello"));
        }
    }

    // ------------------------------------------------------------------ //
    //  toConstantCase
    // ------------------------------------------------------------------ //

    @Nested
    class ToConstantCase {

        @Test
        void fromNormalText() {
            assertEquals("HELLO_WORLD", WordCaseUtils.toConstantCase("hello world"));
        }

        @Test
        void fromCamelCase_viaNormalization() {
            String normalized = WordCaseUtils.fromCamelCase("helloWorld");
            assertEquals("HELLO_WORLD", WordCaseUtils.toConstantCase(normalized));
        }

        @Test
        void singleWord() {
            assertEquals("HELLO", WordCaseUtils.toConstantCase("hello"));
        }
    }

    // ------------------------------------------------------------------ //
    //  toPathCase
    // ------------------------------------------------------------------ //

    @Nested
    class ToPathCase {

        @Test
        void fromNormalText() {
            String separator = System.getProperty("file.separator");
            assertEquals("hello" + separator + "world", WordCaseUtils.toPathCase("hello world"));
        }

        @Test
        void singleWord() {
            assertEquals("hello", WordCaseUtils.toPathCase("hello"));
        }
    }

    // ------------------------------------------------------------------ //
    //  toTitleCase
    // ------------------------------------------------------------------ //

    @Nested
    class ToTitleCase {

        @Test
        void fromNormalText() {
            assertEquals("Hello World", WordCaseUtils.toTitleCase("hello world"));
        }

        @Test
        void singleWord() {
            assertEquals("Hello", WordCaseUtils.toTitleCase("hello"));
        }

        @Test
        void alreadyTitleCase() {
            assertEquals("Hello World", WordCaseUtils.toTitleCase("Hello World"));
        }
    }

    // ------------------------------------------------------------------ //
    //  toDotCase
    // ------------------------------------------------------------------ //

    @Nested
    class ToDotCase {

        @Test
        void fromNormalText() {
            assertEquals("hello.world", WordCaseUtils.toDotCase("hello world"));
        }

        @Test
        void singleWord() {
            assertEquals("hello", WordCaseUtils.toDotCase("hello"));
        }

        @Test
        void threeWords() {
            assertEquals("one.two.three", WordCaseUtils.toDotCase("one two three"));
        }
    }

    // ------------------------------------------------------------------ //
    //  fromCamelCase
    // ------------------------------------------------------------------ //

    @Nested
    class FromCamelCase {

        @Test
        void convertsToSpaceSeparated() {
            assertEquals("hello World", WordCaseUtils.fromCamelCase("helloWorld"));
        }

        @Test
        void multipleUpperCase() {
            assertEquals("my Variable Name", WordCaseUtils.fromCamelCase("myVariableName"));
        }

        @Test
        void singleWord() {
            assertEquals("hello", WordCaseUtils.fromCamelCase("hello"));
        }

        @Test
        void startsWithUpperCase() {
            assertEquals("Hello World", WordCaseUtils.fromCamelCase("HelloWorld"));
        }
    }

    // ------------------------------------------------------------------ //
    //  fromSnakeCase
    // ------------------------------------------------------------------ //

    @Nested
    class FromSnakeCase {

        @Test
        void convertsToSpaceSeparated() {
            assertEquals("hello world", WordCaseUtils.fromSnakeCase("hello_world"));
        }

        @Test
        void singleWord() {
            assertEquals("hello", WordCaseUtils.fromSnakeCase("hello"));
        }

        @Test
        void multipleUnderscores() {
            assertEquals("one two three", WordCaseUtils.fromSnakeCase("one_two_three"));
        }
    }

    // ------------------------------------------------------------------ //
    //  fromKebabCase
    // ------------------------------------------------------------------ //

    @Nested
    class FromKebabCase {

        @Test
        void convertsToSpaceSeparated() {
            assertEquals("hello world", WordCaseUtils.fromKebabCase("hello-world"));
        }

        @Test
        void singleWord() {
            assertEquals("hello", WordCaseUtils.fromKebabCase("hello"));
        }

        @Test
        void multipleDashes() {
            assertEquals("one two three", WordCaseUtils.fromKebabCase("one-two-three"));
        }
    }

    // ------------------------------------------------------------------ //
    //  escapeString / unescapeString round-trip
    // ------------------------------------------------------------------ //

    @Nested
    class EscapeUnescape {

        @Test
        void escapeString_escapesSpecialChars() {
            String input = "line1\nline2\ttab";
            String escaped = WordCaseUtils.escapeString(input);

            assertTrue(escaped.contains("\\n"));
            assertTrue(escaped.contains("\\t"));
            assertFalse(escaped.contains("\n"));
            assertFalse(escaped.contains("\t"));
        }

        @Test
        void unescapeString_unescapesSpecialChars() {
            String input = "line1\\nline2\\ttab";
            String unescaped = WordCaseUtils.unescapeString(input);

            assertTrue(unescaped.contains("\n"));
            assertTrue(unescaped.contains("\t"));
        }

        @Test
        void escapeString_and_unescapeString_roundTrip() {
            // Note: round-trip is lossy for quad-spaces due to escape removing them,
            // so test with content that doesn't include quad-spaces
            String input = "hello\nworld\ttab\"quote\"";
            String escaped = WordCaseUtils.escapeString(input);
            String unescaped = WordCaseUtils.unescapeString(escaped);

            assertEquals(input, unescaped);
        }

        @Test
        void escapeString_escapesDoubleQuotes() {
            String escaped = WordCaseUtils.escapeString("say \"hello\"");
            assertTrue(escaped.contains("\\\""));
        }
    }

    // ------------------------------------------------------------------ //
    //  minify
    // ------------------------------------------------------------------ //

    @Nested
    class Minify {

        @Test
        void removesExtraWhitespace() {
            String input = "hello\n\tworld";
            String result = WordCaseUtils.minify(input, false);

            assertFalse(result.contains("\n"));
            assertFalse(result.contains("\t"));
        }

        @Test
        void withSqlFlag_splitsStatements() {
            String input = "SELECT * FROM a; SELECT * FROM b";
            String result = WordCaseUtils.minify(input, true);

            assertTrue(result.contains(";\n"));
        }

        @Test
        void minifiesJsonFormatting() {
            String input = "{ \"key\": \"value\" }";
            String result = WordCaseUtils.minify(input, false);

            assertTrue(result.contains("{\"key\":\"value\"}"));
        }
    }

    // ------------------------------------------------------------------ //
    //  Empty/single-word across all conversions
    // ------------------------------------------------------------------ //

    @Nested
    class SingleWordAllConversions {

        @Test
        void singleWord_allConversions() {
            assertEquals("hello", WordCaseUtils.toCamelCase("hello"));
            assertEquals("Hello", WordCaseUtils.toPascalCase("hello"));
            assertEquals("hello", WordCaseUtils.toSnakeCase("hello"));
            assertEquals("hello", WordCaseUtils.toKebabCase("hello"));
            assertEquals("HELLO", WordCaseUtils.toConstantCase("hello"));
            assertEquals("hello", WordCaseUtils.toPathCase("hello"));
            assertEquals("Hello", WordCaseUtils.toTitleCase("hello"));
            assertEquals("hello", WordCaseUtils.toDotCase("hello"));
        }
    }

    // ------------------------------------------------------------------ //
    //  Other utility methods
    // ------------------------------------------------------------------ //

    @Nested
    class UtilityMethods {

        @Test
        void capitalize_uppercasesFirstChar() {
            assertEquals("Hello", WordCaseUtils.capitalize("hello"));
            assertEquals("Already", WordCaseUtils.capitalize("Already"));
        }

        @Test
        void swapCase_swapsAllChars() {
            assertEquals("hELLO wORLD", WordCaseUtils.swapCase("Hello World"));
        }

        @Test
        void removeNonAlpha_removesSpecialChars() {
            assertEquals("hello world 123", WordCaseUtils.removeNonAlpha("hello! world@ #123"));
        }

        @Test
        void convertCamelToConstant() {
            assertEquals("HELLO_WORLD", WordCaseUtils.convertCamelToConstant("helloWorld"));
        }

        @Test
        void pascalToNormal() {
            // Should convert PascalCase to "lower case" words and remove "exception"
            String result = WordCaseUtils.pascalToNormal("NullPointerException");
            assertTrue(result.contains("null"));
            assertTrue(result.contains("pointer"));
            assertFalse(result.contains("exception"));
        }

        @Test
        void calculateBytesBits() {
            int[] result = WordCaseUtils.calculateBytesBits("hello");
            assertEquals(5, result[0]); // 5 bytes
            assertEquals(40, result[1]); // 40 bits
        }

        @Test
        void calculateBytesBits_utf8() {
            // Unicode chars take more bytes in UTF-8
            int[] result = WordCaseUtils.calculateBytesBits("\u00E9"); // e with accent
            assertTrue(result[0] > 1);
            assertEquals(result[0] * 8, result[1]);
        }

        @Test
        void fromPascalCase_sameAsFromCamelCase() {
            assertEquals(
                    WordCaseUtils.fromCamelCase("HelloWorld"),
                    WordCaseUtils.fromPascalCase("HelloWorld")
            );
        }
    }
}
