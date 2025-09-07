package vn.com.lcx.vertx.base.wrapper;

import com.google.gson.Gson;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.HostAndPort;
import io.vertx.ext.web.RoutingContext;
import vn.com.lcx.common.config.ClassPool;
import vn.com.lcx.common.utils.JsonMaskingUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.common.utils.MyStringUtils;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class HttpServerResponseLcxWrapper implements HttpServerResponse {

    private final HttpServerResponse realResponse;
    private final RoutingContext context;

    protected HttpServerResponseLcxWrapper(HttpServerResponse realResponse, RoutingContext context) {
        this.realResponse = realResponse;
        this.context = context;
    }

    @Override
    public HttpServerResponse exceptionHandler(Handler<Throwable> handler) {
        realResponse.exceptionHandler(handler);
        return this;
    }

    @Override
    public Future<Void> write(Buffer data) {
        return realResponse.write(data);
    }

    @Override
    public HttpServerResponse setWriteQueueMaxSize(int maxSize) {
        realResponse.setWriteQueueMaxSize(maxSize);
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        return realResponse.writeQueueFull();
    }

    @Override
    public HttpServerResponse drainHandler(Handler<Void> handler) {
        realResponse.drainHandler(handler);
        return this;
    }

    @Override
    public int getStatusCode() {
        return realResponse.getStatusCode();
    }

    @Override
    public HttpServerResponse setStatusCode(int statusCode) {
        realResponse.setStatusCode(statusCode);
        return this;
    }

    @Override
    public String getStatusMessage() {
        return realResponse.getStatusMessage();
    }

    @Override
    public HttpServerResponse setStatusMessage(String statusMessage) {
        realResponse.setStatusMessage(statusMessage);
        return this;
    }

    @Override
    public HttpServerResponse setChunked(boolean chunked) {
        realResponse.setChunked(chunked);
        return this;
    }

    @Override
    public boolean isChunked() {
        return realResponse.isChunked();
    }

    @Override
    public MultiMap headers() {
        return realResponse.headers();
    }

    @Override
    public HttpServerResponse putHeader(String name, String value) {
        realResponse.putHeader(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(CharSequence name, CharSequence value) {
        realResponse.putHeader(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(String name, Iterable<String> values) {
        realResponse.putHeader(name, values);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(CharSequence name, Iterable<CharSequence> values) {
        realResponse.putHeader(name, values);
        return this;
    }

    @Override
    public MultiMap trailers() {
        return realResponse.trailers();
    }

    @Override
    public HttpServerResponse putTrailer(String name, String value) {
        realResponse.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putTrailer(CharSequence name, CharSequence value) {
        realResponse.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putTrailer(String name, Iterable<String> values) {
        realResponse.putTrailer(name, values);
        return this;
    }

    @Override
    public HttpServerResponse putTrailer(CharSequence name, Iterable<CharSequence> value) {
        realResponse.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpServerResponse closeHandler(@Nullable Handler<Void> handler) {
        realResponse.closeHandler(handler);
        return this;
    }

    @Override
    public HttpServerResponse endHandler(@Nullable Handler<Void> handler) {
        realResponse.closeHandler(handler);
        return this;
    }

    @Override
    public Future<Void> writeHead() {
        return realResponse.writeHead();
    }

    @Override
    public Future<Void> write(String chunk, String enc) {
        return realResponse.write(chunk, enc);
    }

    @Override
    public Future<Void> write(String chunk) {
        return realResponse.write(chunk);
    }

    @Override
    public Future<Void> writeContinue() {
        return realResponse.writeContinue();
    }

    @Override
    public Future<Void> writeEarlyHints(MultiMap headers) {
        return realResponse.writeEarlyHints(headers);
    }

    @Override
    public Future<Void> end(String chunk) {
        responseLogging(chunk);
        return realResponse.end(chunk);
    }

    @Override
    public Future<Void> end(String chunk, String enc) {
        responseLogging(chunk);
        return realResponse.end(chunk, enc);
    }

    @Override
    public Future<Void> end(Buffer chunk) {
        responseLogging(chunk);
        return realResponse.end(chunk);
    }

    @Override
    public Future<Void> end() {
        return realResponse.end();
    }

    @Override
    public Future<Void> sendFile(String filename, long offset, long length) {
        return realResponse.sendFile(filename, offset, length);
    }

    @Override
    public Future<Void> sendFile(FileChannel channel, long offset, long length) {
        return realResponse.sendFile(channel, offset, length);
    }

    @Override
    public Future<Void> sendFile(RandomAccessFile file, long offset, long length) {
        return realResponse.sendFile(file, offset, length);
    }

    @Override
    public boolean ended() {
        return realResponse.ended();
    }

    @Override
    public boolean closed() {
        return realResponse.closed();
    }

    @Override
    public boolean headWritten() {
        return realResponse.headWritten();
    }

    @Override
    public HttpServerResponse headersEndHandler(@Nullable Handler<Void> handler) {
        realResponse.headersEndHandler(handler);
        return this;
    }

    @Override
    public HttpServerResponse bodyEndHandler(@Nullable Handler<Void> handler) {
        realResponse.bodyEndHandler(handler);
        return this;
    }

    @Override
    public long bytesWritten() {
        return realResponse.bytesWritten();
    }

    @Override
    public int streamId() {
        return realResponse.streamId();
    }

    @Override
    public Future<HttpServerResponse> push(HttpMethod method, HostAndPort authority, String path, MultiMap headers) {
        return realResponse.push(method, authority, path, headers);
    }

    @Override
    public Future<Void> reset(long code) {
        return realResponse.reset(code);
    }

    @Override
    public Future<Void> writeCustomFrame(int type, int flags, Buffer payload) {
        return realResponse.writeCustomFrame(type, flags, payload);
    }

    @Override
    public HttpServerResponse addCookie(Cookie cookie) {
        return realResponse.addCookie(cookie);
    }

    @Override
    public @Nullable Cookie removeCookie(String name, boolean invalidate) {
        return realResponse.removeCookie(name, invalidate);
    }

    @Override
    public Set<Cookie> removeCookies(String name, boolean invalidate) {
        return realResponse.removeCookies(name, invalidate);
    }

    @Override
    public @Nullable Cookie removeCookie(String name, String domain, String path, boolean invalidate) {
        return realResponse.removeCookie(name, domain, path, invalidate);
    }

    private void responseLogging(String chunk) {
        var apiProcessDuration = 0D;

        if (context.get("startTime") != null) {
            final var startTime = context.<Double>get("startTime");
            final var endTime = (double) System.currentTimeMillis();
            apiProcessDuration = (endTime - startTime);
        }
        final var headerLogMsg = new ArrayList<String>();
        for (Map.Entry<String, String> header : headers()) {
            headerLogMsg.add(
                    String.format(
                            "        - Name: %s\n          Value: %s",
                            header.getKey(),
                            header.getValue()
                    )
            );
        }
        LogUtils.writeLog(
                context,
                LogUtils.Level.INFO,
                "=> Header:\n" +
                        "{}\n" +
                        "=> Process duration: {}ms\n" +
                        "=> Response Payload:\n" +
                        "{}",
                String.join("\n", headerLogMsg),
                apiProcessDuration == 0D ? "unknown duration" : apiProcessDuration,
                MyStringUtils.stringIsJsonFormat(chunk) ?
                        MyStringUtils.minifyJsonString(JsonMaskingUtils.maskJsonFields(ClassPool.getInstance(Gson.class), chunk)) :
                        (chunk.length() > 10000 ? chunk.substring(0, 50) +
                                "..." + chunk.substring(chunk.length() - 50) : chunk)
        );
    }

    private void responseLogging(Buffer chunk) {
        var apiProcessDuration = 0D;

        if (context.get("startTime") != null) {
            final var startTime = context.<Double>get("startTime");
            final var endTime = (double) System.currentTimeMillis();
            apiProcessDuration = (endTime - startTime);
        }
        final var headerLogMsg = new ArrayList<String>();
        for (Map.Entry<String, String> header : headers()) {
            headerLogMsg.add(
                    String.format(
                            "        - Name: %s\n          Value: %s",
                            header.getKey(),
                            header.getValue()
                    )
            );
        }
        LogUtils.writeLog(
                context,
                LogUtils.Level.INFO,
                "=> Header:\n" +
                        "{}\n" +
                        "=> Process duration: {}ms\n" +
                        "=> Response Chunk Size: {} bytes",
                String.join("\n", headerLogMsg),
                apiProcessDuration == 0D ? "unknown duration" : apiProcessDuration,
                chunk.length()
        );
    }

}
