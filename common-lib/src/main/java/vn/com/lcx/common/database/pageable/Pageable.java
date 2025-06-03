package vn.com.lcx.common.database.pageable;

import java.util.Map;

public interface Pageable {
    int getPageNumber();

    void setPageNumber(int pageNumber);

    int getPageSize();

    void setPageSize(int pageSize);

    Map<String, Direction> getColumnNameAndDirectionMap();

    void setColumnNameAndDirectionMap(Map<String, Direction> columnNameAndDirectionMap);

    Class<?> getEntityClass();

    void setEntityClass(Class<?> entityClass);

    void addNewColumnAndDirectionOrder(String columnName, Direction direction);

    Pageable add(String fieldName, Direction direction);

    void fieldToColumn();

    String toSql();

    static PageableImpl ofPageable(int pageNumber, int pageSize) {
        return PageableImpl.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();
    }
}
