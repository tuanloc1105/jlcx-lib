package vn.com.lcx.jpa.utils;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import vn.com.lcx.common.database.pageable.Pageable;
import vn.com.lcx.common.database.pageable.PageableImpl;
import vn.com.lcx.jpa.functional.BatchCallback;
import vn.com.lcx.jpa.functional.ResultBatchCallback;
import vn.com.lcx.jpa.functional.RowMapper;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HibernateUtils {

    private HibernateUtils() {
    }

    public static List<Map<String, Object>> queryToMap(Session session, String sql, Map<String, Object> params) {

        @SuppressWarnings("SqlSourceToSinkFlow") var query = session.createNativeQuery(sql, Object[].class);

        return getMaps(params, query);
    }

    public static List<Map<String, Object>> queryToMap(Session session, String sql, Map<String, Object> params, Pageable pageable) {

        var pageimpl = (PageableImpl) pageable;

        @SuppressWarnings("SqlSourceToSinkFlow") var query = session.createNativeQuery(sql, Object[].class);

        query.setFirstResult(pageimpl.getOffset());
        query.setMaxResults(pageimpl.getPageSize());

        return getMaps(params, query);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    public static void streamQuery(
            Session session,
            String sql,
            Map<String, Object> params,
            int batchSize,
            BatchCallback callback
    ) {

        session.doWork(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    sql,
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY
            )) {

                // Set fetch size for streaming
                ps.setFetchSize(batchSize);

                // Bind parameters
                if (params != null) {
                    int index = 1;
                    for (Map.Entry<String, Object> e : params.entrySet()) {
                        ps.setObject(index++, e.getValue());
                    }
                }

                try (ResultSet rs = ps.executeQuery()) {

                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();

                    List<Map<String, String>> batch = new ArrayList<>(batchSize);

                    while (rs.next()) {
                        Map<String, String> row = new LinkedHashMap<>();

                        for (int i = 1; i <= columnCount; i++) {
                            String alias = meta.getColumnLabel(i);
                            Object value = rs.getObject(i);
                            row.put(alias, convertToString(value));
                        }

                        batch.add(row);

                        if (batch.size() == batchSize) {
                            callback.handle(batch);
                            batch.clear();
                        }
                    }

                    // Last incomplete batch
                    if (!batch.isEmpty()) {
                        callback.handle(batch);
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException("Error streaming query", e);
            }
        });
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    public static <T> void streamQueryWithMapper(
            Session session,
            String sql,
            Map<String, Object> params,
            int batchSize,
            RowMapper<T> mapper,
            ResultBatchCallback<T> callback
    ) {
        session.doWork(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    sql,
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY
            )) {
                ps.setFetchSize(batchSize);

                // Bind parameters
                if (params != null) {
                    int i = 1;
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        ps.setObject(i++, entry.getValue());
                    }
                }

                try (ResultSet rs = ps.executeQuery()) {

                    List<T> batch = new ArrayList<>(batchSize);

                    while (rs.next()) {

                        // Let user map ResultSet → T
                        T item = mapper.map(rs);
                        batch.add(item);

                        // When enough rows collected → callback
                        if (batch.size() == batchSize) {
                            callback.handle(batch);
                            batch.clear();
                        }
                    }

                    // Last partial batch
                    if (!batch.isEmpty()) {
                        callback.handle(batch);
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException("Error while streaming with custom mapper", e);
            }
        });
    }

    private static List<Map<String, Object>> getMaps(Map<String, Object> params, NativeQuery<Object[]> query) {
        // Bind parameters
        if (params != null) {
            params.forEach(query::setParameter);
        }

        // Apply transformer
        NativeQuery<?> nativeQuery = query.unwrap(NativeQuery.class);

        nativeQuery.setTupleTransformer((tuple, aliases) -> {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < aliases.length; i++) {
                map.put(aliases[i], convertToString(tuple[i]));
            }
            return map;
        });

        // Hibernate returns List<Object> but each Object is actually Map<String, Object>
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) nativeQuery.getResultList();

        return result;
    }

    private static String convertToString(Object value) {
        // Null value
        if (value == null) {
            return null;
        }

        // BigDecimal → plain string
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }

        // java.sql.Timestamp → ISO-8601 string
        if (value instanceof java.sql.Timestamp) {
            java.sql.Timestamp ts = (java.sql.Timestamp) value;
            return ts.toInstant().toString();
        }

        // java.sql.Date → yyyy-MM-dd
        if (value instanceof java.sql.Date) {
            java.sql.Date d = (java.sql.Date) value;
            return d.toLocalDate().toString();
        }

        // java.util.Date → ISO-8601 string
        if (value instanceof java.util.Date) {
            java.util.Date d = (java.util.Date) value;
            return d.toInstant().toString();
        }

        // Boolean → "true" / "false"
        if (value instanceof Boolean) {
            return value.toString();
        }

        // Enum → enum name
        if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        }

        // BLOB → convert to UTF-8 string
        if (value instanceof java.sql.Blob) {
            try {
                java.sql.Blob blob = (java.sql.Blob) value;
                return new String(blob.getBytes(1, (int) blob.length()), java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                return null;
            }
        }

        // CLOB → convert character data to string
        if (value instanceof java.sql.Clob) {
            try {
                java.sql.Clob clob = (java.sql.Clob) value;
                return clob.getSubString(1, (int) clob.length());
            } catch (Exception e) {
                return null;
            }
        }

        // Fallback → use toString() for all other types
        return value.toString();
    }

}
