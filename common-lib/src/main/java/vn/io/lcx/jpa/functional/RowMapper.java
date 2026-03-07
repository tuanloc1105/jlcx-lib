package vn.io.lcx.jpa.functional;

import java.sql.ResultSet;

@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet rs) throws Exception;
}
