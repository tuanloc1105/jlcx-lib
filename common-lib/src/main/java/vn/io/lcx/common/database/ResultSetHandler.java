package vn.io.lcx.common.database;

import java.sql.ResultSet;

public interface ResultSetHandler<T> {
    T handle(ResultSet resultSet);
}
