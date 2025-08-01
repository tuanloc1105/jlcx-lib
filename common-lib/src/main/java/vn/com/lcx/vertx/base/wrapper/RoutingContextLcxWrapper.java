package vn.com.lcx.vertx.base.wrapper;

import com.google.gson.Gson;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.ParsedHeaderValues;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.UserContext;
import vn.com.lcx.common.config.ClassPool;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.common.utils.MyStringUtils;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class RoutingContextLcxWrapper implements RoutingContext {

    private final RoutingContext realContext;

    public RoutingContextLcxWrapper(RoutingContext realContext) {
        this.realContext = realContext;
    }

    @Override
    public HttpServerRequest request() {
        return realContext.request();
    }

    @Override
    public HttpServerResponse response() {
        return realContext.response();
    }

    @Override
    public void next() {
        realContext.next();
    }

    @Override
    public void fail(int statusCode) {
        realContext.fail(statusCode);
    }

    @Override
    public void fail(Throwable throwable) {
        realContext.fail(throwable);
    }

    @Override
    public void fail(int statusCode, Throwable throwable) {
        realContext.fail(statusCode, throwable);
    }

    @Override
    public RoutingContext put(String key, Object obj) {
        return realContext.put(key, obj);
    }

    @Override
    public <T> @Nullable T get(String key) {
        return realContext.get(key);
    }

    @Override
    public <T> T get(String key, T defaultValue) {
        return realContext.get(key, defaultValue);
    }

    @Override
    public <T> @Nullable T remove(String key) {
        return realContext.remove(key);
    }

    @Override
    public <T> Map<String, T> data() {
        return realContext.data();
    }

    @Override
    public Vertx vertx() {
        return realContext.vertx();
    }

    @Override
    public @Nullable String mountPoint() {
        return realContext.mountPoint();
    }

    @Override
    public @Nullable Route currentRoute() {
        return realContext.currentRoute();
    }

    @Override
    public String normalizedPath() {
        return realContext.normalizedPath();
    }

    @Override
    public RequestBody body() {
        final RequestBody body = realContext.body();
        final String bodyString = MyStringUtils.minifyJsonString(body.asString(CommonConstant.UTF_8_STANDARD_CHARSET));
        LogUtils.writeLog(
                this,
                LogUtils.Level.INFO,
                "Request Payload:\n{}",
                MyStringUtils.maskJsonFields(ClassPool.getInstance(Gson.class), bodyString)
        );
        if (realContext.get("startTime") == null) {
            realContext.put("startTime", Double.parseDouble(System.currentTimeMillis() + CommonConstant.EMPTY_STRING));
        }
        return body;
    }

    @Override
    public List<FileUpload> fileUploads() {
        return realContext.fileUploads();
    }

    @Override
    public void cancelAndCleanupFileUploads() {
        realContext.cancelAndCleanupFileUploads();
    }

    @Override
    public @Nullable Session session() {
        return realContext.session();
    }

    @Override
    public boolean isSessionAccessed() {
        return realContext.isSessionAccessed();
    }

    @Override
    public UserContext userContext() {
        return realContext.userContext();
    }

    @Override
    public @Nullable User user() {
        return realContext.user();
    }

    @Override
    public @Nullable Throwable failure() {
        return realContext.failure();
    }

    @Override
    public int statusCode() {
        return realContext.statusCode();
    }

    @Override
    public @Nullable String getAcceptableContentType() {
        return realContext.getAcceptableContentType();
    }

    @Override
    public void setAcceptableContentType(@Nullable String contentType) {
        realContext.setAcceptableContentType(contentType);
    }

    @Override
    public ParsedHeaderValues parsedHeaders() {
        return realContext.parsedHeaders();
    }

    @Override
    public int addHeadersEndHandler(Handler<Void> handler) {
        return realContext.addHeadersEndHandler(handler);
    }

    @Override
    public boolean removeHeadersEndHandler(int handlerID) {
        return realContext.removeHeadersEndHandler(handlerID);
    }

    @Override
    public int addBodyEndHandler(Handler<Void> handler) {
        return realContext.addBodyEndHandler(handler);
    }

    @Override
    public boolean removeBodyEndHandler(int handlerID) {
        return realContext.removeBodyEndHandler(handlerID);
    }

    @Override
    public int addEndHandler(Handler<AsyncResult<Void>> handler) {
        return realContext.addEndHandler(handler);
    }

    @Override
    public boolean removeEndHandler(int handlerID) {
        return realContext.removeEndHandler(handlerID);
    }

    @Override
    public boolean failed() {
        return realContext.failed();
    }

    @Override
    public void reroute(String path) {
        realContext.reroute(path);
    }

    @Override
    public void reroute(HttpMethod method, String path) {
        realContext.reroute(method, path);
    }

    @Override
    public List<LanguageHeader> acceptableLanguages() {
        return realContext.acceptableLanguages();
    }

    @Override
    public LanguageHeader preferredLanguage() {
        return realContext.preferredLanguage();
    }

    @Override
    public Map<String, String> pathParams() {
        return realContext.pathParams();
    }

    @Override
    public @Nullable String pathParam(String name) {
        return realContext.pathParam(name);
    }

    @Override
    public MultiMap queryParams() {
        if (realContext.get("startTime") == null) {
            realContext.put("startTime", Double.parseDouble(System.currentTimeMillis() + CommonConstant.EMPTY_STRING));
        }
        return realContext.queryParams();
    }

    @Override
    public MultiMap queryParams(Charset encoding) {
        if (realContext.get("startTime") == null) {
            realContext.put("startTime", Double.parseDouble(System.currentTimeMillis() + CommonConstant.EMPTY_STRING));
        }
        return realContext.queryParams(encoding);
    }

    @Override
    public List<String> queryParam(String name) {
        if (realContext.get("startTime") == null) {
            realContext.put("startTime", Double.parseDouble(System.currentTimeMillis() + CommonConstant.EMPTY_STRING));
        }
        return realContext.queryParam(name);
    }

    @Override
    public RoutingContext attachment(String filename) {
        return realContext.attachment(filename);
    }

    @Override
    public Future<Void> redirect(String url) {
        return realContext.redirect(url);
    }

    @Override
    public Future<Void> json(Object json) {
        return realContext.json(json);
    }

    @Override
    public boolean is(String type) {
        return realContext.is(type);
    }

    @Override
    public boolean isFresh() {
        return realContext.isFresh();
    }

    @Override
    public RoutingContext etag(String etag) {
        return realContext.etag(etag);
    }

    @Override
    public RoutingContext lastModified(Instant instant) {
        return realContext.lastModified(instant);
    }

    @Override
    public RoutingContext lastModified(String instant) {
        return realContext.lastModified(instant);
    }

    @Override
    public Future<Void> end(String chunk) {

        var apiProcessDuration = 0D;

        if (realContext.get("startTime") != null) {
            final var startTime = realContext.<Double>get("startTime");
            final var endTime = (double) System.currentTimeMillis();
            apiProcessDuration = (endTime - startTime);
        }

        LogUtils.writeLog(
                this,
                LogUtils.Level.INFO,
                "Response Payload ({}ms):\n{}",
                apiProcessDuration == 0D ? "unknown duration" : apiProcessDuration,
                MyStringUtils.maskJsonFields(ClassPool.getInstance(Gson.class), MyStringUtils.minifyJsonString(chunk))
        );
        return realContext.end(chunk);
    }

    @Override
    public Future<Void> end(Buffer buffer) {
        return realContext.end(buffer);
    }

    @Override
    public Future<Void> end() {
        return realContext.end();
    }
}
