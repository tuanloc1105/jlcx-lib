package vn.com.lcx.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import vn.com.lcx.common.config.BuildGson;
import vn.com.lcx.common.config.BuildObjectMapper;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.dto.Response;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("DuplicatedCode")
public class HttpUtils {

    private final static String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private final static String APPLICATION_JSON = "application/json";
    private final static String APPLICATION_XML = "application/xml";
    private final static String FORM_URLENCODED = "application/x-www-form-urlencoded";

    static {
        System.setProperty("http.keepAlive", "true");
        System.setProperty("http.maxConnections", "5"); // Số kết nối tối đa
    }

    private final Gson gson = BuildGson.getGson();
    private final Gson gsonBeautify = BuildGson.getGsonPrettyPrint();
    private final JsonMapper jsonMapper = BuildObjectMapper.getJsonMapper();
    private final XmlMapper xmlMapper = BuildObjectMapper.getXMLMapper();
    private boolean isBeautifyPrinting = false;

    public HttpUtils() {
    }

    public static void disableSSLVerification() throws Exception {
        // Create a TrustManager that trusts all certificates
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting TrustManager
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create a HostnameVerifier that doesn't verify hostnames
        HostnameVerifier allHostsValid = (hostname, session) -> true;

        // Install the all-trusting HostnameVerifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    public Gson getGson() {
        return gson;
    }

    public Gson getGsonBeautify() {
        return gsonBeautify;
    }

    public JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    public XmlMapper getXmlMapper() {
        return xmlMapper;
    }

    public boolean isBeautifyPrinting() {
        return isBeautifyPrinting;
    }

    public void setBeautifyPrinting(boolean beautifyPrinting) {
        isBeautifyPrinting = beautifyPrinting;
    }

