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
            if (responseType == String.class) {
                return responseType.cast(responseBody); // Trả về raw String nếu cần
            }
            return responseBody.mapTo(responseType); // Convert JSON thành class mong muốn
        });
    }

}
