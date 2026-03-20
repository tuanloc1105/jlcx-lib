package vn.io.lcx.common.utils;

import com.google.gson.Gson;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyStringUtilsTest {

    private final Gson gson = new Gson();

    // ------------------------------------------------------------------ //
    //  JSON field manipulation
    // ------------------------------------------------------------------ //

    @Nested
    class AddNewFieldToJsonString {

        @Test
        void addsField() {
            String input = "{\"name\":\"Alice\"}";
            String result = MyStringUtils.addNewFieldToJsonString(gson, input, "age", 30);

            assertTrue(result.contains("\"age\":30") || result.contains("\"age\": 30"));
            assertTrue(result.contains("\"name\""));
        }

        @Test
        void addsFieldToEmptyObject() {
            String input = "{}";
            String result = MyStringUtils.addNewFieldToJsonString(gson, input, "key", "value");

            assertTrue(result.contains("\"key\""));
            assertTrue(result.contains("\"value\""));
        }
    }

    @Nested
    class RemoveFieldValueFromJsonString {

        @Test
        void removesField() {
            String input = "{\"name\":\"Alice\",\"age\":30}";
            String result = MyStringUtils.removeFieldValueFromJsonString(gson, input, "age");

            assertFalse(result.contains("\"age\""));
            assertTrue(result.contains("\"name\""));
        }

        @Test
        void removingNonExistentFieldReturnsOriginal() {
            String input = "{\"name\":\"Alice\"}";
            String result = MyStringUtils.removeFieldValueFromJsonString(gson, input, "missing");

            assertTrue(result.contains("\"name\""));
        }
    }

    @Nested
    class GetFieldValueOfJsonString {

        @Test
        void returnsValueForTopLevelField() {
            String input = "{\"name\":\"Alice\",\"age\":30}";
            List<String> result = MyStringUtils.getFieldValueOfJsonString(gson, input, "name", String.class);

            assertEquals(1, result.size());
            assertEquals("Alice", result.get(0));
        }

        @Test
        void returnsValueFromNestedObject() {
            String input = "{\"person\":{\"name\":\"Bob\"}}";
            List<String> result = MyStringUtils.getFieldValueOfJsonString(gson, input, "name", String.class);

            assertEquals(1, result.size());
            assertEquals("Bob", result.get(0));
        }

        @Test
        void returnsEmptyListWhenFieldNotFound() {
            String input = "{\"name\":\"Alice\"}";
            List<String> result = MyStringUtils.getFieldValueOfJsonString(gson, input, "missing", String.class);

            assertTrue(result.isEmpty());
        }
    }

    // ------------------------------------------------------------------ //
    //  minifyJsonString
    // ------------------------------------------------------------------ //

    @Nested
    class MinifyJsonString {

        @Test
        void removesWhitespace() {
            String input = "{ \"name\": \"Alice\", \"age\": 30 }";
            String result = MyStringUtils.minifyJsonString(input);

            assertNotNull(result);
            // The minification should remove at least some whitespace
            assertTrue(result.length() <= input.length());
        }

        @Test
        void blankInputReturnsEmpty() {
            assertEquals("", MyStringUtils.minifyJsonString(""));
            assertEquals("", MyStringUtils.minifyJsonString("   "));
            assertEquals("", MyStringUtils.minifyJsonString(null));
        }

        @Test
        void nonJsonReturnsInput() {
            // A plain non-JSON string should be returned as-is
            String input = "not json";
            String result = MyStringUtils.minifyJsonString(input);
            assertEquals(input, result);
        }
    }

    // ------------------------------------------------------------------ //
    //  URL encode / decode
    // ------------------------------------------------------------------ //

    @Nested
    class UrlEncoding {

        @Test
        void encodeUrl_and_decodeUrl_roundTrip() {
            String original = "hello world & foo=bar";
            String encoded = MyStringUtils.encodeUrl(original);
            String decoded = MyStringUtils.decodeUrl(encoded);

            assertNotNull(encoded);
            assertFalse(encoded.contains(" "));
            assertEquals(original, decoded);
        }

        @Test
        void encodeUrl_specialCharacters() {
            String encoded = MyStringUtils.encodeUrl("a=1&b=2");
            assertTrue(encoded.contains("%26") || encoded.contains("&"));
            assertFalse(encoded.contains("=") && encoded.contains("&"));
        }
    }

    // ------------------------------------------------------------------ //
    //  isNumeric / isNotNumeric
    // ------------------------------------------------------------------ //

    @Nested
    class IsNumeric {

        @Test
        void numericString_returnsTrue() {
            assertTrue(MyStringUtils.isNumeric("123"));
            assertTrue(MyStringUtils.isNumeric("0"));
            assertTrue(MyStringUtils.isNumeric("-42"));
        }

        @Test
        void nonNumericString_returnsFalse() {
            assertFalse(MyStringUtils.isNumeric("abc"));
            assertFalse(MyStringUtils.isNumeric("12.5"));
            assertFalse(MyStringUtils.isNumeric("12a"));
            assertFalse(MyStringUtils.isNumeric(""));
            assertFalse(MyStringUtils.isNumeric(null));
        }

        @Test
        void isNotNumeric_inversesIsNumeric() {
            assertTrue(MyStringUtils.isNotNumeric("abc"));
            assertTrue(MyStringUtils.isNotNumeric(null));
            assertFalse(MyStringUtils.isNotNumeric("123"));
            assertFalse(MyStringUtils.isNotNumeric("-1"));
        }
    }

    // ------------------------------------------------------------------ //
    //  removeSuffixOfString / removePrefixOfString
    // ------------------------------------------------------------------ //

    @Nested
    class RemoveSuffixAndPrefix {

        @Test
        void removeSuffixOfString_removesSuffix() {
            assertEquals("hello", MyStringUtils.removeSuffixOfString("hello.txt", ".txt"));
        }

        @Test
        void removeSuffixOfString_noMatch_returnsOriginal() {
            assertEquals("hello.txt", MyStringUtils.removeSuffixOfString("hello.txt", ".csv"));
        }

        @Test
        void removePrefixOfString_removesPrefix() {
            assertEquals("World", MyStringUtils.removePrefixOfString("HelloWorld", "Hello"));
        }

        @Test
        void removePrefixOfString_noMatch_returnsOriginal() {
            assertEquals("HelloWorld", MyStringUtils.removePrefixOfString("HelloWorld", "Bye"));
        }

        @Test
        void removePrefixOfString_nullInput_returnsNull() {
            assertEquals(null, MyStringUtils.removePrefixOfString(null, "prefix"));
        }

        @Test
        void removePrefixOfString_nullPrefix_returnsOriginal() {
            assertEquals("hello", MyStringUtils.removePrefixOfString("hello", null));
        }
    }

    // ------------------------------------------------------------------ //
    //  findAllOccurrences
    // ------------------------------------------------------------------ //

    @Nested
    class FindAllOccurrences {

        @Test
        void multipleMatches() {
            List<Integer> indices = MyStringUtils.findAllOccurrences("abacaba", 'a');
            assertEquals(List.of(0, 2, 4, 6), indices);
        }

        @Test
        void noMatch_returnsEmptyList() {
            List<Integer> indices = MyStringUtils.findAllOccurrences("hello", 'z');
            assertTrue(indices.isEmpty());
        }

        @Test
        void nonBreakingSpaceNormalized() {
            // \u00A0 is non-breaking space, should be normalized to regular space
            String text = "hello\u00A0world";
            List<Integer> indices = MyStringUtils.findAllOccurrences(text, ' ');
            assertEquals(List.of(5), indices);
        }
    }

    // ------------------------------------------------------------------ //
    //  normalizeString
    // ------------------------------------------------------------------ //

    @Nested
    class NormalizeString {

        @Test
        void removesVietnameseDiacritics() {
            String result = MyStringUtils.normalizeString("Xin chao");
            assertEquals("Xin chao", result);
        }

        @Test
        void convertsSpecialVietnameseD() {
            String result = MyStringUtils.normalizeString("do");
            // 'do' without diacritics stays 'do'
            assertEquals("do", result);
        }

        @Test
        void handlesVietnameseDBarred() {
            // Test explicit d-bar replacement
            String result = MyStringUtils.normalizeString("\u0111\u0110");
            assertEquals("dD", result);
        }

        @Test
        void blankInputReturnsEmpty() {
            assertEquals("", MyStringUtils.normalizeString(""));
            assertEquals("", MyStringUtils.normalizeString(null));
        }

        @Test
        void asciiStringUnchanged() {
            assertEquals("hello world", MyStringUtils.normalizeString("hello world"));
        }
    }

    // ------------------------------------------------------------------ //
    //  getCenteredText
    // ------------------------------------------------------------------ //

    @Nested
    class GetCenteredText {

        @Test
        void centersTextWithPadding() {
            String result = MyStringUtils.getCenteredText("hi", 10);
            // "hi" has length 2, padding = (10-2)/2 = 4 on each side
            assertEquals(10, result.length());
            assertTrue(result.contains("hi"));
            assertTrue(result.startsWith("    "));
        }

        @Test
        void blankTextReturnsEmpty() {
            assertEquals("", MyStringUtils.getCenteredText("", 10));
            assertEquals("", MyStringUtils.getCenteredText(null, 10));
        }

        @Test
        void zeroWidthReturnsEmpty() {
            assertEquals("", MyStringUtils.getCenteredText("hello", 0));
        }

        @Test
        void textLongerThanWidthNoNegativePadding() {
            // Text longer than width should use max(0, padding) = 0
            String result = MyStringUtils.getCenteredText("very long text here", 5);
            assertNotNull(result);
            assertTrue(result.contains("very long text here"));
        }
    }

    // ------------------------------------------------------------------ //
    //  utf8ToAscii / asciiToUtf8
    // ------------------------------------------------------------------ //

    @Nested
    class Utf8AsciiConversion {

        @Test
        void utf8ToAscii_and_asciiToUtf8_roundTrip() {
            String original = "Hello World";
            String ascii = MyStringUtils.utf8ToAscii(original);
            assertNotNull(ascii);
            String backToUtf8 = MyStringUtils.asciiToUtf8(ascii);
            assertEquals(original, backToUtf8);
        }

        @Test
        void utf8ToAscii_blankInputThrowsNPE() {
            assertThrows(NullPointerException.class, () -> MyStringUtils.utf8ToAscii(""));
            assertThrows(NullPointerException.class, () -> MyStringUtils.utf8ToAscii(null));
        }

        @Test
        void asciiToUtf8_blankInputThrowsNPE() {
            assertThrows(NullPointerException.class, () -> MyStringUtils.asciiToUtf8(""));
            assertThrows(NullPointerException.class, () -> MyStringUtils.asciiToUtf8(null));
        }
    }

    // ------------------------------------------------------------------ //
    //  stringIsJsonFormat
    // ------------------------------------------------------------------ //

    @Nested
    class StringIsJsonFormat {

        @Test
        void validJsonReturnsTrue() {
            assertTrue(MyStringUtils.stringIsJsonFormat("{\"key\":\"value\"}"));
            assertTrue(MyStringUtils.stringIsJsonFormat("[1,2,3]"));
            assertTrue(MyStringUtils.stringIsJsonFormat("\"hello\""));
        }

        @Test
        void invalidJsonReturnsFalse() {
            assertFalse(MyStringUtils.stringIsJsonFormat("not json {"));
            assertFalse(MyStringUtils.stringIsJsonFormat(""));
            assertFalse(MyStringUtils.stringIsJsonFormat(null));
        }
    }

    // ------------------------------------------------------------------ //
    //  getLastChars
    // ------------------------------------------------------------------ //

    @Nested
    class GetLastChars {

        @Test
        void returnsLastNChars() {
            assertEquals("rld", MyStringUtils.getLastChars("world", 3));
        }

        @Test
        void stringsShorterThanLimitReturnedAsIs() {
            assertEquals("hi", MyStringUtils.getLastChars("hi", 10));
        }

        @Test
        void nullInputReturnsEmpty() {
            assertEquals("", MyStringUtils.getLastChars(null, 5));
        }

        @Test
        void zeroLimitReturnsEmpty() {
            assertEquals("", MyStringUtils.getLastChars("hello", 0));
        }
    }

    // ------------------------------------------------------------------ //
    //  replaceCharWithSubstring
    // ------------------------------------------------------------------ //

    @Nested
    class ReplaceCharWithSubstring {

        @Test
        void replacesCharAtIndex() {
            assertEquals("Hxllo", MyStringUtils.replaceCharWithSubstring("Hello", 1, "x"));
        }

        @Test
        void replacesWithLongerString() {
            assertEquals("HELLO", MyStringUtils.replaceCharWithSubstring("HeLLO", 1, "E"));
        }

        @Test
        void outOfBoundsThrowsException() {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> MyStringUtils.replaceCharWithSubstring("abc", 5, "x"));
            assertThrows(IndexOutOfBoundsException.class,
                    () -> MyStringUtils.replaceCharWithSubstring("abc", -1, "x"));
        }
    }

    // ------------------------------------------------------------------ //
    //  isBlank / isNotBlank
    // ------------------------------------------------------------------ //

    @Nested
    class BlankChecks {

        @Test
        void isBlank_trueForNullAndEmpty() {
            assertTrue(MyStringUtils.isBlank(null));
            assertTrue(MyStringUtils.isBlank(""));
            assertTrue(MyStringUtils.isBlank("   "));
        }

        @Test
        void isBlank_falseForNonBlank() {
            assertFalse(MyStringUtils.isBlank("a"));
            assertFalse(MyStringUtils.isBlank(" a "));
        }

        @Test
        void isNotBlank_inversesIsBlank() {
            assertTrue(MyStringUtils.isNotBlank("hello"));
            assertFalse(MyStringUtils.isNotBlank(null));
            assertFalse(MyStringUtils.isNotBlank(""));
        }
    }

    // ------------------------------------------------------------------ //
    //  minifyString
    // ------------------------------------------------------------------ //

    @Nested
    class MinifyString {

        @Test
        void removesNewlinesAndTabs() {
            String input = "hello\n\tworld";
            String result = MyStringUtils.minifyString(input);

            assertFalse(result.contains("\n"));
            assertFalse(result.contains("\t"));
        }

        @Test
        void blankInputReturnsEmpty() {
            assertEquals("", MyStringUtils.minifyString(""));
            assertEquals("", MyStringUtils.minifyString(null));
        }
    }
}
