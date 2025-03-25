package vn.com.lcx.common.annotation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MyInjector {

    private static final MyInjector INSTANCE = new MyInjector();

    private final Map<Class<?>, Object> classesMapInstance = new HashMap<>();

    private MyInjector() {
    }

    public static MyInjector getInstance() {
        return INSTANCE;
    }

    public void register(Class<?> clazz) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        classesMapInstance.put(clazz, instance);

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(MyAutowired.class)) {
                Class<?> fieldType = field.getType();
                Object dependency = classesMapInstance.get(fieldType);

                if (dependency == null) {
                    dependency = fieldType.getDeclaredConstructor().newInstance();
                    classesMapInstance.put(fieldType, dependency);
                }

                field.setAccessible(true);
                field.set(instance, dependency);
                field.setAccessible(false);
            }
        }
    }

    public <T> T getInstance(Class<T> clazz) {
        return clazz.cast(classesMapInstance.get(clazz));
    }
}
