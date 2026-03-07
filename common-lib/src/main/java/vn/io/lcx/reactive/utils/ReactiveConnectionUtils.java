package vn.io.lcx.reactive.utils;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import vn.io.lcx.jpa.exception.CodeGenError;
import vn.io.lcx.reactive.context.EntityMappingContainer;

import java.util.ArrayList;
import java.util.List;

public final class ReactiveConnectionUtils {

    private ReactiveConnectionUtils() {
    }

    public static <U> List<U> mappingResult(Class<U> outputClazz, RowSet<Row> rowSet) {
        final List<U> result = new ArrayList<>();
        for (Row row : rowSet) {
            result.add(EntityMappingContainer.<U>getMapping(outputClazz.getName()).vertxRowMapping(row));
        }
        return result;
    }

    public static String extractDatabasePlaceholder(SqlConnection connection) {
        String databaseName = connection.databaseMetadata().productName();
        return switch (databaseName) {
            case "PostgreSQL" -> "$";
            case "Microsoft SQL Server" -> "@p";
            case "MySQL", "MariaDB", "Oracle" -> "?";
            default -> throw new CodeGenError("Unsupported database type");
        };
    }

}
