package vn.io.lcx.common.database.pageable;

import java.util.Map;

public interface Pageable {
    static PageableImpl ofPageable(int pageNumber, int pageSize) {
        return PageableImpl.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();
    }

    int getPageNumber();

    void setPageNumber(int pageNumber);

    int getPageSize();

    void setPageSize(int pageSize);

    Map<String, Direction> getColumnNameAndDirectionMap();

    void setColumnNameAndDirectionMap(Map<String, Direction> columnNameAndDirectionMap);

    default Map<String, Direction> getFieldNameAndDirectionMap() {
        return Map.of();
    }

    default void setFieldNameAndDirectionMap(Map<String, Direction> fieldNameAndDirectionMap) {
        throw new UnsupportedOperationException();
    }

    default int getOffset() {
        if (getPageNumber() > 0 || getPageSize() > 0) {
            int offset = (getPageNumber() - 1) * getPageSize();
            if (offset < 0) {
                throw new IllegalArgumentException("Page number should be started from 1");
            }
            return offset;
        }
        throw new IllegalArgumentException("Invalid `pageNumber` and `pageSize`");
    }

    Class<?> getEntityClass();

    void setEntityClass(Class<?> entityClass);

    void addNewColumnAndDirectionOrder(String columnName, Direction direction);

    Pageable add(String fieldName, Direction direction);

    void fieldToColumn();

    String toSql();
}
