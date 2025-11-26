package vn.com.lcx.jpa.utils;

import org.hibernate.Session;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HibernateUtils {

    private HibernateUtils() {
    }

    public static List<Map<String, Object>> queryToMap(Session session, String sql, Map<String, Object> params) {

        @SuppressWarnings("SqlSourceToSinkFlow") var query = session.createNativeQuery(sql, Object[].class);

        // Bind parameters
        if (params != null) {
            params.forEach(query::setParameter);
        }

        // Apply transformer
        org.hibernate.query.NativeQuery<?> nativeQuery =
                query.unwrap(org.hibernate.query.NativeQuery.class);

        nativeQuery.setTupleTransformer((tuple, aliases) -> {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < aliases.length; i++) {
                map.put(aliases[i], tuple[i]);
            }
            return map;
        });

        // Hibernate returns List<Object> but each Object is actually Map<String, Object>
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) nativeQuery.getResultList();

        return result;
    }

}
