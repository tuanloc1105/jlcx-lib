package vn.com.lcx.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public final class BuildObjectMapper {

    private BuildObjectMapper() {
    }

    public static JsonMapper getJsonMapper() {
        return new JsonMapper() {
            private static final long serialVersionUID = -2832088530758291739L;

            {
                findAndRegisterModules();
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                setSerializationInclusion(JsonInclude.Include.ALWAYS);
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
