package vn.io.lcx.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BuildGsonTest {

    // Simple POJO for serialization tests (no Lombok)
    static class SampleDto {
        private String name;
        private int value;

        public SampleDto() {
        }

        public SampleDto(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    static class DateHolder {
        private LocalDateTime dateTime;
        private LocalDate date;

        public DateHolder() {
        }

        public DateHolder(LocalDateTime dateTime, LocalDate date) {
            this.dateTime = dateTime;
            this.date = date;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    @Test
    void getGson_serializesLocalDateTime() {
        Gson gson = BuildGson.getGson();
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 10, 30, 45, 123000000);

        DateHolder holder = new DateHolder(dateTime, null);
        String json = gson.toJson(holder);

        // DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"
        assertTrue(json.contains("2024-03-15 10:30:45.123"),
                "Expected ISO-like format in JSON but got: " + json);
    }

    @Test
    void getGson_deserializesLocalDateTime() {
        Gson gson = BuildGson.getGson();
        // Use the default pattern "yyyy-MM-dd HH:mm:ss.SSS"
        String json = "{\"dateTime\":\"2024-03-15 10:30:45.123\"}";

        DateHolder holder = gson.fromJson(json, DateHolder.class);

        assertNotNull(holder.getDateTime());
        assertEquals(2024, holder.getDateTime().getYear());
        assertEquals(3, holder.getDateTime().getMonthValue());
        assertEquals(15, holder.getDateTime().getDayOfMonth());
        assertEquals(10, holder.getDateTime().getHour());
        assertEquals(30, holder.getDateTime().getMinute());
        assertEquals(45, holder.getDateTime().getSecond());
    }

    @Test
    void getGson_serializesLocalDate() {
        Gson gson = BuildGson.getGson();
        LocalDate date = LocalDate.of(2024, 6, 20);

        DateHolder holder = new DateHolder(null, date);
        String json = gson.toJson(holder);

        // DEFAULT_LOCAL_DATE_STRING_PATTERN = "yyyy-MM-dd"
        assertTrue(json.contains("2024-06-20"),
                "Expected date in yyyy-MM-dd format but got: " + json);
    }

    @Test
    void getGsonPrettyPrint_formatsJson() {
        Gson gson = BuildGson.getGsonPrettyPrint();
        SampleDto dto = new SampleDto("test", 42);

        String json = gson.toJson(dto);

        // Pretty print should contain newlines and indentation
        assertTrue(json.contains("\n"), "Expected newlines in pretty-printed JSON");
        assertTrue(json.contains("  "), "Expected indentation in pretty-printed JSON");
        assertTrue(json.contains("\"name\""), "Expected name field");
        assertTrue(json.contains("\"test\""), "Expected name value");
    }

    @Test
    void getVietnameseDateFormatGson_formatsDate() {
        Gson gson = BuildGson.getVietnameseDateFormatGson();
        LocalDate date = LocalDate.of(2024, 3, 15);

        DateHolder holder = new DateHolder(null, date);
        String json = gson.toJson(holder);

        // DEFAULT_LOCAL_DATE_VIETNAMESE_STRING_PATTERN = "dd-MM-yyyy"
        assertTrue(json.contains("15-03-2024"),
                "Expected date in dd-MM-yyyy format but got: " + json);
    }

    @Test
    void getGsonBuilder_returnsNonNull() {
        GsonBuilder builder = BuildGson.getGsonBuilder();

        assertNotNull(builder);
        // Should be able to create a Gson instance from it
        Gson gson = builder.create();
        assertNotNull(gson);
    }
}
