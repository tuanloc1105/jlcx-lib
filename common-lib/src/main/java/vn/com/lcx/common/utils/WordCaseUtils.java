package vn.com.lcx.common.utils;

import vn.com.lcx.common.constant.CommonConstant;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class WordCaseUtils {

    private static final String commandToFindProcess = "docker pull";

    // public static String capitalize(String str) {
    //     return str.substring(0, 1).toUpperCase() + str.substring(1);
    // }
    private WordCaseUtils() {
    }

    public static String convertCamelToConstant(String camelCase) {
        StringBuilder result = new StringBuilder();
        for (char c : camelCase.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append('_');
                result.append(Character.toUpperCase(c));
            } else {
                result.append(Character.toUpperCase(c));
            }
        }
        return result.toString();
    }

    public static String pascalToNormal(String pascal) {
        return (pascal.replaceAll("([a-z])([A-Z])", "$1 $2")).toLowerCase().replace("exception", CommonConstant.EMPTY_STRING);
    }

    public static String convertAllCase(String stringToConvertToNormalText,
                                        String stringToCalculateBytesAndBit,
                                        String stringToEscape,
                                        String stringToUnescape,
                                        String stringToMinify,
                                        int startingNumber,
                                        int endingNumber,
                                        int jumpingStep) {
        StringBuilder result = new StringBuilder();

        List<String> normalStringList = new ArrayList<>();
        List<String> toCamelCaseList = new ArrayList<>();
        List<String> toPascalCaseList = new ArrayList<>();
        List<String> toSnakeCaseList = new ArrayList<>();
        List<String> toKebabCaseList = new ArrayList<>();
        List<String> toConstantCaseList = new ArrayList<>();
        List<String> upperCaseStringList = new ArrayList<>();
        List<String> lowerCaseStringList = new ArrayList<>();
        List<String> capitalizedStringList = new ArrayList<>();
        List<String> swappedCaseStringList = new ArrayList<>();
        List<String> toPathCaseList = new ArrayList<>();
        List<String> toTitleCaseList = new ArrayList<>();
        List<String> toDotCaseList = new ArrayList<>();

        if (stringToConvertToNormalText == null || stringToConvertToNormalText.isEmpty()) {
            System.exit(0);
        }

        String[] arrayOfStringToConvertToNormalText = stringToConvertToNormalText.trim().split("\n");
        int[] byteBit = calculateBytesBits(stringToCalculateBytesAndBit.trim());
        int byteCount = byteBit[0];
        int bitCount = byteBit[1];

        for (String element : arrayOfStringToConvertToNormalText) {
            element = fromCamelCase(element);
            element = fromSnakeCase(element);
            element = fromKebabCase(element);
            element = fromPascalCase(element);
            String[] stringToConvertToNormalTextArray = element.trim().split("\\s+");

            String normalString = removeNonAlpha(String.join(" ", stringToConvertToNormalTextArray)).toLowerCase();
            String string = normalString;
            String upperCaseString = string.toUpperCase();
            String lowerCaseString = string.toLowerCase();
            String capitalizedString = capitalize(string);
            String swappedCaseString = swapCase(string);

            normalStringList.add(normalString);
            toCamelCaseList.add(toCamelCase(string));
            toPascalCaseList.add(toPascalCase(string));
            toSnakeCaseList.add(toSnakeCase(string));
            toKebabCaseList.add(toKebabCase(string));
            toConstantCaseList.add(toConstantCase(string));
            upperCaseStringList.add(upperCaseString);
            lowerCaseStringList.add(lowerCaseString);
            capitalizedStringList.add(capitalizedString);
            swappedCaseStringList.add(swappedCaseString);
            toPathCaseList.add(toPathCase(string));
            toTitleCaseList.add(toTitleCase(string));
            toDotCaseList.add(toDotCase(string));
        }

        result.append("####################################\n");
        result.append("\n");
        result.append(String.format("Bytes: %d%n", byteCount));
        result.append("\n");
        result.append(String.format("Bits: %d%n", bitCount));
        result.append("\n");
        result.append(String.format("Normal text:                %s%n", String.join("\n                             ", normalStringList)));
        result.append("\n");
        result.append(String.format("Dot Case:                %s%n", String.join("\n                             ", toDotCaseList)));
        result.append("\n");
        result.append(String.format("Upper case:                %s%n", String.join("\n                             ", upperCaseStringList)));
        result.append("\n");
        result.append(String.format("Lower case:                %s%n", String.join("\n                             ", lowerCaseStringList)));
        result.append("\n");
        result.append(String.format("Capitalized:                %s%n", String.join("\n                             ", capitalizedStringList)));
        result.append("\n");
        result.append(String.format("Swapped case:                %s%n", String.join("\n                             ", swappedCaseStringList)));
        result.append("\n");
        result.append(String.format("Path Case:                %s%n", String.join("\n                             ", toPathCaseList)));
        result.append("\n");
        result.append(String.format("Title Case:                %s%n", String.join("\n                             ", toTitleCaseList)));
        result.append("\n");
        result.append(String.format("Kebab case:                %s%n", String.join("\n                             ", toKebabCaseList)));
        result.append("\n");
        result.append(String.format("Constant case:                %s%n", String.join("\n                             ", toConstantCaseList)));
        result.append("\n");
        result.append(String.format("Camel Case:                %s%n", String.join("\n                             ", toCamelCaseList)));
        result.append("\n");
        result.append(String.format("Pascal case:                %s%n", String.join("\n                             ", toPascalCaseList)));
        result.append("\n");
        result.append(String.format("Snake case:                %s%n", String.join("\n                             ", toSnakeCaseList)));
        result.append("\n");
        result.append("####################################\n");
        result.append("Number order\n");
        for (int i = startingNumber; i <= endingNumber; i += jumpingStep) {
            result.append(i).append(". \n");
        }
        result.append("\n");
        for (int i = startingNumber; i <= endingNumber; i += jumpingStep) {
            result.append(i).append(", \n");
        }
        result.append("\n");
        result.append("Number order is reversed\n");
        for (int i = endingNumber; i >= startingNumber; i -= jumpingStep) {
            result.append(i).append(". \n");
        }
        result.append("\n");
        for (int i = endingNumber; i >= startingNumber; i -= jumpingStep) {
            result.append(i).append(", \n");
        }
        result.append("\n");
        result.append(String.format("escape:\n%s%n", escapeString(stringToEscape)));
        result.append(String.format("unescape:\n%s%n", unescapeString(stringToUnescape)));
        result.append(String.format("minify:\n%s%n", minify(stringToMinify, true)));
        return result.toString();
    }

    public static int[] calculateBytesBits(String inputString) {
        int numBytes = inputString.getBytes(StandardCharsets.UTF_8).length;
        int numBits = numBytes * 8;
        return new int[]{numBytes, numBits};
    }

    public static String escapeString(String inputString) {
        return inputString.trim()
                .replace("\n", "\\n")
                .replace("\f", "\\f")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("    ", "")
                .replace("\"", "\\\"");
    }

    public static String unescapeString(String inputString) {
        return inputString.trim()
                .replace("\\n", "\n")
                .replace("\\f", "\f")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"");
    }

    public static String minify(String inputString, boolean sql) {
        String minified = inputString.trim()
                .replace("\n", " ")
                .replace("\t", "")
                .replace("    ", "")
                .replace("\": \"", "\":\"")
                .replace("\", \"", "\",\"")
                .replace("{ ", "{")
                .replace("} ", "}")
                .replace(": {", ":{")
                .replace("\" }", "\"}");

        if (sql) {
            return minified
                    .replace(";  ", ";\n")
                    .replace("; ", ";\n")
                    .replace(" SELECT ", ";\nSELECT ")
                    .replace(" UPDATE ", ";\nUPDATE ")
                    .replace(" DELETE ", ";\nDELETE ")
                    .replace(" INSERT INTO ", ";\nINSERT INTO ")
                    .replace("  SELECT ", ";\nSELECT ")
                    .replace("  UPDATE ", ";\nUPDATE ")
                    .replace("  DELETE ", ";\nDELETE ")
                    .replace("  INSERT INTO ", ";\nINSERT INTO ");
        }
        return minified;
    }

    public static String removeNonAlpha(String string) {
        return string.replaceAll("[^a-zA-Z0-9\\s]", "");
    }

    public static String fromCamelCase(String string) {
        StringBuilder result = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append(' ').append(c);
            } else {
                result.append(c);
            }
        }
        return result.toString().trim();
    }

    public static String fromSnakeCase(String string) {
        return String.join(" ", string.split("_"));
    }

    public static String fromKebabCase(String string) {
        return String.join(" ", string.split("-"));
    }

    public static String fromPascalCase(String string) {
        return fromCamelCase(string);
    }

    public static String toCamelCase(String string) {
        String[] words = string.split(" ");
        StringBuilder camelCaseString = new StringBuilder(words[0].toLowerCase());
        for (int i = 1; i < words.length; i++) {
            camelCaseString.append(Character.toUpperCase(words[i].charAt(0)))
                    .append(words[i].substring(1).toLowerCase());
        }
        return camelCaseString.toString();
    }

    public static String toPascalCase(String string) {
        String[] words = string.split(" ");
        StringBuilder pascalCaseString = new StringBuilder();
        for (String word : words) {
            pascalCaseString.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase());
        }
        return pascalCaseString.toString();
    }

    public static String toSnakeCase(String string) {
        return string.toLowerCase().replace(" ", "_");
    }

    public static String toKebabCase(String string) {
        return string.toLowerCase().replace(" ", "-");
    }

    public static String toConstantCase(String string) {
        return string.toUpperCase().replace(" ", "_");
    }

    public static String toPathCase(String string) {
        return string.toLowerCase().replace(" ", System.getProperty("file.separator"));
    }

    public static String toTitleCase(String string) {
        String[] words = string.split(" ");
        StringBuilder titleCaseString = new StringBuilder();
        for (String word : words) {
            titleCaseString.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase()).append(" ");
        }
        return titleCaseString.toString().trim();
    }

    public static String toDotCase(String string) {
        return String.join(".", string.split(" "));
    }

    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String swapCase(String string) {
        StringBuilder swapped = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (Character.isUpperCase(c)) {
                swapped.append(Character.toLowerCase(c));
            } else {
                swapped.append(Character.toUpperCase(c));
            }
        }
        return swapped.toString();
    }

}
