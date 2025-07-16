package vn.com.lcx.vertx.base.custom;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.ParsedHeaderValues;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.UserContext;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmptyRoutingContext implements RoutingContext {

    private final HashMap<String, Object> map = new HashMap<>();

    public EmptyRoutingContext() {
    }

    @Override
    public HttpServerRequest request() {
        throw new NotImplementedException();
    }

    @Override
    public HttpServerResponse response() {
        throw new NotImplementedException();
    }

    @Override
    public void next() {
        throw new NotImplementedException();
    }

    @Override
    public void fail(int statusCode) {
        throw new NotImplementedException();
    }

    @Override
    public void fail(Throwable throwable) {
        throw new NotImplementedException();
    }

    @Override
    public void fail(int statusCode, Throwable throwable) {
        throw new NotImplementedException();
    }

    @Override
    public RoutingContext put(String key, Object obj) {
        map.put(key, obj);
        return this;
    }

    @Override
    public <T> @Nullable T get(String key) {
        return (T) map.get(key);
    }

    @Override
    public <T> T get(String key, T defaultValue) {
        return (T) map.getOrDefault(key, defaultValue);
    }

    @Override
    public <T> @Nullable T remove(String key) {
        return (T) map.remove(key);
    }

    @Override
    public <T> Map<String, T> data() {
        throw new NotImplementedException();
    }

    @Override
    public Vertx vertx() {
        throw new NotImplementedException();
    }

    @Override
    public @Nullable String mountPoint() {
        throw new NotImplementedException();
    }

    @Override
    public @Nullable Route currentRoute() {
        throw new NotImplementedException();
    }

    @Override
    public String normalizedPath() {
        throw new NotImplementedException();
    }

    @Override
    public RequestBody body() {
        throw new NotImplementedException();
    }

    @Override
    public List<FileUpload> fileUploads() {
        throw new NotImplementedException();
    }

    @Override
    public void cancelAndCleanupFileUploads() {
        throw new NotImplementedException();
    }

    @Override
    public @Nullable Session session() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isSessionAccessed() {
        throw new NotImplementedException();
    }

    @Override
    public UserContext userContext() {
        throw new NotImplementedException();
    }

    @Override
    public @Nullable Throwable failure() {
        throw new NotImplementedException();
    }

    @Override
    public int statusCode() {
        throw new NotImplementedException();
    }

    @Override
    public @Nullable String getAcceptableContentType() {
        throw new NotImplementedException();
    }

    @Override
    public ParsedHeaderValues parsedHeaders() {
        throw new NotImplementedException();
    }

    @Override
    public int addHeadersEndHandler(Handler<Void> handler) {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeHeadersEndHandler(int handlerID) {
        throw new NotImplementedException();
    }

    @Override
    public int addBodyEndHandler(Handler<Void> handler) {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeBodyEndHandler(int handlerID) {
        throw new NotImplementedException();
    }

    @Override
    public int addEndHandler(Handler<AsyncResult<Void>> handler) {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeEndHandler(int handlerID) {
        throw new NotImplementedException();
    }

    @Override
    public boolean failed() {
        throw new NotImplementedException();
    }

    @Override
    public void setAcceptableContentType(@Nullable String contentType) {
        throw new NotImplementedException();
    }

    @Override
    public void reroute(HttpMethod method, String path) {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, String> pathParams() {
        return Map.of();
    }

    @Override
    public @Nullable String pathParam(String name) {
        throw new NotImplementedException();
    }

    @Override
    public MultiMap queryParams() {
        throw new NotImplementedException();
    }

    @Override
    public MultiMap queryParams(Charset encoding) {
        throw new NotImplementedException();
    }

    @Override
    public List<String> queryParam(String name) {
        return List.of();
    }
}
