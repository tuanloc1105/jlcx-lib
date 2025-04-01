package vn.com.lcx.common.proxy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.com.lcx.common.annotation.Repository;
import vn.com.lcx.common.config.ClassPool;
import vn.com.lcx.common.database.context.ConnectionContext;
import vn.com.lcx.common.database.pool.LCXDataSource;
import vn.com.lcx.common.database.pool.entry.ConnectionEntry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RepositoryProxyHandler<T> implements InvocationHandler {
    private final Logger log = LoggerFactory.getLogger("proxy");
    private final Object target;
    private final Class<T> repositoryInterface;

    public RepositoryProxyHandler(Object target, Class<T> repositoryInterface) {
        this.target = target;
        this.repositoryInterface = repositoryInterface;
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> interfaceType, Object target) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                new RepositoryProxyHandler<T>(target, interfaceType)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Handle Object methods
        switch (method.getName()) {
            case "toString":
                return "Proxy for " + repositoryInterface.getName();
            case "hashCode":
                return System.identityHashCode(proxy);
            case "equals":
                return proxy == args[0];
        }
        Repository repositoryAnnotation = repositoryInterface.getAnnotation(Repository.class);
        ConnectionEntry connection = ConnectionContext.get(repositoryAnnotation.connectionInstanceName());
        boolean closeAfterExecuted = false;
        if (connection == null) {
            closeAfterExecuted = true;
            LCXDataSource dataSource;
            if (StringUtils.isNotBlank(repositoryAnnotation.connectionInstanceName())) {
                dataSource = ClassPool.getInstance(repositoryAnnotation.connectionInstanceName(), LCXDataSource.class);
            } else {
                dataSource = ClassPool.getInstance(LCXDataSource.class);
            }
            connection = dataSource.getConnection();
        }
        ConnectionContext.set(connection);

        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getCause();
            if (targetException instanceof RuntimeException || targetException instanceof Error) {
                throw targetException;
            }
            throw new RuntimeException("Exception in method " + method.getName(), targetException);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error invoking method " + method.getName(), e);
        } finally {
            if (closeAfterExecuted) {
                connection.close();
            }
            ConnectionContext.clear();
        }
    }

}
