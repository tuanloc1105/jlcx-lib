package vn.com.lcx.common.utils;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class MyStringUtils {

    private MyStringUtils() {
    }

    public static String addNewFieldToJsonString(final Gson gson, final String inputJsonString, final String fieldName, final Object fieldValue) {
        final LinkedHashMap<String, Object> jsonMap = gson.fromJson(inputJsonString, CommonConstant.HASH_MAP_GSON_TYPE_TOKEN.getType());
        jsonMap.put(fieldName, fieldValue);
        return gson.toJson(jsonMap);
    }

    public static String removeFieldValueFromJsonString(final Gson gson, final String inputJsonString, final String fieldName) {
        final LinkedHashMap<String, Object> jsonMap = gson.fromJson(inputJsonString, CommonConstant.HASH_MAP_GSON_TYPE_TOKEN.getType());
        jsonMap.remove(fieldName);
        return gson.toJson(jsonMap);
    }

    public static <T> List<T> getFieldValueOfJsonString(final Gson gson, final String inputJsonString, final String fieldName, final Class<T> fieldDataType) {
        val result = new ArrayList<T>();
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

    public static String minifyJsonString(String input) {
        if (StringUtils.isBlank(input)) {
            return CommonConstant.EMPTY_STRING;
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

    public static String encodeUrl(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return CommonConstant.EMPTY_STRING;
        }
    }

    public static String decodeUrl(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            return CommonConstant.EMPTY_STRING;
        }
    }

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

    public static boolean stringIsJsonFormat(final String input) {
        try {
            if (StringUtils.isBlank(input)) {
                return false;
            }
            JsonParser.parseString(input);
            return true;
        } catch (Exception e) {
            LogUtils.writeLog(LogUtils.Level.WARN, e.getMessage());
            return false;
        }
    }

    private static String repeatString(String str, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> str)
                .collect(Collectors.joining());
    }

    public static String getCenteredText(String text, int consoleWidth) {
        if (StringUtils.isBlank(text) || consoleWidth == 0) {
            return CommonConstant.EMPTY_STRING;
        }
        // Calculate the padding needed to center the text
        int padding = (consoleWidth - text.length()) / 2;

        // Create the padding spaces and return the centered text
        return repeatString(" ", Math.max(0, padding)) + text + repeatString(" ", Math.max(0, padding));
    }

    public static String alignLeftText(String text, int consoleWidth) {
        if (StringUtils.isBlank(text) || consoleWidth == 0) {
            return CommonConstant.EMPTY_STRING;
        }
        // Calculate the padding needed to center the text
        int padding = (consoleWidth - text.length()) / 2;

        // Create the padding spaces and return the centered text
        return text + repeatString(" ", Math.max(0, padding)) + repeatString(" ", Math.max(0, padding));
    }

    public static String alignRightText(String text, int consoleWidth) {
        if (StringUtils.isBlank(text) || consoleWidth == 0) {
            return CommonConstant.EMPTY_STRING;
        }
        // Calculate the padding needed to center the text
        int padding = (consoleWidth - text.length()) / 2;

        // Create the padding spaces and return the centered text
        return repeatString(" ", Math.max(0, padding)) + repeatString(" ", Math.max(0, padding)) + text;
    }

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

    public static boolean isNumeric(String str) {
        return StringUtils.isNotBlank(str) && str.matches("-?\\d+");
    }

    public static boolean isNotNumeric(String str) {
        return !isNumeric(str);
    }

    public static <T> T fromXML(String xml, Class<T> clz) {
        try {
            val jaxbContext = JAXBContext.newInstance(clz);
            val unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(xml);
            return clz.cast(unmarshaller.unmarshal(reader));
        } catch (Exception e) {
            LogUtils.writeLog(e.getMessage(), e);
            return null;
        }
    }

    public static <T> String toXML(T input) {
        try {
            val jaxbContext = JAXBContext.newInstance(input.getClass());
            val marshaller = jaxbContext.createMarshaller();
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
        val listOfResult = new ArrayList<String>();
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

    public static String formatStringSpace2(List<List<String>> input, String... delimiter) {
        val formatedList = formatStringWithEqualSpaceLength(input);
        if (delimiter.length == 1 && StringUtils.isNotBlank(delimiter[0])) {
            return String.join(delimiter[0], formatedList);
        } else {
            return String.join(System.lineSeparator(), formatedList);
        }
    }

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
                result += "%-" + lengthOfEachPart.get(i) + "s    ";
            }
            listOfResult.add(
                    removeSuffixOfString(
                            String.format(
                                    result,
                                    wordsInLine.toArray()
                            ),
                            "    "
                    ).trim()
            );
        }
        return listOfResult;
    }

    public static String removeSuffixOfString(String input, String suffixWillBeRemoved) {
        int indexOfSuffix = input.lastIndexOf(suffixWillBeRemoved);
        if (indexOfSuffix > 0) {
            return input.substring(0, indexOfSuffix);
        }
        return input;
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public enum ParagraphMode {
        CENTER,
        ALIGN_LEFT,
        ALIGN_RIGHT,
    }

}
