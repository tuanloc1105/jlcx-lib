package vn.com.lcx.common.database.specification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.constant.CommonConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static vn.com.lcx.common.database.utils.EntityUtils.getColumnNameFromFieldName;
import static vn.com.lcx.common.database.utils.EntityUtils.getTableShortenedName;

/**
 * This class is still being tested
 */
@SuppressWarnings("unused")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleSpecificationImpl implements Specification {

    private final Class<?> entityClass;
    @Getter
    private StringBuilder finalSQL;
    @Getter
    private List<Object> parameters;
    @Getter
    private int times;

    {
        finalSQL = new StringBuilder();
        parameters = new ArrayList<>();
        times = 0;
    }

    public static SimpleSpecificationImpl of(Class<?> entityClass) {
        return new SimpleSpecificationImpl(entityClass);
    }

    public SimpleSpecificationImpl where(String fieldName, Object value) {
        times++;
        return equal(fieldName, value);
    }

    public SimpleSpecificationImpl and(String fieldName, Object value) {
        if (finalSQL.length() != 0) {
            finalSQL.append(" AND ");
        }
        times++;
        return equal(fieldName, value);
    }

    public SimpleSpecificationImpl or(String fieldName, Object value) {
        if (finalSQL.length() != 0) {
            finalSQL.append(" OR ");
        }
        times++;
        return equal(fieldName, value);
    }

    public SimpleSpecificationImpl where(Specification specification) {
        StringBuilder inputFinalSQL = specification.getFinalSQL();
        List<Object> inputParameters = specification.getParameters();
        parameters.addAll(inputParameters);
        if (finalSQL.length() != 0) {
            finalSQL.append("(").append(inputFinalSQL).append(")");
        } else {
            finalSQL.append(inputFinalSQL);
        }
        times++;
        return this;
    }

    public SimpleSpecificationImpl and(Specification specification) {
        StringBuilder inputFinalSQL = specification.getFinalSQL();
        List<Object> inputParameters = specification.getParameters();
        parameters.addAll(inputParameters);
        if (finalSQL.length() != 0) {
            if (specification.getTimes() < 2) {
                finalSQL.append(" AND ").append(inputFinalSQL);
            } else {
                finalSQL.append(" AND ( ").append(inputFinalSQL).append(" )");
            }
        } else {
            finalSQL.append(inputFinalSQL);
        }
        times++;
        return this;
    }

    public SimpleSpecificationImpl or(Specification specification) {
        StringBuilder inputFinalSQL = specification.getFinalSQL();
        List<Object> inputParameters = specification.getParameters();
        parameters.addAll(inputParameters);
        if (finalSQL.length() != 0) {
            if (specification.getTimes() < 2) {
                finalSQL.append(" OR ").append(inputFinalSQL);
            } else {
                finalSQL.append(" OR ( ").append(inputFinalSQL).append(" )");
            }
        } else {
            finalSQL.append(inputFinalSQL);
        }
        times++;
        return this;
    }

    public SimpleSpecificationImpl equal(String fieldName, Object value) {
        final String tableShortenName = getTableShortenedName(entityClass);
        final String columnNameOfField = getColumnNameFromFieldName(fieldName + CommonConstant.EMPTY_STRING, entityClass);
        if (columnNameOfField == null) {
            throw new IllegalArgumentException(String.format("Cannot find the column name of [%s]", fieldName));
        }
        parameters.add(value);
        finalSQL.append(tableShortenName).append(".").append(columnNameOfField).append(" = ?");
        times++;
        return this;
    }

    public SimpleSpecificationImpl notEqual(String fieldName, Object value) {
        final String tableShortenName = getTableShortenedName(entityClass);
        final String columnNameOfField = getColumnNameFromFieldName(fieldName + CommonConstant.EMPTY_STRING, entityClass);
        if (columnNameOfField == null) {
            throw new IllegalArgumentException(String.format("Cannot find the column name of [%s]", fieldName));
        }
        parameters.add(value);
        finalSQL.append(tableShortenName).append(".").append(columnNameOfField).append(" <> ?");
        times++;
        return this;
    }

    public SimpleSpecificationImpl in(String fieldName, List<Object> values) {
        final String tableShortenName = getTableShortenedName(entityClass);
        final String columnNameOfField = getColumnNameFromFieldName(fieldName + CommonConstant.EMPTY_STRING, entityClass);
        if (columnNameOfField == null) {
            throw new IllegalArgumentException(String.format("Cannot find the column name of [%s]", fieldName));
        }
        parameters.addAll(values);
        finalSQL.append(tableShortenName)
                .append(".")
                .append(columnNameOfField)
                .append(" IN ")
                .append("(")
                .append(
                        values.stream()
                                .map(a -> "?")
                                .collect(Collectors.joining(", "))
                )
                .append(")");
        times++;
        return this;
    }

    public SimpleSpecificationImpl like(String fieldName, Object value) {
        final String tableShortenName = getTableShortenedName(entityClass);
        final String columnNameOfField = getColumnNameFromFieldName(fieldName + CommonConstant.EMPTY_STRING, entityClass);
        if (columnNameOfField == null) {
            throw new IllegalArgumentException(String.format("Cannot find the column name of [%s]", fieldName));
        }
        parameters.add(value);
        finalSQL.append(tableShortenName).append(".").append(columnNameOfField).append(" LIKE ?");
        times++;
        return this;
    }

    public SimpleSpecificationImpl between(String fieldName, Object value1, Object value2) {
        final String tableShortenName = getTableShortenedName(entityClass);
        final String columnNameOfField = getColumnNameFromFieldName(fieldName + CommonConstant.EMPTY_STRING, entityClass);
        if (columnNameOfField == null) {
            throw new IllegalArgumentException(String.format("Cannot find the column name of [%s]", fieldName));
        }
        parameters.add(value1);
        parameters.add(value2);
        finalSQL.append(tableShortenName)
                .append(".")
                .append(columnNameOfField)
                .append(" BETWEEN ? AND ?");
        times++;
        return this;
    }

    public SimpleSpecificationImpl lessThan(String fieldName, Object value) {
        final String tableShortenName = getTableShortenedName(entityClass);
        final String columnNameOfField = getColumnNameFromFieldName(fieldName + CommonConstant.EMPTY_STRING, entityClass);
        if (columnNameOfField == null) {
            throw new IllegalArgumentException(String.format("Cannot find the column name of [%s]", fieldName));
        }
        parameters.add(value);
        finalSQL.append(tableShortenName)
                .append(".")
                .append(columnNameOfField)
                .append(" < ?");
        times++;
        return this;
    }

    public SimpleSpecificationImpl lessThanOrEqual(String fieldName, Object value) {
        final String tableShortenName = getTableShortenedName(entityClass);
        final String columnNameOfField = getColumnNameFromFieldName(fieldName + CommonConstant.EMPTY_STRING, entityClass);
        if (columnNameOfField == null) {
            throw new IllegalArgumentException(String.format("Cannot find the column name of [%s]", fieldName));
        }
        parameters.add(value);
        finalSQL.append(tableShortenName)
                .append(".")
                .append(columnNameOfField)
                .append(" <= ?");
        times++;
        return this;
    }

    public SimpleSpecificationImpl greaterThan(String fieldName, Object value) {
        final String tableShortenName = getTableShortenedName(entityClass);
        final String columnNameOfField = getColumnNameFromFieldName(fieldName + CommonConstant.EMPTY_STRING, entityClass);
        if (columnNameOfField == null) {
            throw new IllegalArgumentException(String.format("Cannot find the column name of [%s]", fieldName));
        }
        parameters.add(value);
        finalSQL.append(tableShortenName)
                .append(".")
                .append(columnNameOfField)
                .append(" > ?");
        times++;
        return this;
    }

    public SimpleSpecificationImpl greaterThanOrEqual(String fieldName, Object value) {
        final String tableShortenName = getTableShortenedName(entityClass);
        final String columnNameOfField = getColumnNameFromFieldName(fieldName + CommonConstant.EMPTY_STRING, entityClass);
        if (columnNameOfField == null) {
            throw new IllegalArgumentException(String.format("Cannot find the column name of [%s]", fieldName));
        }
        parameters.add(value);
        finalSQL.append(tableShortenName)
                .append(".")
                .append(columnNameOfField)
                .append(" >= ?");
        times++;
        return this;
    }

    public SimpleSpecificationImpl isNull(String fieldName) {
        final String tableShortenName = getTableShortenedName(entityClass);
        final String columnNameOfField = getColumnNameFromFieldName(fieldName + CommonConstant.EMPTY_STRING, entityClass);
        if (columnNameOfField == null) {
            throw new IllegalArgumentException(String.format("Cannot find the column name of [%s]", fieldName));
        }
        finalSQL.append(tableShortenName)
                .append(".")
                .append(columnNameOfField)
                .append(" IS NULL");
        times++;
        return this;
    }

    public SimpleSpecificationImpl isNotNull(String fieldName) {
        final String tableShortenName = getTableShortenedName(entityClass);
        final String columnNameOfField = getColumnNameFromFieldName(fieldName + CommonConstant.EMPTY_STRING, entityClass);
        if (columnNameOfField == null) {
            throw new IllegalArgumentException(String.format("Cannot find the column name of [%s]", fieldName));
        }
        finalSQL.append(tableShortenName)
                .append(".")
                .append(columnNameOfField)
                .append(" IS NOT NULL");
        times++;
        return this;
    }

}
