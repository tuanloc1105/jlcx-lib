package vn.com.lcx.common.database.specification;

import java.util.List;

public interface Specification {

    static Specification create(Class<?> clz) {
        return SimpleSpecificationImpl.of(clz);
    }

    StringBuilder getFinalSQL();

    List<Object> getParameters();

    int getTimes();

    Specification where(String fieldName, Object value);

    Specification and(String fieldName, Object value);

    Specification or(String fieldName, Object value);

    Specification where(Specification specification);

    Specification and(Specification specification);

    Specification or(Specification specification);

    Specification equal(String fieldName, Object value);

    Specification notEqual(String fieldName, Object value);

    Specification in(String fieldName, List<Object> values);

    Specification like(String fieldName, Object value);

    Specification between(String fieldName, Object value1, Object value2);

    Specification lessThan(String fieldName, Object value);

    Specification lessThanOrEqual(String fieldName, Object value);

    Specification greaterThan(String fieldName, Object value);

    Specification greaterThanOrEqual(String fieldName, Object value);

    Specification isNull(String fieldName);

    Specification isNotNull(String fieldName);

}
