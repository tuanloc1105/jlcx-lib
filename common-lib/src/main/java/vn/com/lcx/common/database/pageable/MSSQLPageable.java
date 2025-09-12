package vn.com.lcx.common.database.pageable;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.reactive.context.EntityMappingContainer;
import vn.com.lcx.reactive.entity.EntityMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static vn.com.lcx.common.database.utils.EntityUtils.getTableShortenedName;

public class MSSQLPageable implements Pageable {

    private int pageNumber;
    private int pageSize;
    private Map<String, Direction> columnNameAndDirectionMap;
    private Map<String, Direction> fieldNameAndDirectionMap;
    private Class<?> entityClass;

    public MSSQLPageable() {
    }

    public MSSQLPageable(int pageNumber,
                         int pageSize,
                         Map<String, Direction> columnNameAndDirectionMap,
                         Map<String, Direction> fieldNameAndDirectionMap,
                         Class<?> entityClass) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.columnNameAndDirectionMap = columnNameAndDirectionMap;
        this.fieldNameAndDirectionMap = fieldNameAndDirectionMap;
        this.entityClass = entityClass;
    }

    public static MSSQLPageableBuilder builder() {
        return new MSSQLPageableBuilder();
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public void setPageNumber(int pageNumber) {
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
        return columnNameAndDirectionMap;
    }

    @Override
    public void setColumnNameAndDirectionMap(Map<String, Direction> columnNameAndDirectionMap) {
        this.columnNameAndDirectionMap = columnNameAndDirectionMap;
    }

    public Map<String, Direction> getFieldNameAndDirectionMap() {
        return fieldNameAndDirectionMap;
    }

    public void setFieldNameAndDirectionMap(Map<String, Direction> fieldNameAndDirectionMap) {
        this.fieldNameAndDirectionMap = fieldNameAndDirectionMap;
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }

    @Override
    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public void addNewColumnAndDirectionOrder(String columnName, Direction direction) {
        if (this.columnNameAndDirectionMap == null) {
            this.columnNameAndDirectionMap = new HashMap<>();
        }
        this.columnNameAndDirectionMap.put(columnName, direction);
    }

    @Override
    public MSSQLPageable add(String fieldName, Direction direction) {
        if (this.fieldNameAndDirectionMap == null) {
            this.fieldNameAndDirectionMap = new HashMap<>();
        }
        this.fieldNameAndDirectionMap.put(fieldName, direction);
        return this;
    }

    @Override
    public void fieldToColumn() {
        if (this.fieldNameAndDirectionMap != null && !this.fieldNameAndDirectionMap.isEmpty() && entityClass != null) {
            for (Map.Entry<String, Direction> entry : this.fieldNameAndDirectionMap.entrySet()) {
                EntityMapping<?> entityMapping = EntityMappingContainer.getMapping(entityClass.getName());
                final var columnName = entityMapping.getColumnNameFromFieldName(entry.getKey());
                // final var columnName = EntityUtils.getColumnNameFromFieldName(entry.getKey(), entityClass);
                if (StringUtils.isBlank(columnName)) {
                    throw new IllegalArgumentException("Cannot find column name for field " + entry.getKey());
                }
                final var direction = entry.getValue();
                this.addNewColumnAndDirectionOrder(columnName, direction);
            }
        }
    }

    @Override
    public String toSql() {
        final var listOfOrder = new ArrayList<String>();
        final var orderClause = new StringBuilder();
        var offSetClause = CommonConstant.EMPTY_STRING;

        if (this.pageNumber > 0 || this.pageSize > 0) {
            int offset = (pageNumber - 1) * pageSize;
            if (offset < 0) {
                throw new IllegalArgumentException("page number should be started from 1");
            }
            offSetClause = String.format(
                    "OFFSET %s ROWS FETCH NEXT %s ROWS ONLY",
                    offset,
                    pageSize
            );

        }
        fieldToColumn();
        if (this.columnNameAndDirectionMap != null && !this.columnNameAndDirectionMap.isEmpty()) {
            if (this.entityClass != null) {
                final var tableShortName = getTableShortenedName(this.entityClass);
                for (Map.Entry<String, Direction> entry : this.columnNameAndDirectionMap.entrySet()) {
                    listOfOrder.add(
                            String.format("%s.%s %s", tableShortName, entry.getKey(), entry.getValue().name())
                    );
                }
            } else {
                for (Map.Entry<String, Direction> entry : this.columnNameAndDirectionMap.entrySet()) {
                    listOfOrder.add(
                            String.format("%s %s", entry.getKey(), entry.getValue().name())
                    );
                }
            }
            orderClause.append("ORDER BY ").append(String.join(", ", listOfOrder));
        }


        return String.format(
                " %s %s",
                orderClause,
                offSetClause
        );
    }

    public static class MSSQLPageableBuilder {
        private int pageNumber;
        private int pageSize;
        private Map<String, Direction> columnNameAndDirectionMap;
        private Map<String, Direction> fieldNameAndDirectionMap;
        private Class<?> entityClass;

        public MSSQLPageableBuilder() {
        }

        public MSSQLPageableBuilder pageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public MSSQLPageableBuilder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public MSSQLPageableBuilder columnNameAndDirectionMap(Map<String, Direction> columnNameAndDirectionMap) {
            this.columnNameAndDirectionMap = columnNameAndDirectionMap;
            return this;
        }

        public MSSQLPageableBuilder fieldNameAndDirectionMap(Map<String, Direction> fieldNameAndDirectionMap) {
            this.fieldNameAndDirectionMap = fieldNameAndDirectionMap;
            return this;
        }

        public MSSQLPageableBuilder entityClass(Class<?> entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public MSSQLPageable build() {
            return new MSSQLPageable(pageNumber, pageSize, columnNameAndDirectionMap, fieldNameAndDirectionMap, entityClass);
        }
    }

}
