/*
package vn.com.lcx.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpUtilsTest {
    private HttpUtils httpUtils;

    @BeforeEach
    public void setUp() {
        httpUtils = new HttpUtils();
    }

    @Test
    public void testGsonGetters() {
        assertNotNull(httpUtils.getGson());
        assertNotNull(httpUtils.getGsonBeautify());
    }

    @Test
    public void testJsonMapperGetter() {
        assertNotNull(httpUtils.getJsonMapper());
    }

    @Test
    public void testXmlMapperGetter() {
        assertNotNull(httpUtils.getXmlMapper());
    }

    @Test
    public void testBeautifyPrintingFlag() {
        assertFalse(httpUtils.isBeautifyPrinting());
        httpUtils.setBeautifyPrinting(true);
        assertTrue(httpUtils.isBeautifyPrinting());
    }

    @Test
    public void testFormatJSON() {
        String uglyJson = "{\"a\":1,\"b\":2}";
        String prettyJson = httpUtils.formatJSON(uglyJson);
        assertTrue(prettyJson.contains("\n"));
        assertTrue(prettyJson.contains("a"));
        assertTrue(prettyJson.contains("b"));
    }

    @Test
    public void testFormatXML() {
        String uglyXml = "<root><a>1</a><b>2</b></root>";
        String prettyXml = httpUtils.formatXML(uglyXml);
        assertTrue(prettyXml.contains("\n"));
        assertTrue(prettyXml.contains("<a>1</a>"));
        assertTrue(prettyXml.contains("<b>2</b>"));
    }

    // Note: Actual HTTP request methods should be tested with integration tests or using a mock server.
}
*/