    private HttpURLConnection generateConnection(String host) throws Exception {
        URL url = (new URI(host)).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true); // Hỗ trợ tự động redirect nếu có
        connection.setUseCaches(false); // Giảm caching để tránh lỗi dữ liệu cũ
        return connection;
    }

    public <T> Response<T> sendRequestJson(Object data,
                                           String url,
                                           HttpMethod method,
                                           Map<String, String> requestHeader,
                                           TypeToken<T> targetClass) throws Exception {
        final var startingTime = (double) System.currentTimeMillis();
        final var httpLogMessage = new StringBuilder("\nURL: ").append(url);
        HttpURLConnection http = this.generateConnection(url);
        final var responseBuilder = Response.<T>builder();
        http.setRequestMethod(method.name());
        http.setRequestProperty(CONTENT_TYPE_HEADER_NAME, APPLICATION_JSON);
        if (requestHeader != null && !requestHeader.isEmpty()) {
            for (Map.Entry<String, String> entry : requestHeader.entrySet()) {
                http.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        httpLogMessage.append("\n- Request header: ");
        httpLogMessage.append(JsonMaskingUtils.maskJsonFields(gson, gson.toJson(requestHeader)));
        if (data != null && (HttpMethod.POST.name().equals(method.name()) || HttpMethod.PUT.name().equals(method.name()))) {
            http.setDoOutput(true);
            httpLogMessage.append("\n- Request body: ");
            var dataInput = this.gson.toJson(data);
            httpLogMessage.append("\n")
                    .append(this.isBeautifyPrinting ? "" : "\t")
                    .append(this.isBeautifyPrinting ? this.formatJSON(dataInput) : dataInput);
            byte[] out = dataInput.getBytes(StandardCharsets.UTF_8);
            OutputStream stream = http.getOutputStream();
            stream.write(out);
            stream.close();
        }
        try (
                final InputStream responseStream = http.getResponseCode() == 200 ? http.getInputStream() : http.getErrorStream();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(
                                responseStream == null ? new ByteArrayInputStream(new byte[0]) : responseStream,
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            StringBuilder stringBuilder = new StringBuilder();
            String output;
            while ((output = bufferedReader.readLine()) != null) {
                stringBuilder.append(output);
            }
            httpLogMessage.append("\n- Response status code: ").append(http.getResponseCode());
            httpLogMessage.append("\n- Response header: ");
            httpLogMessage.append(JsonMaskingUtils.maskJsonFields(gson, gson.toJson(http.getHeaderFields())));
            httpLogMessage.append("\n- Response body: ");
            httpLogMessage.append("\n")
                    .append(this.isBeautifyPrinting ? "" : "\t")
                    .append(this.isBeautifyPrinting ? this.formatJSON(stringBuilder.toString()) : stringBuilder.toString());
            LogUtils.writeLog2(LogUtils.Level.INFO, httpLogMessage.toString());
            T result = null;
            if (MyStringUtils.stringIsJsonFormat(stringBuilder.toString())) {
                result = this.gson.fromJson(stringBuilder.toString(), targetClass.getType());
            }
            // http.disconnect();
            responseBuilder.code(http.getResponseCode())
                    .msg(http.getResponseMessage())
                    .errorResponse(http.getResponseCode() == 200 ? null : stringBuilder.toString())
                    .responseHeaders(http.getHeaderFields())
                    .response(result);
            return responseBuilder.build();
        } finally {
            final var endingTime = (double) System.currentTimeMillis();
            final var duration = (endingTime - startingTime) / 1000D;
            LogUtils.writeLog2(LogUtils.Level.INFO, "Took {} second(s) to made a HTTP request", duration);
        }
    }

    public <T> Response<T> sendRequestXML(Object data,
                                          String url,
                                          HttpMethod method,
                                          Map<String, String> requestHeader,
                                          TypeReference<T> targetClass) throws Exception {
        final var startingTime = (double) System.currentTimeMillis();
        final var httpLogMessage = new StringBuilder("\nURL: ").append(url);
        HttpURLConnection http = this.generateConnection(url);
        final var responseBuilder = Response.<T>builder();
        http.setRequestMethod(method.name());
        http.setRequestProperty(CONTENT_TYPE_HEADER_NAME, APPLICATION_XML);
        if (requestHeader != null && !requestHeader.isEmpty()) {
            for (Map.Entry<String, String> entry : requestHeader.entrySet()) {
                http.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        httpLogMessage.append("\n- Request header");
        for (Map.Entry<String, List<String>> header : http.getRequestProperties().entrySet()) {
            httpLogMessage.append("\n    - ").append(header.getKey()).append(": ").append(String.join(", ", header.getValue()));
        }
        if (data != null && (HttpMethod.POST.name().equals(method.name()) || HttpMethod.PUT.name().equals(method.name()))) {
            http.setDoOutput(true);
            httpLogMessage.append("\n- Request body: ");
            var dataInput = this.xmlMapper.writeValueAsString(data);
            httpLogMessage.append("\n")
                    .append(this.isBeautifyPrinting ? "" : "\t")
                    .append(this.isBeautifyPrinting ? this.formatXML(dataInput) : dataInput);
            byte[] out = dataInput.getBytes(StandardCharsets.UTF_8);
            OutputStream stream = http.getOutputStream();
            stream.write(out);
            stream.close();
        }
        try (
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(
                                http.getResponseCode() == 200 ? http.getInputStream() : http.getErrorStream(),
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            StringBuilder stringBuilder = new StringBuilder();
            String output;
            while ((output = bufferedReader.readLine()) != null) {
                stringBuilder.append(output);
            }
            httpLogMessage.append("\n- Response status code: ").append(http.getResponseCode());
            httpLogMessage.append("\n- Response header");
            for (Map.Entry<String, List<String>> header : http.getHeaderFields().entrySet()) {
                httpLogMessage.append("\n    - ").append(header.getKey()).append(": ").append(String.join(", ", header.getValue()));
            }
            httpLogMessage.append("\n- Response body: ");
            httpLogMessage.append("\n")
                    .append(this.isBeautifyPrinting ? "" : "\t")
                    .append(this.isBeautifyPrinting ? this.formatXML(stringBuilder.toString()) : stringBuilder.toString());
            LogUtils.writeLog2(LogUtils.Level.INFO, httpLogMessage.toString());
            T result = this.xmlMapper.readValue(stringBuilder.toString(), targetClass);
            // http.disconnect();
            responseBuilder.code(http.getResponseCode())
                    .msg(http.getResponseMessage())
                    .errorResponse(http.getResponseCode() == 200 ? null : stringBuilder.toString())
                    .responseHeaders(http.getHeaderFields())
                    .response(result);
            return responseBuilder.build();
        } finally {
            final var endingTime = (double) System.currentTimeMillis();
            final var duration = (endingTime - startingTime) / 1000D;
            LogUtils.writeLog2(LogUtils.Level.INFO, "Took {} second(s) to made a HTTP request", duration);
        }
    }

    public <T> Response<T> sendRequestFormUrlencoded(Map<String, String> data,
                                                     String url,
                                                     HttpMethod method,
                                                     Map<String, String> requestHeader,
                                                     TypeToken<T> targetClass) throws Exception {
        final var startingTime = (double) System.currentTimeMillis();
        final var httpLogMessage = new StringBuilder("\nURL: ").append(url);
        HttpURLConnection http = this.generateConnection(url);
        final var responseBuilder = Response.<T>builder();
        http.setRequestMethod(method.name());
        http.setRequestProperty(CONTENT_TYPE_HEADER_NAME, FORM_URLENCODED);
        if (requestHeader != null && !requestHeader.isEmpty()) {
            for (Map.Entry<String, String> entry : requestHeader.entrySet()) {
                http.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        httpLogMessage.append("\n- Response status code: ").append(http.getResponseCode());
        httpLogMessage.append("\n- Request header");
        for (Map.Entry<String, List<String>> header : http.getRequestProperties().entrySet()) {
            httpLogMessage.append("\n    - ").append(header.getKey()).append(": ").append(String.join(", ", header.getValue()));
        }
        if (data != null && !data.isEmpty() && (HttpMethod.POST.name().equals(method.name()) || HttpMethod.PUT.name().equals(method.name()))) {
            http.setDoOutput(true);
            httpLogMessage.append("\n- Request body: ");
            var urlArray = new ArrayList<String>();
            for (Map.Entry<String, String> element : data.entrySet()) {
                if (Optional.ofNullable(element.getValue()).filter(s -> !s.isEmpty()).isPresent()) {
                    urlArray.add(element.getKey() + "=" + MyStringUtils.encodeUrl(element.getValue()));
                }
            }
            var dataInput = String.join("&", urlArray);
            httpLogMessage.append("\n\t").append(dataInput);
            byte[] out = dataInput.getBytes(StandardCharsets.UTF_8);
            OutputStream stream = http.getOutputStream();
            stream.write(out);
            stream.close();
        }
        InputStream stream = http.getResponseCode() == 200 ? http.getInputStream() : http.getErrorStream();
        try (
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(
                                stream == null ? new ByteArrayInputStream(new byte[0]) : stream,
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            StringBuilder stringBuilder = new StringBuilder();
            String output;
            while ((output = bufferedReader.readLine()) != null) {
                stringBuilder.append(output);
            }
            httpLogMessage.append("\n- Response header");
            for (Map.Entry<String, List<String>> header : http.getHeaderFields().entrySet()) {
                httpLogMessage.append("\n    - ").append(header.getKey()).append(": ").append(String.join(", ", header.getValue()));
            }
            httpLogMessage.append("\n- Response body: ");
            httpLogMessage.append("\n\t").append(stringBuilder);
            LogUtils.writeLog2(LogUtils.Level.INFO, httpLogMessage.toString());
            T result = null;
            if (MyStringUtils.stringIsJsonFormat(stringBuilder.toString())) {
                result = this.gson.fromJson(stringBuilder.toString(), targetClass.getType());
            }
            // http.disconnect();
            responseBuilder.code(http.getResponseCode())
                    .msg(http.getResponseMessage())
                    .errorResponse(http.getResponseCode() == 200 ? null : stringBuilder.toString())
                    .responseHeaders(http.getHeaderFields())
                    .response(result);
            return responseBuilder.build();
        } finally {
            final var endingTime = (double) System.currentTimeMillis();
            final var duration = (endingTime - startingTime) / 1000D;
            LogUtils.writeLog2(LogUtils.Level.INFO, "Took {} second(s) to made a HTTP request", duration);
        }
    }

    public String formatXML(String xml) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            // Set the indentation properties
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Transform the input XML string into a pretty-printed format
            StreamSource source = new StreamSource(new StringReader(xml));
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            transformer.transform(source, result);

            return writer.toString();
        } catch (Exception e) {
            LogUtils.writeLog2(e.getMessage(), e);
            return CommonConstant.EMPTY_STRING;
        }
    }

    public String formatJSON(String json) {
        Object gsonObject = this.gsonBeautify.fromJson(json, Object.class);
        return this.gsonBeautify.toJson(gsonObject);
    }

    public enum HttpMethod {
        GET,
        HEAD,
        POST,
        PUT,
        PATCH,
        DELETE,
        OPTIONS,
        TRACE,
    }

}
