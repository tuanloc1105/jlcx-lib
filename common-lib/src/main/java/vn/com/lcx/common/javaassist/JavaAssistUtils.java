package vn.com.lcx.common.javaassist;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

public class JavaAssistUtils {

    public <T> void modifyMethodBody(T obj, String methodName, VoidMethodModifier modifier, boolean callingOriginalMethodBody) throws Exception {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(obj.getClass());
        Class<?> proxyClass = factory.createClass();
        @SuppressWarnings("unchecked") T proxyInstance = (T) proxyClass.getDeclaredConstructor().newInstance();
        ((ProxyObject) proxyInstance).setHandler((self, method, proceed, args1) -> {
            if (methodName.equals(method.getName())) {
                Object result = null;
                if (callingOriginalMethodBody) {
                    result = proceed.invoke(obj, args1); // Call the original instance method
                }
                modifier.modifyMethodBody();
                return result;
            }
            return method.invoke(obj, args1); // Default behavior for other methods
        });
    }

    public interface VoidMethodModifier {
        void modifyMethodBody();
    }

    // public interface OutputMethodModifier<T> {
    //     T modifyMethodBody();
    // }

}
