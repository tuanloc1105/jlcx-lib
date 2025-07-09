package vn.com.lcx.vertx.base.utils;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.core.buffer.Buffer;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

// Sử dụng SLF4J nếu có, nếu không thì dùng System.err
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

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
 *     .onSuccess(response -> System.out.println("Received: " + response))
 *     .onFailure(Throwable::printStackTrace);
 * </pre>
 */
public class VertxSocketClientUtils {

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
        this(vertx, 5000, Charset.forName("UTF-8"));
    }

    /**
     * Constructs a VertxSocketClientUtils with custom timeout and encoding.
     *
     * @param vertx the Vertx instance
     * @param timeoutMillis timeout in milliseconds for connect + receive
     * @param encoding charset for encoding/decoding messages
     */
    public VertxSocketClientUtils(Vertx vertx, long timeoutMillis, Charset encoding) {
        this.client = vertx.createNetClient();
        this.timeoutMillis = timeoutMillis;
        this.encoding = encoding;
    }

    /**
     * Sends a message to the specified TCP server and asynchronously receives the response.
     * The connection is closed after the first response is received or on timeout/error.
     *
     * @param socketHost   the host of the TCP server
     * @param socketPort   the port of the TCP server
     * @param inputMessage the message to send
     * @return a Future that will be completed with the response from the server,
     *         or failed if the connection or communication fails
     */
    public Future<String> sendAndReceive(String socketHost, int socketPort, String inputMessage) {
        Promise<String> promise = Promise.promise();
        final long startTime = System.currentTimeMillis();
        final var connectFuture = client.connect(socketPort, socketHost);
        final Vertx vertx = client.vertx();

        // Timeout handler
        final long timerId = vertx.setTimer(timeoutMillis, tid -> {
            if (!promise.future().isComplete()) {
                // logger.error("Socket operation timed out after {} ms", timeoutMillis);
                System.err.println("Socket operation timed out after " + timeoutMillis + " ms");
                promise.tryFail(new RuntimeException("Socket operation timed out after " + timeoutMillis + " ms"));
            }
        });

        connectFuture.onSuccess(socket -> {
            try {
                socket.write(inputMessage);
                socket.handler(buffer -> handleBuffer(socket, buffer, promise, timerId));
                socket.exceptionHandler(ex -> {
                    // logger.error("Socket exception: ", ex);
                    System.err.println("Socket exception: " + ex.getMessage());
                    promise.tryFail(ex);
                    closeSocket(socket);
                });
                socket.closeHandler(v -> {
                    if (!promise.future().isComplete()) {
                        // logger.error("Socket closed before response received");
                        System.err.println("Socket closed before response received");
                        promise.tryFail(new RuntimeException("Socket closed before response received"));
                    }
                });
            } catch (Exception ex) {
                // logger.error("Error during socket operation: ", ex);
                System.err.println("Error during socket operation: " + ex.getMessage());
                promise.tryFail(ex);
                closeSocket(socket);
            }
        }).onFailure(ex -> {
            // logger.error("Failed to connect to {}:{}", socketHost, socketPort, ex);
            System.err.println("Failed to connect to " + socketHost + ":" + socketPort + ", error: " + ex.getMessage());
            promise.tryFail(ex);
        });

        // Ensure timer is cancelled when promise completes
        promise.future().onComplete(ar -> vertx.cancelTimer(timerId));
        return promise.future();
    }

    private void handleBuffer(NetSocket socket, Buffer buffer, Promise<String> promise, long timerId) {
        if (!promise.future().isComplete()) {
            String response = buffer.toString(encoding);
            promise.tryComplete(response);
            closeSocket(socket);
        }
    }

    private void closeSocket(NetSocket socket) {
        if (!socket.isClosed()) {
            socket.close();
        }
    }
}
