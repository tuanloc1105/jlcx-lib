package vn.com.lcx.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JSON masking utility that supports both Gson and Jackson.
 */
public class JsonMaskingUtils {

    public static final List<String> CUSTOM_FIELD = new ArrayList<>();

    /**
     * Mask the values of specified fields in a JSON string using either Gson or Jackson.
     *
     * @param jsonHandler     Either Gson or Jackson ObjectMapper instance
     * @param inputJsonString The input JSON string
     * @param fieldNames      The names of the fields to mask (case-sensitive)
     * @return The JSON string with specified fields' values masked
     */
    public static String maskJsonFields(final Object jsonHandler,
                                        final String inputJsonString,
                                        final String... fieldNames) {
        if (StringUtils.isBlank(inputJsonString) ||
                fieldNames == null ||
                fieldNames.length == 0) {
            return inputJsonString;
        }

        if (!MyStringUtils.stringIsJsonFormat(inputJsonString)) {
            return inputJsonString;
        }

        if (!(jsonHandler instanceof Gson) && !(jsonHandler instanceof ObjectMapper)) {
            throw new UnsupportedOperationException("Unknown json handler. Only support `Gson` and `Jackson`");
        }

        try {
            Object jsonMap;
            if (jsonHandler instanceof Gson) {
                Gson gson = (Gson) jsonHandler;
                JsonReader jsonReader = new JsonReader(new StringReader(inputJsonString));
                jsonMap = gson.fromJson(jsonReader, CommonConstant.HASH_MAP_GSON_TYPE_TOKEN.getType());
                maskJsonFieldsRecursively(jsonMap, Arrays.asList(fieldNames));
                return gson.toJson(jsonMap);
            } else {
                ObjectMapper objectMapper = (ObjectMapper) jsonHandler;
                jsonMap = objectMapper.readValue(inputJsonString, Map.class);
                maskJsonFieldsRecursively(jsonMap, Arrays.asList(fieldNames));
                return objectMapper.writeValueAsString(jsonMap);
            }
        } catch (Exception e) {
            LogUtils.writeLog("Error masking JSON fields: " + e.getMessage(), e, LogUtils.Level.WARN);
            return inputJsonString;
        }
    }

    /**
     * Masks the values of default sensitive fields in a JSON string.
     *
     * @param jsonHandler     Either Gson or Jackson ObjectMapper instance
     * @param inputJsonString The input JSON string
     * @return The JSON string with default sensitive fields' values masked
     */
    public static String maskJsonFields(final Object jsonHandler, final String inputJsonString) {
        return maskJsonFields(jsonHandler, inputJsonString,
                CUSTOM_FIELD.isEmpty() ?
                        CommonConstant.SENSITIVE_FIELD_NAMES.toArray(String[]::new) :
                        CUSTOM_FIELD.toArray(String[]::new)
        );
    }

    /**
     * Recursively traverse a JSON object (Map/List) and mask the values of specified fields.
     *
     * @param jsonObject The JSON object (Map or List) to process
     * @param fieldNames The list of field names to mask
     */
    @SuppressWarnings("unchecked")
    private static void maskJsonFieldsRecursively(Object jsonObject, List<String> fieldNames) {
        if (jsonObject instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) jsonObject;

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (fieldNames.contains(key)) {
                    if (value instanceof String) {
                        String strValue = (String) value;
                        if (StringUtils.isNotBlank(strValue)) {
                            map.put(key, repeatString("*", Math.min(strValue.length(), 8)));
                        }
                    } else if (value instanceof Number || value instanceof Boolean) {
                        map.put(key, "***");
                    } else {
                        map.put(key, "***");
                    }
                } else {
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

    private static String repeatString(String s, int count) {
        return String.join("", Collections.nCopies(count, s));
    }
}
