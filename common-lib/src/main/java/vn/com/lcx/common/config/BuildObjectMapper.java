package vn.com.lcx.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import vn.com.lcx.common.constant.CommonConstant;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class BuildObjectMapper {

    private BuildObjectMapper() {
    }

    public static JsonMapper getJsonMapper() {
        return new JsonMapper() {
            private static final long serialVersionUID = -2832088530758291739L;

            {
                findAndRegisterModules();
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS);
            }
        };
    }

    public static JsonMapper getJsonMapper2() {
        return new JsonMapper() {
            private static final long serialVersionUID = 5374319019351307276L;

            {
                findAndRegisterModules();
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                coercionConfigFor(LogicalType.Enum)
                        .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
                enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
                setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
                setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
                setDateFormat(new SimpleDateFormat(CommonConstant.DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN));
                JavaTimeModule javaTimeModule = new JavaTimeModule();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_STRING_PATTERN);
                javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
                javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
            }
        };
    }

    public static XmlMapper getXMLMapper() {
        return new XmlMapper() {
            private static final long serialVersionUID = -2832088530758291739L;

            {
                findAndRegisterModules();
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                setSerializationInclusion(JsonInclude.Include.ALWAYS);
            }
        };
    }

}
