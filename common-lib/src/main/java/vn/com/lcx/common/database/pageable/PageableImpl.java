package vn.com.lcx.common.database.pageable;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

public class PageableImpl implements Pageable {

    @Getter
    private int pageNumber;
    @Getter
    @Setter
    private int pageSize;
    @Getter
    private final Map<String, Direction> fieldNameAndDirectionMap;

    @Builder
    public PageableImpl(int pageSize, int pageNumber) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }

    {
        pageNumber = 0;
        pageSize = 0;
        fieldNameAndDirectionMap = new HashMap<>();
    }

    @Override
    public void setPageNumber(int pageNumber) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be started from 1");
        }
        this.pageNumber = pageNumber;
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
}
