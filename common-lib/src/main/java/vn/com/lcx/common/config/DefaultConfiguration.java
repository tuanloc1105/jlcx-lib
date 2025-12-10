package vn.com.lcx.common.config;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Instance;
import vn.com.lcx.common.utils.HttpUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.common.utils.SocketUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.AutoCloseable;

@Component
public class DefaultConfiguration {

    @Instance
    public Gson defaultGson() {
        return BuildGson.getGson();
    }

    @Instance
    public JsonMapper defaultJsonMapper() {
        return BuildObjectMapper.getJsonMapper2();
    }

    @Instance
    public XmlMapper defaultXmlMapper() {
        return BuildObjectMapper.getXMLMapper();
    }

    @Instance
    public HttpUtils defaultHttpUtils() {
        return new HttpUtils();
    }

    @Instance
    public SocketUtils defaultSocketUtils() {
        return new SocketUtils();
    }

    @Instance
    public ExecutorService defaultExecutorService() {
        final int major = Runtime.version().feature();

        final ExecutorService service =
                (major < 21)
                        ? Executors.newCachedThreadPool()
                        : createVirtualThreadExecutor();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                service.shutdownNow();
                if (major >= 21 && service instanceof AutoCloseable) {
                    // Call close() if ExecutorService implements AutoCloseable
                    ((AutoCloseable) service).close();
                }
            } catch (Exception e) {
                LogUtils.writeLog(LogUtils.Level.WARN, e.getMessage(), e);
            }
        }));

        return service;
    }

    private ExecutorService createVirtualThreadExecutor() {
        try {
            Class<?> executorsClass = Class.forName("java.util.concurrent.Executors");

            // Lấy method newVirtualThreadPerTaskExecutor()
            Method method =
                    executorsClass.getMethod("newVirtualThreadPerTaskExecutor");

            // Invoke static method → instance of ExecutorService
            return (ExecutorService) method.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new UnsupportedOperationException("Virtual threads are not supported on this JVM", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
