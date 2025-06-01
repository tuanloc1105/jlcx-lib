package vn.com.lcx.jpa.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.SessionFactory;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.jpa.exception.IllegalEntityClassException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityContainer {
    private final static Map<String, UUID> entityContainer = new HashMap<>();
    private final static Map<UUID, SessionFactory> factoryContainer = new HashMap<>();

    public static void addEntityManager(ArrayList<String> entities, SessionFactory sessionFactory) {
        if (CollectionUtils.isEmpty(entities) || sessionFactory == null) {
            LogUtils.writeLog(LogUtils.Level.WARN, "`entities` or `sessionFactory` is empty");
            return;
        }
        UUID uuid = UUID.randomUUID();
        entities.forEach(entity -> entityContainer.put(entity, uuid));
        factoryContainer.put(uuid, sessionFactory);
    }

    public static SessionFactory getEntityManager(Class<?> entityClass) {
        return factoryContainer.get(getEntityContainerUUID((entityClass)));
    }

    public static UUID getEntityContainerUUID(Class<?> entityClass) {
        var uuid = entityContainer.get(entityClass.getName());
        if (uuid == null) {
            throw new IllegalEntityClassException("Class " + entityClass.getName() + " does not exist in container");
        }
        return uuid;
    }

}
