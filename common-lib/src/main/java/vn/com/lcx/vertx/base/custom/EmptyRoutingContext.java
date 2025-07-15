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
        return null;
    }

    @Override
    public HttpServerResponse response() {
        return null;
    }

    @Override
    public void next() {

    }

    @Override
    public void fail(int statusCode) {

    }

    @Override
    public void fail(Throwable throwable) {

    }

    @Override
    public void fail(int statusCode, Throwable throwable) {

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
        return Map.of();
    }

    @Override
    public Vertx vertx() {
        return null;
    }

    @Override
    public @Nullable String mountPoint() {
        return "";
    }

    @Override
    public @Nullable Route currentRoute() {
        return null;
    }

    @Override
    public String normalizedPath() {
        return "";
    }

    @Override
    public RequestBody body() {
        return null;
    }

    @Override
    public List<FileUpload> fileUploads() {
        return List.of();
    }

    @Override
    public void cancelAndCleanupFileUploads() {

    }

    @Override
    public @Nullable Session session() {
        return null;
    }

    @Override
    public boolean isSessionAccessed() {
        return false;
    }

    @Override
    public UserContext userContext() {
        return null;
    }

    @Override
    public @Nullable Throwable failure() {
        return null;
    }

    @Override
    public int statusCode() {
        return 0;
    }

    @Override
    public @Nullable String getAcceptableContentType() {
        return "";
    }

    @Override
    public ParsedHeaderValues parsedHeaders() {
        return null;
    }

    @Override
    public int addHeadersEndHandler(Handler<Void> handler) {
        return 0;
    }

    @Override
    public boolean removeHeadersEndHandler(int handlerID) {
        return false;
    }

    @Override
    public int addBodyEndHandler(Handler<Void> handler) {
        return 0;
    }

    @Override
    public boolean removeBodyEndHandler(int handlerID) {
        return false;
    }

    @Override
    public int addEndHandler(Handler<AsyncResult<Void>> handler) {
        return 0;
    }

    @Override
    public boolean removeEndHandler(int handlerID) {
        return false;
    }

    @Override
    public boolean failed() {
        return false;
    }

    @Override
    public void setAcceptableContentType(@Nullable String contentType) {

    }

    @Override
    public void reroute(HttpMethod method, String path) {

    }

    @Override
    public Map<String, String> pathParams() {
        return Map.of();
    }

    @Override
    public @Nullable String pathParam(String name) {
        return "";
    }

    @Override
    public MultiMap queryParams() {
        return null;
    }

    @Override
    public MultiMap queryParams(Charset encoding) {
        return null;
    }

    @Override
    public List<String> queryParam(String name) {
        return List.of();
    }
}
