package vn.com.lcx.common.database.type;

import vn.com.lcx.common.database.reflect.SelectStatementBuilder;

import java.lang.reflect.Field;

public class SubTableEntry {

    private String columnName;
    private Field field;
    private Field matchField;
    private JoinType joinType;
    private SelectStatementBuilder selectStatementBuilder;

    public SubTableEntry() {
    }

    public SubTableEntry(String columnName, Field field, Field matchField, JoinType joinType, SelectStatementBuilder selectStatementBuilder) {
        this.columnName = columnName;
        this.field = field;
        this.matchField = matchField;
        this.joinType = joinType;
        this.selectStatementBuilder = selectStatementBuilder;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Field getMatchField() {
        return matchField;
    }

    public void setMatchField(Field matchField) {
        this.matchField = matchField;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public SelectStatementBuilder getSelectStatementBuilder() {
        return selectStatementBuilder;
    }

    public void setSelectStatementBuilder(SelectStatementBuilder selectStatementBuilder) {
        this.selectStatementBuilder = selectStatementBuilder;
    }

    public static SubTableEntryBuilder builder() {
        return new SubTableEntryBuilder();
    }

    public static class SubTableEntryBuilder {
        private String columnName;
        private Field field;
        private Field matchField;
        private JoinType joinType;
        private SelectStatementBuilder selectStatementBuilder;

        public SubTableEntryBuilder() {
        }

        public SubTableEntryBuilder columnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        public SubTableEntryBuilder field(Field field) {
            this.field = field;
            return this;
        }

        public SubTableEntryBuilder matchField(Field matchField) {
            this.matchField = matchField;
            return this;
        }

        public SubTableEntryBuilder joinType(JoinType joinType) {
            this.joinType = joinType;
            return this;
        }

        public SubTableEntryBuilder selectStatementBuilder(SelectStatementBuilder selectStatementBuilder) {
            this.selectStatementBuilder = selectStatementBuilder;
            return this;
        }

        public SubTableEntry build() {
            return new SubTableEntry(columnName, field, matchField, joinType, selectStatementBuilder);
        }
    }

}
