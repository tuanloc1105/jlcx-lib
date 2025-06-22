package vn.com.lcx.common.database.type;

public enum JoinType {

    INNER_JOIN("INNER JOIN"),
    LEFT_JOIN("LEFT JOIN"),
    RIGHT_JOIN("RIGHT JOIN"),
    FULL_JOIN("FULL JOIN"),
    CROSS_JOIN("CROSS JOIN"),
    ;
    private final String statement;

    JoinType(String statement) {
        this.statement = statement;
    }

    public String getStatement() {
        return statement;
    }

}
