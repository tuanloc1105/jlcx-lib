package vn.com.lcx.common.config;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Instance;
import vn.com.lcx.common.utils.HttpUtils;
import vn.com.lcx.common.utils.SocketUtils;

@Component
public class DefaultConfiguration {

    @Instance
    public Gson defaultGson() {
        return BuildGson.getGson();
    }

    @Instance
    public JsonMapper defaultJsonMapper() {
        return BuildObjectMapper.getJsonMapper();
    }

    @Instance
    public XmlMapper defaultXmlMapper() {
        return BuildObjectMapper.getXMLMapper();
    }

    @Instance
    public HttpUtils defaultHttpUtils() {
        return new HttpUtils();
    }

    @Instance
    public SocketUtils defaultSocketUtils() {
        return new SocketUtils();
    }

}
