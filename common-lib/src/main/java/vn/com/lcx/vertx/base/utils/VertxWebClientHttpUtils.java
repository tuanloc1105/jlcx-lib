package vn.com.lcx.vertx.base.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.dto.Response;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.common.utils.MyStringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VertxWebClientHttpUtils {

    private final WebClient client;
    private final Gson gson;

    public VertxWebClientHttpUtils(Vertx vertx, Gson gson) {
        this.client = WebClient.create(vertx, new WebClientOptions()
                .setKeepAlive(true)
                .setConnectTimeout(5000) // Timeout 5s
        );
        this.gson = gson;
    }

    @Deprecated
    public <T> Future<T> callApi(
            HttpMethod method,
            String url,
            Map<String, String> headers,
            JsonObject payload,
            Class<T> responseType
    ) {
        HttpRequest<Buffer> request = client.requestAbs(method, url);

        if (headers != null) {
            headers.forEach(request::putHeader);
        }

        Future<JsonObject> futureResponse;
        if (method == HttpMethod.GET || method == HttpMethod.DELETE) {
            futureResponse = request.send().map(HttpResponse::bodyAsJsonObject);
        } else {
            futureResponse = (payload != null ? request.sendJson(payload) : request.send())
                    .map(HttpResponse::bodyAsJsonObject);
        }

        return futureResponse.map(responseBody -> {
            return responseBody.mapTo(responseType);
        });
    }

    /**
     * Sends an HTTP request and returns a Future containing the response.
     *
     * <p>This method provides comprehensive HTTP request functionality with detailed logging,
     * error handling, and response processing. It supports all HTTP methods and automatically
     * handles request/response serialization using Gson.</p>
     *
     * <p>Key features:</p>
     * <ul>
     *   <li>Automatic request/response logging with masked sensitive fields</li>
     *   <li>Duration tracking for performance monitoring</li>
     *   <li>Comprehensive error handling and status code checking</li>
     *   <li>Flexible payload handling (JSON for POST/PUT, empty for GET/DELETE)</li>
     *   <li>Response header capture and processing</li>
     * </ul>
     *
     * @param <T>          The type of the response data to be deserialized
     * @param context      The routing context for logging purposes. Used to associate logs with the current request
     * @param method       The HTTP method to use for the request (GET, POST, PUT, DELETE, etc.)
     * @param url          The complete URL to send the request to
     * @param headers      Optional map of HTTP headers to include in the request. Can be null
     * @param payload      The request payload object. Will be serialized to JSON for POST/PUT requests.
     *                     Ignored for GET/DELETE requests. Can be null
     * @param responseType The TypeToken representing the expected response type for deserialization
     * @return A Future that completes with a Response&lt;T&gt; object containing:
     * <ul>
     *   <li>HTTP status code</li>
     *   <li>Response headers as a Map</li>
     *   <li>Deserialized response data of type T (if status is 200)</li>
     *   <li>Error response string (if status is not 200)</li>
     * </ul>
     * @throws IllegalArgumentException if url is null or empty
     * @throws RuntimeException         if JSON serialization/deserialization fails
     * @example <pre>{@code
     * // GET request example
     * Future<Response<User>> future = sendRequest(
     *     context,
     *     HttpMethod.GET,
     *     "https://api.example.com/users/123",
     *     Map.of("Authorization", "Bearer token"),
     *     null,
     *     new TypeToken<User>(){}
     * );
     *
     * // POST request example
     * User newUser = new User("John", "Doe");
     * Future<Response<User>> future = sendRequest(
     *     context,
     *     HttpMethod.POST,
     *     "https://api.example.com/users",
     *     Map.of("Content-Type", "application/json"),
     *     newUser,
     *     new TypeToken<User>(){}
     * );
     * }</pre>
     */
    public <T> Future<Response<T>> sendRequest(
            RoutingContext context,
            HttpMethod method,
            String url,
            Map<String, String> headers,
            Object payload,
            TypeToken<T> responseType
    ) {
        final var startingTime = (double) System.currentTimeMillis();
        final var httpLogMessage = new StringBuilder("\nURL: ")
                .append(url).append("\nMethod: ").append(method.name());
        HttpRequest<Buffer> request = client.requestAbs(method, url);
        httpLogMessage.append("\n- Request header");
        final var responseBuilder = Response.<T>builder();
        if (headers != null) {
            headers.forEach((name, value) -> {
                request.putHeader(name, value);
                httpLogMessage.append("\n    - ").append(name).append(": ").append(value);
            });
        }
        final String jsonString = Optional.ofNullable(payload).map(gson::toJson).orElse(CommonConstant.EMPTY_STRING);
        httpLogMessage.append("\n- Request body: ").append(MyStringUtils.maskJsonFields(gson, jsonString));
        Future<HttpResponse<Buffer>> sendFuture;
        if (payload instanceof Map) {
            @SuppressWarnings("unchecked") Map<String, String> map = (Map<String, String>) payload;
            MultiMap form = MultiMap.caseInsensitiveMultiMap();
            form.addAll(map);
            sendFuture = request.sendForm(form, CommonConstant.UTF_8_STANDARD_CHARSET);
        } else {
            sendFuture = (method == HttpMethod.GET || method == HttpMethod.DELETE) ?
                    request.send() :
                    (!jsonString.isEmpty() ? request.sendJson(new JsonObject(jsonString)) : request.send());
        }
        return sendFuture.compose(response -> {
            final var responseStatusCode = response.statusCode();
            final var responseBodyAsString = response.bodyAsString();
            httpLogMessage.append("\n- Response status code: ").append(responseStatusCode);
            httpLogMessage.append("\n- Response header");
            final var headerMap = new HashMap<String, List<String>>();
            for (Map.Entry<String, String> header : response.headers()) {
                httpLogMessage.append("\n    - ").append(header.getKey()).append(": ").append(header.getValue());
                headerMap.put(header.getKey(), Collections.singletonList(header.getValue()));
            }
            httpLogMessage.append("\n- Response body: ").append(MyStringUtils.maskJsonFields(gson, responseBodyAsString));
            final var endingTime = (double) System.currentTimeMillis();
            final var duration = endingTime - startingTime;
            httpLogMessage.append("\n- Duration: ").append(duration).append(" ms");
            responseBuilder.code(responseStatusCode)
                    .responseHeaders(headerMap)
                    .errorResponse(responseStatusCode == 200 ? null : responseBodyAsString)
                    .response(responseBodyAsString == null ? null : gson.fromJson(responseBodyAsString, responseType));
            LogUtils.writeLog(context, LogUtils.Level.INFO, httpLogMessage.toString());
            return Future.succeededFuture(responseBuilder.build());
        });
    }

}
