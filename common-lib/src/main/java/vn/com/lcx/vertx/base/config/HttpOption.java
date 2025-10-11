package vn.com.lcx.vertx.base.config;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.KeyCertOptions;

public final class HttpOption {

    private HttpOption() {
    }

    /**
     * HTTP/2 với TLS (h2)
     *
     * @param port               port
     * @param pathToKeyStoreFile Đường dẫn đến file KeyStore (.jks)
     * @param keyStorePassword   Mật khẩu KeyStore
     * @return HttpServerOptions
     */
    public static HttpServerOptions configureHttp2TlsH2(Integer port, final String pathToKeyStoreFile, final String keyStorePassword) {
        KeyCertOptions option = new JksOptions()
                .setPath(pathToKeyStoreFile)
                .setPassword(keyStorePassword);
        return new HttpServerOptions()
                .setSsl(true)
                .setKeyCertOptions(option)
                .setUseAlpn(true) // Cần bật ALPN để hỗ trợ HTTP/2
                .setPort(port)
                // .setLogActivity(true)
                .setIdleTimeout(10)
                .setCompressionSupported(true) // Bật gzip/brotli compression
                .setHandle100ContinueAutomatically(true)
                .setMaxInitialLineLength(4096)
                .setMaxHeaderSize(8192)
                .setMaxChunkSize(8192);
    }

    /**
     * HTTP/2 không cần TLS (h2c)
     *
     * @param port - port
     * @return HttpServerOptions
     */
    public static HttpServerOptions configureHttp2H2C(Integer port) {
        return new HttpServerOptions()
                .setPort(port != null && port > 0 ? port : 8443)
                // .setLogActivity(true)
                .setCompressionSupported(true)
                .setHandle100ContinueAutomatically(true)
                .setMaxInitialLineLength(4096)
                .setMaxHeaderSize(8192)
                .setMaxChunkSize(8192);
    }
}
