package vn.com.lcx.common.database.specification;

import java.util.List;

public interface Specification {

    StringBuilder getFinalSQL();

    List<Object> getParameters();

    SimpleSpecificationImpl where(String fieldName, Object value);

    SimpleSpecificationImpl and(String fieldName, Object value);

    SimpleSpecificationImpl or(String fieldName, Object value);

    SimpleSpecificationImpl where(Specification specification);

    SimpleSpecificationImpl and(Specification specification);

    SimpleSpecificationImpl or(Specification specification);

    SimpleSpecificationImpl equal(String fieldName, Object value);

    SimpleSpecificationImpl notEqual(String fieldName, Object value);

    SimpleSpecificationImpl in(String fieldName, List<Object> values);

    SimpleSpecificationImpl like(String fieldName, Object value);

    SimpleSpecificationImpl between(String fieldName, Object value1, Object value2);

    SimpleSpecificationImpl lessThan(String fieldName, Object value);

    SimpleSpecificationImpl lessThanOrEqual(String fieldName, Object value);

    SimpleSpecificationImpl greaterThan(String fieldName, Object value);

    SimpleSpecificationImpl greaterThanOrEqual(String fieldName, Object value);

    SimpleSpecificationImpl isNull(String fieldName);

    SimpleSpecificationImpl isNotNull(String fieldName);

}
