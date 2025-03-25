package vn.com.lcx.common.database.type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.common.database.reflect.SelectStatementBuilder;

import java.lang.reflect.Field;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubTableEntry {

    private String columnName;
    private Field field;
    private Field matchField;
    private JoinType joinType;
    private SelectStatementBuilder selectStatementBuilder;

}
