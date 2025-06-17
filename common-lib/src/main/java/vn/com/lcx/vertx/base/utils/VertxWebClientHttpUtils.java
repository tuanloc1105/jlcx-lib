package vn.com.lcx.vertx.base.utils;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.Map;

public class VertxWebClientHttpUtils {

    private final WebClient client;

    public VertxWebClientHttpUtils(Vertx vertx) {
        this.client = WebClient.create(vertx, new WebClientOptions()
                .setKeepAlive(true)
                .setConnectTimeout(5000) // Timeout 5s
        );
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
     * Sends an HTTP request using Vert.x WebClient and returns a Future of the expected response type.
     *
     * <p></p>
     *
     * <b>Usage:</b>
     * <pre>
     * sendRequest(HttpMethod.GET, "<a href="https://api.example.com/data">...</a>", null, null, JsonObject.class, BodyType.JSON)
     *     .onSuccess(result -> { // handle result  })
     *     .onFailure(err -> { // handle error });
     * </pre>
     *
     * <b>Notes:</b>
     * <ul>
     *   <li>For GET/DELETE, payload is ignored.</li>
     *   <li>For POST/PUT, payload is sent as JSON if provided.</li>
     *   <li>Throws IllegalArgumentException if unsupported BodyType is provided.</li>
     *   <li>Fails with RuntimeException if HTTP status code is not 2xx.</li>
     * </ul>
     *
     * @param method           The HTTP method (e.g., GET, POST, PUT, DELETE).
     * @param url              The absolute URL to send the request to.
     * @param headers          Optional HTTP headers to include in the request (can be null).
     * @param payload          Optional JSON payload for methods like POST/PUT (can be null).
     * @param responseType     The class type to map the response body to (e.g., String.class, JsonObject.class, etc.).
     * @param expectedBodyType The expected body type of the response (STRING, JSON, or BUFFER).
     * @param <T>              The type of the response object.
     * @return A Future containing the response mapped to the specified type, or a failed Future on error.
     */
    public <T> Future<T> sendRequest(
            HttpMethod method,
            String url,
            Map<String, String> headers,
            JsonObject payload,
            Class<T> responseType,
            BodyType expectedBodyType
    ) {
        HttpRequest<Buffer> request = client.requestAbs(method, url);

        if (headers != null) {
            headers.forEach(request::putHeader);
        }
        Future<HttpResponse<Buffer>> sendFuture = (method == HttpMethod.GET || method == HttpMethod.DELETE) ?
                request.send() :
                (payload != null ? request.sendJson(payload) : request.send());
        return sendFuture.compose(response -> {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (expectedBodyType == BodyType.STRING) {
                    // Ensure T is a String or compatible.
                    return Future.succeededFuture(responseType.cast(response.bodyAsString()));
                } else if (expectedBodyType == BodyType.JSON) {
                    // Ensure T is a JsonObject or a mappable POJO.
                    return Future.succeededFuture(response.bodyAsJsonObject().mapTo(responseType));
                } else if (expectedBodyType == BodyType.BUFFER) {
                    // Ensure T is a Buffer or compatible.
                    return Future.succeededFuture(responseType.cast(response.body()));
                } else {
                    // In default or unexpected cases, it may fail or return null/an Exception.
                    return Future.failedFuture(new IllegalArgumentException("Unsupported BodyType: " + expectedBodyType));
                }
            } else {
                return Future.failedFuture(new RuntimeException("HTTP Error: " + response.statusCode()));
            }
        });
    }

    public enum BodyType {
        JSON, STRING, BUFFER
    }

}
