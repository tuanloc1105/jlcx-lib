package vn.com.lcx.jpa.functional;

import java.sql.ResultSet;

@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet rs) throws Exception;
}
