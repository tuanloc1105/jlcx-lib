package vn.com.lcx.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.com.lcx.common.constant.CommonConstant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class BuildGson {

    private final static Logger log = LoggerFactory.getLogger("GSON");

    private BuildGson() {
    }

    public static GsonBuilder getGsonBuilder() {
        return new GsonBuilder()
                // .serializeNulls()
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> {
                            try {
                                return LocalDateTime.parse(
                                        json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_INSTANT
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                            try {
                                return LocalDateTime.parse(
                                        json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                            try {
                                return LocalDateTime.parse(
                                        json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN)
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                            try {
                                return LocalDateTime.parse(
                                        json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_VIETNAMESE_STRING_PATTERN)
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                            try {
                                return LocalDateTime.parse(
                                        json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(CommonConstant.LOCAL_DATE_TIME_STRING_PATTERN_1)
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                            try {
                                return LocalDateTime.parse(
                                        json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(CommonConstant.LOCAL_DATE_TIME_STRING_PATTERN_2)
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                            try {
                                return LocalDateTime.parse(
                                        json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(CommonConstant.LOCAL_DATE_TIME_STRING_PATTERN_3)
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                            try {
                                return LocalDateTime.parse(
                                        json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(CommonConstant.LOCAL_DATE_TIME_STRING_PATTERN_4)
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                            return null;
                        }
                )
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (localDateTime, type, jsonSerializationContext) ->
                                new JsonPrimitive(localDateTime.format(DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN)))
                )
                .registerTypeAdapter(
                        LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext) -> {
                            try {
                                return LocalDate.parse(
                                        json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_STRING_PATTERN)
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                            try {
                                return LocalDate.parse(
                                        json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_VIETNAMESE_STRING_PATTERN)
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                            return null;
                        }
                )
                .registerTypeAdapter(
                        LocalDate.class,
                        (JsonSerializer<LocalDate>) (localDate, type, jsonSerializationContext) ->
                                new JsonPrimitive(localDate.format(DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_STRING_PATTERN)))
                );
    }

    public static Gson getGson() {
        return getGsonBuilder().create();
    }

    public static Gson getGsonPrettyPrint() {
        return getGsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public static Gson getVietnameseDateFormatGson() {
        return new GsonBuilder()
                // .serializeNulls()
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> {
                            try {
                                return LocalDateTime.parse(
                                        json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_VIETNAMESE_STRING_PATTERN)
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                            return null;
                        }
                )
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (localDateTime, type, jsonSerializationContext) ->
                                new JsonPrimitive(localDateTime.format(DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_VIETNAMESE_STRING_PATTERN)))
                )
                .registerTypeAdapter(
                        LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext) -> {
                            try {
                                return LocalDate.parse(
                                        json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_VIETNAMESE_STRING_PATTERN)
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                            return null;
                        }
                )
                .registerTypeAdapter(
                        LocalDate.class,
                        (JsonSerializer<LocalDate>) (localDate, type, jsonSerializationContext) ->
                                new JsonPrimitive(localDate.format(DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_VIETNAMESE_STRING_PATTERN)))
                )
                .create();
    }

}
