package vn.com.lcx.common.utils;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class MyStringUtils {

    private MyStringUtils() {
    }

    /**
     * Adds a new field with the specified name and value to a JSON string.
     *
     * @param gson            the Gson instance to use for parsing and serialization
     * @param inputJsonString the input JSON string
     * @param fieldName       the name of the field to add
     * @param fieldValue      the value of the field to add
     * @return the resulting JSON string with the new field added
     */
    public static String addNewFieldToJsonString(final Gson gson, final String inputJsonString, final String fieldName, final Object fieldValue) {
        final LinkedHashMap<String, Object> jsonMap = gson.fromJson(inputJsonString, CommonConstant.HASH_MAP_GSON_TYPE_TOKEN.getType());
        jsonMap.put(fieldName, fieldValue);
        return gson.toJson(jsonMap);
    }

    /**
     * Removes a field with the specified name from a JSON string.
     *
     * @param gson            the Gson instance to use for parsing and serialization
     * @param inputJsonString the input JSON string
     * @param fieldName       the name of the field to remove
     * @return the resulting JSON string with the field removed
     */
    public static String removeFieldValueFromJsonString(final Gson gson, final String inputJsonString, final String fieldName) {
        final LinkedHashMap<String, Object> jsonMap = gson.fromJson(inputJsonString, CommonConstant.HASH_MAP_GSON_TYPE_TOKEN.getType());
        jsonMap.remove(fieldName);
        return gson.toJson(jsonMap);
    }

    /**
     * Recursively retrieves all values of a specified field name from a JSON string.
     *
     * @param gson            the Gson instance to use for parsing and serialization
     * @param inputJsonString the input JSON string
     * @param fieldName       the name of the field to search for
     * @param fieldDataType   the expected type of the field value
     * @param <T>             the type of the field value
     * @return a list of all values found for the specified field name
     */
    public static <T> List<T> getFieldValueOfJsonString(final Gson gson, final String inputJsonString, final String fieldName, final Class<T> fieldDataType) {
        final var result = new ArrayList<T>();
        final LinkedHashMap<String, Object> jsonMap = gson.fromJson(inputJsonString, CommonConstant.HASH_MAP_GSON_TYPE_TOKEN.getType());
        for (Map.Entry<String, Object> header : jsonMap.entrySet()) {
            Object value = header.getValue();
            if (fieldName.equals(header.getKey())) {
                result.add(fieldDataType.cast(value));
            }
            if (value instanceof Map) {
                result.addAll(getFieldValueOfJsonString(gson, gson.toJson(value), fieldName, fieldDataType));
            }
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (!list.isEmpty()) {
                    if (list.get(0) instanceof Map) {
                        for (Object o : list) {
                            result.addAll(getFieldValueOfJsonString(gson, gson.toJson(o), fieldName, fieldDataType));
                        }
                    }
                }
            }
            if (!result.isEmpty()) {
                break;
            }
        }

        return result;
    }

    /**
     * Minifies a JSON string by removing unnecessary whitespace and formatting.
     * If the input is not valid JSON, an exception is thrown.
     *
     * @param input the JSON string to minify
     * @return the minified JSON string
     * @throws IllegalArgumentException if the input is not valid JSON
     */
    public static String minifyJsonString(String input) {
        if (StringUtils.isBlank(input)) {
            return CommonConstant.EMPTY_STRING;
        }
        if (!stringIsJsonFormat(input)) {
            return input;
        }
        if (!stringIsJsonFormat(input.trim())) {
            throw new IllegalArgumentException(input.trim() + " is not a valid JSON string");
        }
        if (input.length() > 100000) {
            return input.substring(0, 50) + "..." + input.substring(input.length() - 50);
        }
        return input
                .replace("\n", " ")
                .replace("\r", CommonConstant.EMPTY_STRING)
                .replace("\t", CommonConstant.EMPTY_STRING)
                .replace("    ", CommonConstant.EMPTY_STRING)
                .replace("\": \"", "\":\"")
                .replace("\", \"", "\",\"")
                .replace("{ ", "{")
                .replace("} ", "}")
                .replace(": {", ":{")
                .replace(" [", "[")
                .replace("[ ", "[")
                .replace(" ]", "]")
                .replace("] ", "]")
                .replace(", ", ",")
                .replace("\" }", "\"}");
    }

    /**
     * Minifies a string by trimming, removing tabs, and condensing whitespace and formatting.
     *
     * @param inputString the string to minify
     * @return the minified string
     */
    public static String minifyString(String inputString) {
        if (StringUtils.isBlank(inputString)) {
            return CommonConstant.EMPTY_STRING;
        }
        // Step 1: Trim the string and replace spaces/new lines
        String minified = inputString.trim() // Removes leading and trailing whitespaces
                .replace("\n", " ") // Replaces new lines with space
                .replace("\t", "") // Removes tabs
                .replace("    ", "") // Removes quadruple spaces
                .replace("\": \"", "\":\"") // Minifies JSON
                .replace("\", \"", "\",\"")
                .replace("{ ", "{")
                .replace("} ", "}")
                .replace(": {", ":{")
                .replace("\" }", "\"}");

        // Step 2: Format SQL statements
        // minified = minified
        //         .replace(";  ", ";\n") // Handles multiple spaces before semicolons
        //         .replace("; ", ";\n") // Replaces spaces after semicolons
        //         .replace(" SELECT ", ";\nSELECT ") // Inserts line breaks before SELECT
        //         .replace(" UPDATE ", ";\nUPDATE ") // Inserts line breaks before UPDATE
        //         .replace(" DELETE ", ";\nDELETE ") // Inserts line breaks before DELETE
        //         .replace(" INSERT INTO ", ";\nINSERT INTO ") // Inserts line breaks before INSERT INTO
        //         .replace("  SELECT ", ";\nSELECT ") // Handles multiple spaces before SELECT
        //         .replace("  UPDATE ", ";\nUPDATE ") // Handles multiple spaces before UPDATE
        //         .replace("  DELETE ", ";\nDELETE ") // Handles multiple spaces before DELETE
        //         .replace("  INSERT INTO ", ";\nINSERT INTO "); // Handles multiple spaces before INSERT INTO

        return minified;
    }

    /**
     * Encodes a string for safe use in URLs using UTF-8 encoding.
     *
     * @param value the string to encode
     * @return the URL-encoded string, or an empty string if encoding fails
     */
    public static String encodeUrl(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return CommonConstant.EMPTY_STRING;
        }
    }

    /**
     * Decodes a URL-encoded string using UTF-8 encoding.
     *
     * @param value the string to decode
     * @return the decoded string, or an empty string if decoding fails
     */
    public static String decodeUrl(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            return CommonConstant.EMPTY_STRING;
        }
    }

    /**
     * Returns the last N characters of a string, or the whole string if shorter than the limit.
     *
     * @param input            the input string
     * @param lengthLimitation the number of characters to return from the end
     * @return the last N characters, or the input if shorter
     */
    public static String getLastChars(String input, int lengthLimitation) {
        if (input == null || lengthLimitation == 0) {
            return CommonConstant.EMPTY_STRING;
        }
        if (input.length() > lengthLimitation) {
            return input.substring(input.length() - lengthLimitation);
        } else {
            return input;
        }
    }

    /**
     * Checks if a string is in valid JSON format.
     *
     * @param input the string to check
     * @return true if the string is valid JSON, false otherwise
     */
    public static boolean stringIsJsonFormat(final String input) {
        try {
            if (StringUtils.isBlank(input)) {
                return false;
            }
            JsonParser.parseString(input);
            return true;
        } catch (Exception e) {
            // LogUtils.writeLog(LogUtils.Level.WARN, e.getMessage());
            return false;
        }
    }

    private static String repeatString(String str, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> str)
                .collect(Collectors.joining());
    }

    /**
     * Returns the input text centered within the specified console width, padded with spaces.
     *
     * @param text         the text to center
     * @param consoleWidth the width to center within
     * @return the centered text
     */
    public static String getCenteredText(String text, int consoleWidth) {
        if (StringUtils.isBlank(text) || consoleWidth == 0) {
            return CommonConstant.EMPTY_STRING;
        }
        // Calculate the padding needed to center the text
        int padding = (consoleWidth - text.length()) / 2;

        // Create the padding spaces and return the centered text
        return repeatString(" ", Math.max(0, padding)) + text + repeatString(" ", Math.max(0, padding));
    }

    /**
     * Returns the input text left-aligned within the specified console width, padded with spaces.
     *
     * @param text         the text to align left
     * @param consoleWidth the width to align within
     * @return the left-aligned text
     */
    public static String alignLeftText(String text, int consoleWidth) {
        if (StringUtils.isBlank(text) || consoleWidth == 0) {
            return CommonConstant.EMPTY_STRING;
        }
        // Calculate the padding needed to center the text
        int padding = (consoleWidth - text.length()) / 2;

        // Create the padding spaces and return the centered text
        return text + repeatString(" ", Math.max(0, padding)) + repeatString(" ", Math.max(0, padding));
    }

    /**
     * Returns the input text right-aligned within the specified console width, padded with spaces.
     *
     * @param text         the text to align right
     * @param consoleWidth the width to align within
     * @return the right-aligned text
     */
    public static String alignRightText(String text, int consoleWidth) {
        if (StringUtils.isBlank(text) || consoleWidth == 0) {
            return CommonConstant.EMPTY_STRING;
        }
        // Calculate the padding needed to center the text
        int padding = (consoleWidth - text.length()) / 2;

        // Create the padding spaces and return the centered text
        return repeatString(" ", Math.max(0, padding)) + repeatString(" ", Math.max(0, padding)) + text;
    }

    /**
     * Puts one or more strings into a box with borders, optionally centering or aligning the text.
     *
     * @param logWithConsoleWidthIsTheLongestLine if true, box width is set to the longest line
     * @param mode                                the paragraph alignment mode (center, left, right)
     * @param linesOfString                       the lines of text to put in the box
     * @return the boxed string
     */
    public static String putStringIntoABox(final boolean logWithConsoleWidthIsTheLongestLine,
                                           ParagraphMode mode,
                                           String... linesOfString) {
        if (linesOfString == null || linesOfString.length == 0) {
            return CommonConstant.EMPTY_STRING;
        }
        int consoleWidth;
        if (logWithConsoleWidthIsTheLongestLine) {
            consoleWidth = 0;
            for (String line : linesOfString) {
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                if (line.length() > consoleWidth) {
                    consoleWidth = line.length();
                }
            }
        } else {
            consoleWidth = 100;
        }

        List<String> listOfStringsAfterCentered = new ArrayList<>();

        int longestLength = 0;
        for (String line : linesOfString) {
            String lineAfterCentered;

            switch (mode) {
                case CENTER:
                    lineAfterCentered = "│" + getCenteredText(line, consoleWidth);
                    break;
                case ALIGN_LEFT:
                    lineAfterCentered = "│" + alignLeftText(line, consoleWidth);
                    break;
                case ALIGN_RIGHT:
                    lineAfterCentered = "│" + alignRightText(line, consoleWidth);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + mode);
            }

            var currentLineLength = lineAfterCentered.length();
            listOfStringsAfterCentered.add(lineAfterCentered);
            if (currentLineLength > longestLength) {
                longestLength = currentLineLength;
            }
        }
        for (int index = 0; index < listOfStringsAfterCentered.size(); index++) {
            var currentIndexString = listOfStringsAfterCentered.get(index);
            var currentIndexLength = currentIndexString.length();
            if (currentIndexLength < longestLength) {
                var numberOfSpaceNeedToBeAdd = longestLength - currentIndexLength;
                for (int i = 0; i < numberOfSpaceNeedToBeAdd; i++) {
                    currentIndexString += " ";
                }
            }
            currentIndexString += "│";
            listOfStringsAfterCentered.set(index, currentIndexString);
        }
        int lengthOfHeaderAndFooterOfBox = listOfStringsAfterCentered.get(0).length();
        StringBuilder boxHeader = new StringBuilder();
        StringBuilder boxFooter = new StringBuilder();
        for (int i = 0; i < lengthOfHeaderAndFooterOfBox; i++) {
            if (i == 0) {
                boxHeader.append("┌");
                boxFooter.append("└");
            } else if (i == lengthOfHeaderAndFooterOfBox - 1) {
                boxHeader.append("┐");
                boxFooter.append("┘");
            } else {
                boxHeader.append("─");
                boxFooter.append("─");
            }
        }
        return boxHeader + "\n" + String.join("\n", listOfStringsAfterCentered) + "\n" + boxFooter;

    }

    /**
     * Converts a UTF-8 string to ASCII encoding.
     *
     * @param utf8String the UTF-8 string
     * @return the ASCII-encoded string
     * @throws NullPointerException if the input is blank
     */
    public static String utf8ToAscii(String utf8String) {
        if (StringUtils.isBlank(utf8String)) {
            throw new NullPointerException();
        }
        try {
            byte[] utf8Bytes = utf8String.getBytes(StandardCharsets.UTF_8);
            return new String(utf8Bytes, StandardCharsets.ISO_8859_1);
        } catch (Exception e) {
            LogUtils.writeLog("Convert error", e, LogUtils.Level.WARN);
            return null;
        }
    }

    /**
     * Converts an ASCII string to UTF-8 encoding.
     *
     * @param asciiString the ASCII string
     * @return the UTF-8 encoded string
     * @throws NullPointerException if the input is blank
     */
    public static String asciiToUtf8(String asciiString) {
        if (StringUtils.isBlank(asciiString)) {
            throw new NullPointerException();
        }
        try {
            byte[] asciiBytes = asciiString.getBytes(StandardCharsets.ISO_8859_1);
            return new String(asciiBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LogUtils.writeLog("Convert error", e, LogUtils.Level.WARN);
            return null;
        }
    }

    /**
     * Checks if a string is numeric (optionally negative).
     *
     * @param str the string to check
     * @return true if the string is numeric, false otherwise
     */
    public static boolean isNumeric(String str) {
        return StringUtils.isNotBlank(str) && str.matches("-?\\d+");
    }

    /**
     * Checks if a string is not numeric.
     *
     * @param str the string to check
     * @return true if the string is not numeric, false otherwise
     */
    public static boolean isNotNumeric(String str) {
        return !isNumeric(str);
    }

    /**
     * Deserializes an XML string to an object of the specified class.
     *
     * @param xml the XML string
     * @param clz the class to deserialize to
     * @param <T> the type of the object
     * @return the deserialized object, or null if conversion fails
     */
    public static <T> T fromXML(String xml, Class<T> clz) {
        try {
            final var jaxbContext = JAXBContext.newInstance(clz);
            final var unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(xml);
            return clz.cast(unmarshaller.unmarshal(reader));
        } catch (Exception e) {
            LogUtils.writeLog(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Serializes an object to an XML string.
     *
     * @param input the object to serialize
     * @param <T>   the type of the object
     * @return the XML string, or empty string if conversion fails
     */
    public static <T> String toXML(T input) {
        try {
            final var jaxbContext = JAXBContext.newInstance(input.getClass());
            final var marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            // Write the XML to a string
            StringWriter sw = new StringWriter();
            marshaller.marshal(input, sw);
            return sw.toString();
        } catch (Exception e) {
            LogUtils.writeLog(e.getMessage(), e);
            return CommonConstant.EMPTY_STRING;
        }
    }

    /**
     * Formats a list of strings so that each word is padded to align with the longest word in its column.
     *
     * @param input the list of strings to format
     * @return the formatted string with aligned columns
     */
    public static String formatStringSpace(List<String> input) {
        var lengthOfEachPart = new ArrayList<Integer>();
        for (String currentLine : input) {
            var wordsInLine = Arrays.asList(currentLine.split(" "));
            for (int i = 0; i < wordsInLine.size(); i++) {
                final int lengthOfCurrentWord = wordsInLine.get(i).length();
                try {
                    var lengthOfCurrentPart = lengthOfEachPart.get(i);
                    if (lengthOfCurrentWord > lengthOfCurrentPart) {
                        lengthOfEachPart.set(i, lengthOfCurrentWord);
                    }
                } catch (Exception e) {
                    lengthOfEachPart.add(lengthOfCurrentWord);
                }
            }
        }
        final var listOfResult = new ArrayList<String>();
        for (String currentLine : input) {
            String result = "";
            var wordsInLine = Arrays.asList(currentLine.split(" "));
            for (int i = 0; i < wordsInLine.size(); i++) {
                result += "%-" + lengthOfEachPart.get(i) + "s    ";
            }
            listOfResult.add(String.format(result, wordsInLine.toArray()));

        }
        return String.join(System.lineSeparator(), listOfResult);
    }

    /**
     * Formats a list of lists of strings so that each column is aligned, with an optional delimiter.
     *
     * @param input     the list of lists of strings to format
     * @param delimiter optional delimiter to use between lines
     * @return the formatted string with aligned columns
     */
    public static String formatStringSpace2(List<List<String>> input, String... delimiter) {
        final var formatedList = formatStringWithEqualSpaceLength(input);
        if (delimiter.length == 1 && StringUtils.isNotBlank(delimiter[0])) {
            return String.join(delimiter[0], formatedList);
        } else {
            return String.join(System.lineSeparator(), formatedList);
        }
    }

    /**
     * Formats a list of lists of strings so that each column is aligned to the maximum width in that column.
     *
     * @param input the list of lists of strings to format
     * @return a list of formatted strings with aligned columns
     */
    public static List<String> formatStringWithEqualSpaceLength(List<List<String>> input) {
        List<Integer> lengthOfEachPart = new ArrayList<>();
        for (List<String> wordsInLine : input) {
            for (int i = 0; i < wordsInLine.size(); i++) {
                var lengthOfCurrentWord = wordsInLine.get(i).length();
                try {
                    var lengthOfCurrentPart = lengthOfEachPart.get(i);
                    if (lengthOfCurrentWord > lengthOfCurrentPart) {
                        lengthOfEachPart.set(i, lengthOfCurrentWord);
                    }
                } catch (Exception e) {
                    lengthOfEachPart.add(lengthOfCurrentWord);
                }
            }
        }
        final ArrayList<String> listOfResult = new ArrayList<>();
        for (List<String> wordsInLine : input) {
            var result = "";
            for (int i = 0; i < wordsInLine.size(); i++) {
                result += "%-" + lengthOfEachPart.get(i) + "s ";
            }
            listOfResult.add(
                    removeSuffixOfString(
                            String.format(
                                    result,
                                    wordsInLine.toArray()
                            ),
                            " "
                    ).trim()
            );
        }
        return listOfResult;
    }

    /**
     * Remove the suffix from the input string if it ends with the specified suffix.
     *
     * @param input               The original string
     * @param suffixWillBeRemoved The suffix to remove
     * @return String without the suffix if present, otherwise the original string
     */
    public static String removeSuffixOfString(String input, String suffixWillBeRemoved) {
        int indexOfSuffix = input.lastIndexOf(suffixWillBeRemoved);
        if (indexOfSuffix > 0) {
            return input.substring(0, indexOfSuffix);
        }
        return input;
    }

    /**
     * Remove prefix from the input string if it starts with the prefix.
     *
     * @param input  The original string
     * @param prefix The prefix to remove
     * @return String without prefix if present, otherwise the original string
     */
    public static String removePrefixOfString(String input, String prefix) {
        if (input == null || prefix == null) {
            return input;
        }
        if (input.startsWith(prefix)) {
            return input.substring(prefix.length());
        }
        return input;
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Normalizes a string by removing diacritical marks and converting special Vietnamese characters to ASCII.
     *
     * @param s the string to normalize
     * @return the normalized string
     */
    public static String normalizeString(String s) {
        if (StringUtils.isBlank(s)) {
            return CommonConstant.EMPTY_STRING;
        }
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        return temp.replace("đ", "d").replace("Đ", "D");
    }

    /**
     * Mask the values of specified fields in a JSON string, regardless of their nesting level.
     * <p>
     * This method will traverse the JSON structure recursively and replace the value of any field whose name matches
     * one of the provided fieldNames with a masked value (e.g., asterisks for strings, "***" for numbers/booleans).
     * </p>
     *
     * @param gson            Gson instance for parsing and serializing JSON
     * @param inputJsonString The input JSON string
     * @param fieldNames      The names of the fields to mask (case-sensitive)
     * @return The JSON string with specified fields' values masked
     */
    public static String maskJsonFields(final Gson gson, final String inputJsonString, final String... fieldNames) {
        if (StringUtils.isBlank(inputJsonString) ||
                fieldNames == null ||
                fieldNames.length == 0 ||
                !MyStringUtils.stringIsJsonFormat(inputJsonString)) {
            return inputJsonString;
        }

        try {
            // Parse JSON to LinkedHashMap to preserve order
            JsonReader jsonReader = new JsonReader(new StringReader(inputJsonString));
            jsonReader.setStrictness(Strictness.LENIENT);
            final LinkedHashMap<String, Object> jsonMap = gson.fromJson(jsonReader, CommonConstant.HASH_MAP_GSON_TYPE_TOKEN.getType());

            // Mask fields recursively
            maskJsonFieldsRecursively(jsonMap, Arrays.asList(fieldNames));

            return gson.toJson(jsonMap);
        } catch (Exception e) {
            LogUtils.writeLog("Error masking JSON fields: " + e.getMessage(), e, LogUtils.Level.WARN);
            return inputJsonString;
        }
    }

    /**
     * Masks the values of default sensitive fields in a JSON string.
     * <p>
     * This method is an overload of {@link #maskJsonFields(Gson, String, String...)} that uses a predefined
     * list of sensitive field names from {@link CommonConstant#SENSITIVE_FIELD_NAMES}.
     * It will traverse the JSON structure recursively and replace the value of any matching field
     * with a masked value.
     * </p>
     *
     * @param gson            Gson instance for parsing and serializing JSON.
     * @param inputJsonString The input JSON string.
     * @return The JSON string with default sensitive fields' values masked.
     * @see #maskJsonFields(Gson, String, String...)
     */
    public static String maskJsonFields(final Gson gson, final String inputJsonString) {
        return maskJsonFields(gson, inputJsonString, CommonConstant.SENSITIVE_FIELD_NAMES.toArray(String[]::new));
    }

    /**
     * Recursively traverse a JSON object (Map/List) and mask the values of specified fields.
     *
     * @param jsonObject The JSON object (Map or List) to process
     * @param fieldNames The list of field names to mask
     */
    private static void maskJsonFieldsRecursively(Object jsonObject, List<String> fieldNames) {
        if (jsonObject instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) jsonObject;

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // Check if current field should be masked
                if (fieldNames.contains(key)) {
                    if (value instanceof String) {
                        String strValue = (String) value;
                        if (StringUtils.isNotBlank(strValue)) {
                            // Mask string value with asterisks
                            map.put(key, repeatString("*", Math.min(strValue.length(), 8)));
                        }
                    } else if (value instanceof Number) {
                        // Mask numeric value
                        map.put(key, "***");
                    } else if (value instanceof Boolean) {
                        // Mask boolean value
                        map.put(key, "***");
                    } else {
                        // Mask other types
                        map.put(key, "***");
                    }
                } else {
                    // Recursively process nested objects
                    if (value instanceof Map || value instanceof List) {
                        maskJsonFieldsRecursively(value, fieldNames);
                    }
                    if (value instanceof String) {
                        String strValue = (String) value;
                        if (StringUtils.isNotBlank(strValue) && strValue.length() > 10000) {
                            map.put(key,
                                    strValue.substring(0, 50) +
                                            "..." + strValue.substring(strValue.length() - 50));
                        }
                    }
                }
            }
        } else if (jsonObject instanceof List) {
            List<?> list = (List<?>) jsonObject;
            for (Object item : list) {
                if (item instanceof Map || item instanceof List) {
                    maskJsonFieldsRecursively(item, fieldNames);
                }
            }
        }
    }

    public enum ParagraphMode {
        CENTER,
        ALIGN_LEFT,
        ALIGN_RIGHT,
    }

}
