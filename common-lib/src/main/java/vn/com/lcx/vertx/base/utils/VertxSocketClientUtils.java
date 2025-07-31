package vn.com.lcx.vertx.base.utils;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.web.RoutingContext;
import vn.com.lcx.common.utils.LogUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for sending and receiving messages over TCP sockets using Vert.x NetClient.
 * <p>
 * This class provides a simple API to send a message to a TCP server and asynchronously receive the response.
 * The connection is closed automatically after the first response is received or on timeout/error.
 * </p>
 *
 * <pre>
 * Usage example:
 *   Vertx vertx = Vertx.vertx();
 *   VertxSocketClientUtils clientUtils = new VertxSocketClientUtils(vertx);
 *   clientUtils.sendAndReceive("localhost", 1234, "Hello")
 *     .onFailure(Throwable::printStackTrace);
 * </pre>
 */
public class VertxSocketClientUtils {

    private final Vertx vertx;
    private final NetClient client;
    private final long timeoutMillis;
    private final Charset encoding;
    // private static final Logger logger = LoggerFactory.getLogger(VertxSocketClientUtils.class);

    /**
     * Constructs a VertxSocketClientUtils with the given Vertx instance, default timeout 5s, UTF-8 encoding.
     *
     * @param vertx the Vertx instance to use for creating the NetClient
     */
    public VertxSocketClientUtils(Vertx vertx) {
        this(vertx, 5000, StandardCharsets.UTF_8);
    }

    /**
     * Constructs a VertxSocketClientUtils with custom timeout and encoding.
     *
     * @param vertx         the Vertx instance
     * @param timeoutMillis timeout in milliseconds for connect + receive
     * @param encoding      charset for encoding/decoding messages
     */
    public VertxSocketClientUtils(Vertx vertx, long timeoutMillis, Charset encoding) {
        this.vertx = vertx;
        this.client = vertx.createNetClient();
        this.timeoutMillis = timeoutMillis;
        this.encoding = encoding;
    }

    /**
     * Sends a message to the specified TCP server and asynchronously receives the response.
     * The connection is closed after the first response is received or on timeout/error.
     *
     * @param context      The routing context for logging purposes. Used to associate logs with the current request
     * @param socketHost   the host of the TCP server
     * @param socketPort   the port of the TCP server
     * @param inputMessage the message to send
     * @return a Future that will be completed with the response from the server,
     * or failed if the connection or communication fails
     */
    public Future<String> sendAndReceive(RoutingContext context, String socketHost, int socketPort, String inputMessage) {
        Promise<String> promise = Promise.promise();
        final long startTime = System.currentTimeMillis();
        final var connectFuture = client.connect(socketPort, socketHost);
        final var logMessage = new StringBuilder()
                .append("\n- Destination: ")
                .append(socketHost)
                .append(":")
                .append(socketPort)
                .append("\n- Input message: ")
                .append(inputMessage);

        // Timeout handler
        final long timerId = vertx.setTimer(timeoutMillis, tid -> {
            if (!promise.future().isComplete()) {
                LogUtils.writeLog(context, LogUtils.Level.INFO, "Socket operation timed out after {} ms", timeoutMillis);
                promise.tryFail(new RuntimeException("Socket operation timed out after " + timeoutMillis + " ms"));
            }
        });

        connectFuture.onSuccess(socket -> {
            try {
                socket.write(inputMessage);
                socket.handler(buffer -> handleBuffer(context, socket, buffer, promise, startTime, logMessage));
                socket.exceptionHandler(ex -> {
                    LogUtils.writeLog(context, "Socket exception: ", ex);
                    promise.tryFail(ex);
                    closeSocket(socket);
                });
                socket.closeHandler(v -> {
                    if (!promise.future().isComplete()) {
                        LogUtils.writeLog(context, LogUtils.Level.ERROR, "Socket closed before response received");
                        promise.tryFail(new RuntimeException("Socket closed before response received"));
                    }
                });
            } catch (Exception ex) {
                LogUtils.writeLog(context, "Error during socket operation: ", ex);
                promise.tryFail(ex);
                closeSocket(socket);
            }
        }).onFailure(ex -> {
            LogUtils.writeLog(context, "Failed to connect: ", ex);
            promise.tryFail(ex);
        });

        // Ensure timer is cancelled when promise completes
        promise.future().onComplete(ar -> vertx.cancelTimer(timerId));
        return promise.future();
    }

    private void handleBuffer(RoutingContext context, NetSocket socket, Buffer buffer, Promise<String> promise, long startTime, StringBuilder logMessage) {
        if (!promise.future().isComplete()) {
            String response = buffer.toString(encoding);
            logMessage.append("\n- Output message: ")
                    .append(response);
            final var endingTime = (double) System.currentTimeMillis();
            final var duration = endingTime - startTime;
            logMessage.append("\n- Duration: ").append(duration).append(" ms");
            LogUtils.writeLog(context, LogUtils.Level.INFO, logMessage.toString());
            promise.tryComplete(response);
            closeSocket(socket);
        }
    }

    private void closeSocket(NetSocket socket) {
        socket.close();
    }
}
