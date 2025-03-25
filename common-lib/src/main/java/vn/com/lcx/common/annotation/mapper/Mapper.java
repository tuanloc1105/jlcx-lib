package vn.com.lcx.common.annotation.mapper;

import lombok.var;
import vn.com.lcx.common.scanner.PackageScanner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class Mapper {

    private static final ConcurrentHashMap<String, Object> MAPPER_CLASS_POOL = new ConcurrentHashMap<>();

    public static <T> T getInstance(Class<T> mapperInterfaceClass) {

        var classFromPool = MAPPER_CLASS_POOL.get(mapperInterfaceClass.getSimpleName());

        if (classFromPool != null) {
            return mapperInterfaceClass.cast(classFromPool);
        }

        synchronized (MAPPER_CLASS_POOL) {
            classFromPool = MAPPER_CLASS_POOL.get(mapperInterfaceClass.getSimpleName());

            if (classFromPool != null) {
                return mapperInterfaceClass.cast(classFromPool);
            }
            try {
                List<Class<?>> listOfClassInPackage = PackageScanner.findClasses(mapperInterfaceClass.getPackage().getName());
                T implementClass = null;
                for (Class<?> aClass : listOfClassInPackage) {
                    if (aClass.isInterface()) {
                        continue;
                    } else {
                        // if (aClass.getSimpleName().contains(mapperInterfaceClass.getSimpleName())) {
                        //     implementClass = mapperInterfaceClass.cast(aClass.getDeclaredConstructor().newInstance());
                        //     break;
                        // }
                        if (aClass.getSimpleName().equals(mapperInterfaceClass.getSimpleName() + "Impl")) {
                            implementClass = mapperInterfaceClass.cast(aClass.getDeclaredConstructor().newInstance());
                            break;
                        }
                    }
                }
                if (implementClass == null) {
                    throw new ClassNotFoundException(
                            String.format(
                                    "Cannot find implement class of class %s",
                                    mapperInterfaceClass.getName()
                            )
                    );
                }
                MAPPER_CLASS_POOL.put(mapperInterfaceClass.getSimpleName(), implementClass);
                return implementClass;
            } catch (IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
