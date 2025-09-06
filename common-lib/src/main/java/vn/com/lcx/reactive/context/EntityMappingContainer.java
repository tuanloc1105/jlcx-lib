package vn.com.lcx.reactive.context;

import vn.com.lcx.common.utils.ExceptionUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.jpa.exception.IllegalEntityClassException;
import vn.com.lcx.reactive.entity.EntityMapping;
import vn.com.lcx.vertx.base.custom.EmptyRoutingContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityMappingContainer {

    private final static Map<String, UUID> entityContainer = new HashMap<>();
    private final static Map<UUID, EntityMapping<?>> mapperContainer = new HashMap<>();

    public static void addMapping(String entityClassName, EntityMapping<?> mapping) {
        UUID uuid = UUID.randomUUID();
        entityContainer.put(entityClassName, uuid);
        mapperContainer.put(uuid, mapping);
    }

    public static <T> EntityMapping<T> getMapping(String entityClassName) {
        var uuid = entityContainer.get(entityClassName);
        if (uuid == null) {
            throw new IllegalEntityClassException("Class `" + entityClassName + "` does not exist in container");
        }
        //noinspection unchecked
        return (EntityMapping<T>) mapperContainer.get(uuid);
    }

    public static void addMapping(Class<?> clazz) {
        if (EntityMapping.class.isAssignableFrom(clazz)) {
            for (Type type : clazz.getGenericInterfaces()) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) type;
                    if (pType.getRawType().getTypeName().equals(EntityMapping.class.getName())) {
                        Type[] typeArgs = pType.getActualTypeArguments();
                        if (typeArgs.length > 0) {
                            try {
                                final var object = (EntityMapping<?>) clazz.getDeclaredConstructor().newInstance();
                                addMapping(typeArgs[0].getTypeName(), object);
                            } catch (Exception e) {
                                LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.WARN, "Error adding mapping for entity {}", ExceptionUtils.getStackTrace(e));
                            }
                        }
                    }
                }
            }
        }
    }

}
