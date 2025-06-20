package vn.com.lcx.common.database.pageable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.utils.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static vn.com.lcx.common.database.utils.EntityUtils.getTableShortenedName;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MSSQLPageable implements Pageable {

    private int pageNumber;
    private int pageSize;
    private Map<String, Direction> columnNameAndDirectionMap;
    private Map<String, Direction> fieldNameAndDirectionMap;
    private Class<?> entityClass;

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
                final var columnName = EntityUtils.getColumnNameFromFieldName(entry.getKey(), entityClass);
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

}
