package vn.com.lcx.common.database.pageable;

import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

public class PageableImpl implements Pageable {

    private final Map<String, Direction> fieldNameAndDirectionMap;
    private int pageNumber;
    private int pageSize;

    {
        pageNumber = 0;
        pageSize = 0;
        fieldNameAndDirectionMap = new HashMap<>();
    }

    public PageableImpl(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public static PageableImplBuilder builder() {
        return new PageableImplBuilder();
    }

    public Map<String, Direction> getFieldNameAndDirectionMap() {
        return fieldNameAndDirectionMap;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public void setPageNumber(int pageNumber) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be started from 1");
        }
        this.pageNumber = pageNumber;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public Map<String, Direction> getColumnNameAndDirectionMap() {
        return Map.of();
    }

    @Override
    public void setColumnNameAndDirectionMap(Map<String, Direction> columnNameAndDirectionMap) {
        throw new NotImplementedException();
    }

    @Override
    public Class<?> getEntityClass() {
        throw new NotImplementedException();
    }

    @Override
    public void setEntityClass(Class<?> entityClass) {
        throw new NotImplementedException();
    }

    @Override
    public void addNewColumnAndDirectionOrder(String columnName, Direction direction) {
        throw new NotImplementedException();
    }

    @Override
    public Pageable add(String fieldName, Direction direction) {
        this.fieldNameAndDirectionMap.put(fieldName, direction);
        return this;
    }

    @Override
    public void fieldToColumn() {
        throw new NotImplementedException();
    }

    @Override
    public String toSql() {
        throw new NotImplementedException();
    }

    public int getOffset() {
        if (this.pageNumber > 0 || this.pageSize > 0) {
            int offset = (pageNumber - 1) * pageSize;
            if (offset < 0) {
                throw new IllegalArgumentException("Page number should be started from 1");
            }
            return offset;
        }
        throw new IllegalArgumentException("Invalid `pageNumber` and `pageSize`");
    }

    public static class PageableImplBuilder {
        private int pageNumber;
        private int pageSize;

        public PageableImplBuilder() {
        }

        public PageableImplBuilder pageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public PageableImplBuilder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PageableImpl build() {
            return new PageableImpl(pageNumber, pageSize);
        }

    }

}
