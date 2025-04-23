package vn.com.lcx.common.database.pool.wrapper;

import vn.com.lcx.common.database.type.DBTypeEnum;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class HikariConnectionWrapper extends LCXConnection {

    private Connection realHikariConnection;
    private DBTypeEnum dbType;

    public HikariConnectionWrapper(Connection connection, DBTypeEnum dbType) {
        super(null);
        this.realHikariConnection = connection;
        this.dbType = dbType;
    }

    public DBTypeEnum getDBType() {
        return dbType;
    }

    @Override
    public void close() throws SQLException {
        realHikariConnection.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return realHikariConnection.isClosed();
    }

    @Override
    public Statement createStatement() throws SQLException {
        return realHikariConnection.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return realHikariConnection.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return realHikariConnection.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return realHikariConnection.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        realHikariConnection.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return realHikariConnection.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        realHikariConnection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        realHikariConnection.rollback();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return realHikariConnection.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        realHikariConnection.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return realHikariConnection.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        realHikariConnection.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return realHikariConnection.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        realHikariConnection.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return realHikariConnection.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return realHikariConnection.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        realHikariConnection.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return realHikariConnection.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return realHikariConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return realHikariConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return realHikariConnection.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        realHikariConnection.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        realHikariConnection.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return realHikariConnection.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return realHikariConnection.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return realHikariConnection.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        realHikariConnection.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        realHikariConnection.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return realHikariConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return realHikariConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return realHikariConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return realHikariConnection.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return realHikariConnection.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return realHikariConnection.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return realHikariConnection.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return realHikariConnection.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return realHikariConnection.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return realHikariConnection.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return realHikariConnection.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        realHikariConnection.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        realHikariConnection.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return realHikariConnection.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return realHikariConnection.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return realHikariConnection.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return realHikariConnection.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        realHikariConnection.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return realHikariConnection.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        realHikariConnection.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        realHikariConnection.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return realHikariConnection.getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return realHikariConnection.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return realHikariConnection.isWrapperFor(iface);
    }

}
