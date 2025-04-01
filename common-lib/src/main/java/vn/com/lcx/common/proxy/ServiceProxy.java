package vn.com.lcx.common.proxy;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.ServiceClass;
import vn.com.lcx.common.annotation.Transaction;
import vn.com.lcx.common.config.ClassPool;
import vn.com.lcx.common.database.context.ConnectionContext;
import vn.com.lcx.common.database.pool.LCXDataSource;
import vn.com.lcx.common.database.pool.entry.ConnectionEntry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

@AllArgsConstructor
public class ServiceProxy<T> implements InvocationHandler {

    private final Object target;

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceType, Object target) {
        if (interfaceType == null) {
            return (T) Proxy.newProxyInstance(
                    ServiceClass.class.getClassLoader(),
                    new Class<?>[]{ServiceClass.class},
                    new ServiceProxy<T>(target)
            );
        }
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                new ServiceProxy<>(target)
        );
    }

    public static <T> T create(Object target) {
        return create(null, target);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ConnectionEntry connection = null;
        boolean openConnection = true;
        // Check if the connection can be closed
        // In case there are 2 more methods that annotated with `Transaction`
        // For example, method A calling method B that both of them annotated with `Transaction`
        // The current connection will not be close after the method B was executed, but will happen after method A finished
        boolean ableToCloseConnection = false;
        String connectionName = "";
        Method implementMethod = Arrays.stream(target.getClass().getMethods())
                .filter(
                        m -> {
                            if (!m.getName().equals(method.getName())) {
                                return false;
                            }
                            if (args == null) {
                                return true;
                            }
                            if (m.getParameterCount() != args.length) {
                                return false;
                            }
                            for (int i = 0; i < args.length; i++) {
                                if (!m.getParameterTypes()[i].isAssignableFrom(args[i].getClass())) {
                                    return false;
                                }
                            }
                            return true;
                        }
                )
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find valid method"));
        if (implementMethod.getAnnotation(Transaction.class) != null) {
            Transaction transaction = implementMethod.getAnnotation(Transaction.class);
            openConnection = !transaction.readOnly();
            LCXDataSource dataSource;
            if (StringUtils.isNotBlank(transaction.connectionInstanceName())) {
                connectionName = transaction.connectionInstanceName();
                dataSource = ClassPool.getInstance(transaction.connectionInstanceName(), LCXDataSource.class);
            } else {
                dataSource = ClassPool.getInstance(LCXDataSource.class);
            }
            if (dataSource == null) {
                throw new Exception("Cannot find datasource");
            }
            if (ConnectionContext.get(connectionName) == null) {
                ableToCloseConnection = true;
                connection = dataSource.getConnection();
                ConnectionContext.set(connectionName, connection);
            } else {
                connection = ConnectionContext.get(connectionName);
            }
        }

        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            if (openConnection && ableToCloseConnection && connection != null) {
                connection.rollback();
            }
            Throwable targetException = e.getCause();
            if (targetException instanceof RuntimeException || targetException instanceof Error) {
                throw targetException;
            }
            throw new RuntimeException("Exception in method " + method.getName(), targetException);
        } catch (Exception e) {
            if (openConnection && ableToCloseConnection && connection != null) {
                connection.rollback();
            }
            throw new RuntimeException("Unexpected error invoking method " + method.getName(), e);
        } finally {
            if (ableToCloseConnection && connection != null) {
                connection.close();
                ConnectionContext.clear(connectionName);
            }
        }
    }

}
