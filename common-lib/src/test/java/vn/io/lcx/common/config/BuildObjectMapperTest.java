package vn.io.lcx.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BuildObjectMapperTest {

    // Simple POJO for serialization tests (no Lombok)
    static class SimpleDto {
        private String name;
        private Integer count;

        public SimpleDto() {
        }

        public SimpleDto(String name, Integer count) {
            this.name = name;
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

    static class DtoWithNulls {
        private String name;
        private String nullField;
        private String emptyField;

        public DtoWithNulls() {
        }

        public DtoWithNulls(String name, String nullField, String emptyField) {
            this.name = name;
            this.nullField = nullField;
            this.emptyField = emptyField;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNullField() {
            return nullField;
        }

        public void setNullField(String nullField) {
            this.nullField = nullField;
        }

        public String getEmptyField() {
            return emptyField;
        }

        public void setEmptyField(String emptyField) {
            this.emptyField = emptyField;
        }
    }

    static class DtoWithDate {
        private String name;
        private LocalDate date;

        public DtoWithDate() {
        }

        public DtoWithDate(String name, LocalDate date) {
            this.name = name;
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    @Test
    void getJsonMapper_ignoresUnknownProperties() throws JsonProcessingException {
        JsonMapper mapper = BuildObjectMapper.getJsonMapper();
        // JSON with an extra field not in SimpleDto
        String json = "{\"name\":\"test\",\"count\":5,\"unknownField\":\"surprise\"}";

        SimpleDto dto = mapper.readValue(json, SimpleDto.class);

        assertNotNull(dto);
        assertEquals("test", dto.getName());
        assertEquals(5, dto.getCount());
    }

    @Test
    void getJsonMapper2_nonEmptyNonNull() throws JsonProcessingException {
        JsonMapper mapper = BuildObjectMapper.getJsonMapper2();
        // The mapper sets NON_NULL as the last inclusion rule (overriding NON_EMPTY),
        // so null fields are excluded but empty strings are still included.
        DtoWithNulls dto = new DtoWithNulls("hello", null, "visible");

        String json = mapper.writeValueAsString(dto);

        assertNotNull(json);
        assertTrue(json.contains("\"name\""), "Expected name field in JSON");
        assertTrue(json.contains("hello"), "Expected name value in JSON");
        assertFalse(json.contains("nullField"), "Null fields should be excluded");
    }

    @Test
    void getJsonMapper2_serializesLocalDate() throws JsonProcessingException {
        JsonMapper mapper = BuildObjectMapper.getJsonMapper2();
        DtoWithDate dto = new DtoWithDate("test", LocalDate.of(2024, 6, 15));

        String json = mapper.writeValueAsString(dto);

        assertNotNull(json);
        // DEFAULT_LOCAL_DATE_STRING_PATTERN = "yyyy-MM-dd"
        assertTrue(json.contains("2024-06-15"),
                "Expected date in yyyy-MM-dd format but got: " + json);
    }

    @Test
    void getXMLMapper_ignoresUnknownProperties() throws JsonProcessingException {
        XmlMapper mapper = BuildObjectMapper.getXMLMapper();
        // XML with an extra element not in SimpleDto
        String xml = "<SimpleDto><name>test</name><count>5</count><unknownField>surprise</unknownField></SimpleDto>";

        SimpleDto dto = mapper.readValue(xml, SimpleDto.class);

        assertNotNull(dto);
        assertEquals("test", dto.getName());
        assertEquals(5, dto.getCount());
    }
}
