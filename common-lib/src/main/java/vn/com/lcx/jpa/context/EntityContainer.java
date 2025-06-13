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

    /**
     * Maps a list of entity classes to a SessionFactory, and stores the mapping in memory.
     * This method is used to register JPA entities with the JPA context.
     *
     * @param entities       a list of entity classes to be mapped to {@code sessionFactory}
     * @param sessionFactory the SessionFactory to be associated with the given entities
     * @throws IllegalEntityClassException if the given list of entities contains an entity class that is not annotated with {@link javax.persistence.Entity}
     */
    public static void addEntityManager(ArrayList<String> entities, SessionFactory sessionFactory) {
        if (CollectionUtils.isEmpty(entities) || sessionFactory == null) {
            LogUtils.writeLog(LogUtils.Level.WARN, "`entities` or `sessionFactory` is empty");
            return;
        }
        UUID uuid = UUID.randomUUID();
        entities.forEach(entity -> entityContainer.put(entity, uuid));
        factoryContainer.put(uuid, sessionFactory);
    }

    /**
     * Gets the SessionFactory that is associated with the given entity class.
     *
     * @param entityClass the entity class for which to retrieve the SessionFactory
     * @return the SessionFactory that is associated with the given entity class
     * @throws IllegalEntityClassException if the given entity class does not exist in the container
     */
    public static SessionFactory getEntityManager(Class<?> entityClass) {
        return factoryContainer.get(getEntityContainerUUID((entityClass)));
    }

    /**
     * Retrieves the UUID associated with the given entity class.
     *
     * @param entityClass the entity class for which to retrieve the UUID
     * @return the UUID associated with the given entity class
     * @throws IllegalEntityClassException if the given entity class does not exist in the container
     */
    public static UUID getEntityContainerUUID(Class<?> entityClass) {
        var uuid = entityContainer.get(entityClass.getName());
        if (uuid == null) {
            throw new IllegalEntityClassException("Class " + entityClass.getName() + " does not exist in container");
        }
        return uuid;
    }

}
