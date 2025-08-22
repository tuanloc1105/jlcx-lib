package vn.com.lcx.common.database.pool.wrapper;

import vn.com.lcx.common.database.pool.entry.ConnectionEntry;
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

/**
 * A wrapper class for JDBC {@link Connection} that integrates with the connection pool.
 * This class delegates all JDBC operations to the underlying connection while providing
 * connection pool management capabilities through the associated {@link ConnectionEntry}.
 *
 * <p>This class implements the standard JDBC {@link Connection} interface and adds
 * connection pool-specific functionality.</p>
 *
 * <strong>NOTE: No longer maintained</strong>
 *
 * @see Connection
 * @see ConnectionEntry
 */
@SuppressWarnings("SqlSourceToSinkFlow")
public class LCXConnection implements Connection {

    /**
     * The underlying connection entry that manages the connection's lifecycle in the pool.
     */
    private final ConnectionEntry connectionEntry;

    /**
     * Creates a new LCXConnection wrapper for the given connection entry.
     *
     * @param connectionEntry the connection entry to wrap, must not be null
     * @throws NullPointerException if connectionEntry is null
     */
    public LCXConnection(ConnectionEntry connectionEntry) {
        this.connectionEntry = connectionEntry;
    }

    /**
     * Gets the underlying JDBC connection from the connection entry.
     *
     * @return the underlying JDBC connection
     */
    private Connection getRealConnection() {
        return connectionEntry.getConnection();
    }

    /**
     * Gets the database type of this connection.
     *
     * @return the database type enum
     */
    public DBTypeEnum getDBType() {
        return connectionEntry.getDbType();
    }

    /**
     * Returns the connection to the pool by deactivating the connection entry.
     *
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void close() throws SQLException {
        this.connectionEntry.deactivate();
    }

    /**
     * {@inheritDoc}
     *
     * @return true if the connection is closed, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean isClosed() throws SQLException {
        return getRealConnection().isClosed();
    }

    /**
     * {@inheritDoc}
     *
     * @return a new default Statement object
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Statement createStatement() throws SQLException {
        return getRealConnection().createStatement();
    }

    /**
     * {@inheritDoc}
     *
     * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
     * @return a new default PreparedStatement object containing the pre-compiled SQL statement
     * @throws SQLException if a database access error occurs
     */
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return getRealConnection().prepareStatement(sql);
    }

    /**
     * {@inheritDoc}
     *
     * @param sql an SQL statement that may contain one or more '?' parameter placeholders
     * @return a new default CallableStatement object containing the pre-compiled SQL statement
     * @throws SQLException if a database access error occurs
     */
    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return getRealConnection().prepareCall(sql);
    }

    /**
     * {@inheritDoc}
     *
     * @param sql an SQL statement that may contain one or more '?' parameter placeholders
     * @return the native form of this statement
     * @throws SQLException if a database access error occurs
     */
    @Override
    public String nativeSQL(String sql) throws SQLException {
        return getRealConnection().nativeSQL(sql);
    }

    /**
     * {@inheritDoc}
     *
     * @return the current auto-commit mode of this connection
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean getAutoCommit() throws SQLException {
        return getRealConnection().getAutoCommit();
    }

    /**
     * {@inheritDoc}
     *
     * @param autoCommit true to enable auto-commit mode; false to disable it
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        getRealConnection().setAutoCommit(autoCommit);
    }

    /**
     * {@inheritDoc}
     *
     * @throws SQLException if a database access error occurs or this method is called while
     *                      in auto-commit mode
     */
    @Override
    public void commit() throws SQLException {
        getRealConnection().commit();
    }

    /**
     * {@inheritDoc}
     *
     * @throws SQLException if a database access error occurs or this method is called while
     *                      in auto-commit mode
     */
    @Override
    public void rollback() throws SQLException {
        getRealConnection().rollback();
    }

    /**
     * {@inheritDoc}
     *
     * @return a DatabaseMetaData object for this connection
     * @throws SQLException if a database access error occurs
     */
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return getRealConnection().getMetaData();
    }

    /**
     * {@inheritDoc}
     *
     * @return true if this connection is read-only, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean isReadOnly() throws SQLException {
        return getRealConnection().isReadOnly();
    }

    /**
     * {@inheritDoc}
     *
     * @param readOnly true enables read-only mode; false disables it
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        getRealConnection().setReadOnly(readOnly);
    }

    /**
     * {@inheritDoc}
     *
     * @return the current catalog name or null if there is none
     * @throws SQLException if a database access error occurs
     */
    @Override
    public String getCatalog() throws SQLException {
        return getRealConnection().getCatalog();
    }

    /**
     * {@inheritDoc}
     *
     * @param catalog the name of the catalog (subspace in this database) to connect to
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void setCatalog(String catalog) throws SQLException {
        getRealConnection().setCatalog(catalog);
    }

    /**
     * {@inheritDoc}
     *
     * @return the current transaction isolation level
     * @throws SQLException if a database access error occurs
     */
    @Override
    public int getTransactionIsolation() throws SQLException {
        return getRealConnection().getTransactionIsolation();
    }

    /**
     * {@inheritDoc}
     *
     * @param level one of the following Connection constants:
     *              Connection.TRANSACTION_READ_UNCOMMITTED,
     *              Connection.TRANSACTION_READ_COMMITTED,
     *              Connection.TRANSACTION_REPEATABLE_READ, or
     *              Connection.TRANSACTION_SERIALIZABLE
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        getRealConnection().setTransactionIsolation(level);
    }

    /**
     * {@inheritDoc}
     *
     * @return the first SQLWarning object or null if there are no warnings
     * @throws SQLException if a database access error occurs
     */
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return getRealConnection().getWarnings();
    }

    /**
     * {@inheritDoc}
     *
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void clearWarnings() throws SQLException {
        getRealConnection().clearWarnings();
    }

    /**
     * {@inheritDoc}
     *
     * @param resultSetType        a result set type; one of
     *                             ResultSet.TYPE_FORWARD_ONLY,
     *                             ResultSet.TYPE_SCROLL_INSENSITIVE, or
     *                             ResultSet.TYPE_SCROLL_SENSITIVE
     * @param resultSetConcurrency a concurrency type; one of
     *                             ResultSet.CONCUR_READ_ONLY or
     *                             ResultSet.CONCUR_UPDATABLE
     * @return a new Statement object that will generate ResultSet objects with the given
     * type and concurrency
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return getRealConnection().createStatement(resultSetType, resultSetConcurrency);
    }

    /**
     * {@inheritDoc}
     *
     * @param sql                  a String containing the SQL statement
     * @param resultSetType        a result set type; one of
     *                             ResultSet.TYPE_FORWARD_ONLY,
     *                             ResultSet.TYPE_SCROLL_INSENSITIVE, or
     *                             ResultSet.TYPE_SCROLL_SENSITIVE
     * @param resultSetConcurrency a concurrency type; one of
     *                             ResultSet.CONCUR_READ_ONLY or
     *                             ResultSet.CONCUR_UPDATABLE
     * @return a new PreparedStatement object containing the pre-compiled SQL statement
     * that will produce ResultSet objects with the given type and concurrency
     * @throws SQLException if a database access error occurs
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return getRealConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    /**
     * {@inheritDoc}
     *
     * @param sql                  a String containing the SQL statement
     * @param resultSetType        a result set type; one of
     *                             ResultSet.TYPE_FORWARD_ONLY,
     *                             ResultSet.TYPE_SCROLL_INSENSITIVE, or
     *                             ResultSet.TYPE_SCROLL_SENSITIVE
     * @param resultSetConcurrency a concurrency type; one of
     *                             ResultSet.CONCUR_READ_ONLY or
     *                             ResultSet.CONCUR_UPDATABLE
     * @return a new CallableStatement object containing the pre-compiled SQL statement
     * that will produce ResultSet objects with the given type and concurrency
     * @throws SQLException if a database access error occurs
     */
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return getRealConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    /**
     * {@inheritDoc}
     *
     * @return the type map object associated with this connection
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return getRealConnection().getTypeMap();
    }

    /**
     * {@inheritDoc}
     *
     * @param map the type map to install as the default for this connection
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        getRealConnection().setTypeMap(map);
    }

    /**
     * {@inheritDoc}
     *
     * @return the holdability of ResultSet objects created using this Connection object
     * @throws SQLException if a database access error occurs
     */
    @Override
    public int getHoldability() throws SQLException {
        return getRealConnection().getHoldability();
    }

    /**
     * {@inheritDoc}
     *
     * @param holdability a ResultSet holdability constant:
     *                    ResultSet.HOLD_CURSORS_OVER_COMMIT or
     *                    ResultSet.CLOSE_CURSORS_AT_COMMIT
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void setHoldability(int holdability) throws SQLException {
        getRealConnection().setHoldability(holdability);
    }

    /**
     * {@inheritDoc}
     *
     * @return the new Savepoint object
     * @throws SQLException if a database access error occurs or this method is called while
     *                      in auto-commit mode
     */
    @Override
    public Savepoint setSavepoint() throws SQLException {
        return getRealConnection().setSavepoint();
    }

    /**
     * {@inheritDoc}
     *
     * @param name a String containing the name of the savepoint
     * @return the new Savepoint object
     * @throws SQLException if a database access error occurs or this method is called while
     *                      in auto-commit mode
     */
    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return getRealConnection().setSavepoint(name);
    }

    /**
     * {@inheritDoc}
     *
     * @param savepoint the Savepoint object to roll back to
     * @throws SQLException if a database access error occurs or the Savepoint object is not valid
     */
    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        getRealConnection().rollback(savepoint);
    }

    /**
     * {@inheritDoc}
     *
     * @param savepoint the Savepoint object to be removed
     * @throws SQLException if a database access error occurs or the Savepoint object is not valid
     */
    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        getRealConnection().releaseSavepoint(savepoint);
    }

    /**
     * {@inheritDoc}
     *
     * @param resultSetType        one of the following ResultSet constants:
     *                             ResultSet.TYPE_FORWARD_ONLY,
     *                             ResultSet.TYPE_SCROLL_INSENSITIVE, or
     *                             ResultSet.TYPE_SCROLL_SENSITIVE
     * @param resultSetConcurrency one of the following ResultSet constants
     *                             for specifying the result set's concurrency type:
     *                             ResultSet.CONCUR_READ_ONLY or
     *                             ResultSet.CONCUR_UPDATABLE
     * @param resultSetHoldability one of the following ResultSet constants:
     *                             ResultSet.HOLD_CURSORS_OVER_COMMIT or
     *                             ResultSet.CLOSE_CURSORS_AT_COMMIT
     * @return a new Statement object that will generate ResultSet objects with the given
     * type, concurrency, and holdability
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return getRealConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    /**
     * {@inheritDoc}
     *
     * @param sql                  a String containing the SQL statement
     * @param resultSetType        one of the following ResultSet constants:
     *                             ResultSet.TYPE_FORWARD_ONLY,
     *                             ResultSet.TYPE_SCROLL_INSENSITIVE, or
     *                             ResultSet.TYPE_SCROLL_SENSITIVE
     * @param resultSetConcurrency one of the following ResultSet constants
     *                             for specifying the result set's concurrency type:
     *                             ResultSet.CONCUR_READ_ONLY or
     *                             ResultSet.CONCUR_UPDATABLE
     * @param resultSetHoldability one of the following ResultSet constants:
     *                             ResultSet.HOLD_CURSORS_OVER_COMMIT or
     *                             ResultSet.CLOSE_CURSORS_AT_COMMIT
     * @return a new PreparedStatement object, containing the pre-compiled SQL statement,
     * that will generate ResultSet objects with the given type, concurrency, and holdability
     * @throws SQLException if a database access error occurs
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return getRealConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    /**
     * {@inheritDoc}
     *
     * @param sql                  a String containing the SQL statement
     * @param resultSetType        one of the following ResultSet constants:
     *                             ResultSet.TYPE_FORWARD_ONLY,
     *                             ResultSet.TYPE_SCROLL_INSENSITIVE, or
     *                             ResultSet.TYPE_SCROLL_SENSITIVE
     * @param resultSetConcurrency one of the following ResultSet constants
     *                             for specifying the result set's concurrency type:
     *                             ResultSet.CONCUR_READ_ONLY or
     *                             ResultSet.CONCUR_UPDATABLE
     * @param resultSetHoldability one of the following ResultSet constants:
     *                             ResultSet.HOLD_CURSORS_OVER_COMMIT or
     *                             ResultSet.CLOSE_CURSORS_AT_COMMIT
     * @return a new CallableStatement object, containing the pre-compiled SQL statement,
     * that will generate ResultSet objects with the given type, concurrency, and holdability
     * @throws SQLException if a database access error occurs
     */
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return getRealConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    /**
     * {@inheritDoc}
     *
     * @param sql               an SQL statement that may contain one or more '?' IN parameter placeholders
     * @param autoGeneratedKeys a flag indicating whether auto-generated keys should be returned;
     *                          one of Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
     * @return a new PreparedStatement object, containing the pre-compiled SQL statement,
     * that will have the capability of returning auto-generated keys
     * @throws SQLException if a database access error occurs
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return getRealConnection().prepareStatement(sql, autoGeneratedKeys);
    }

    /**
     * {@inheritDoc}
     *
     * @param sql           an SQL statement that may contain one or more '?' IN parameter placeholders
     * @param columnIndexes an array of column indexes indicating the columns that should be
     *                      returned from the inserted row or rows
     * @return a new PreparedStatement object, containing the pre-compiled statement,
     * that is capable of returning the auto-generated keys designated by the given array
     * @throws SQLException if a database access error occurs
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return getRealConnection().prepareStatement(sql, columnIndexes);
    }

    /**
     * {@inheritDoc}
     *
     * @param sql         an SQL statement that may contain one or more '?' IN parameter placeholders
     * @param columnNames an array of column names indicating the columns that should be
     *                    returned from the inserted row or rows
     * @return a new PreparedStatement object, containing the pre-compiled statement,
     * that is capable of returning the auto-generated keys designated by the given array
     * @throws SQLException if a database access error occurs
     */
    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return getRealConnection().prepareStatement(sql, columnNames);
    }

    /**
     * {@inheritDoc}
     *
     * @return a Clob object containing no data
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Clob createClob() throws SQLException {
        return getRealConnection().createClob();
    }

    /**
     * {@inheritDoc}
     *
     * @return a Blob object containing no data
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Blob createBlob() throws SQLException {
        return getRealConnection().createBlob();
    }

    /**
     * {@inheritDoc}
     *
     * @return a NClob object containing no data
     * @throws SQLException if a database access error occurs
     */
    @Override
    public NClob createNClob() throws SQLException {
        return getRealConnection().createNClob();
    }

    /**
     * {@inheritDoc}
     *
     * @return a SQLXML object containing no data
     * @throws SQLException if a database access error occurs
     */
    @Override
    public SQLXML createSQLXML() throws SQLException {
        return getRealConnection().createSQLXML();
    }

    /**
     * {@inheritDoc}
     *
     * @param timeout the maximum time in seconds to wait for the database operation used to validate
     *                the connection to complete. If the timeout period expires before the operation
     *                completes, this method returns false. A value of 0 indicates a timeout is not
     *                applied to the database operation.
     * @return true if the connection is valid, false otherwise
     * @throws SQLException if the value supplied for timeout is less than 0
     */
    @Override
    public boolean isValid(int timeout) throws SQLException {
        return getRealConnection().isValid(timeout);
    }

    /**
     * {@inheritDoc}
     *
     * @param name  the name of the client info property to set
     * @param value the value to set the client info property to
     * @throws SQLClientInfoException if the database server returns an error while
     *                                setting the client info value on the database server
     */
    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        getRealConnection().setClientInfo(name, value);
    }

    /**
     * {@inheritDoc}
     *
     * @param name the name of the client info property to retrieve
     * @return the value of the client info property specified, or null if the property is not found
     * @throws SQLException if the database server returns an error when fetching the client info value
     */
    @Override
    public String getClientInfo(String name) throws SQLException {
        return getRealConnection().getClientInfo(name);
    }

    /**
     * {@inheritDoc}
     *
     * @return a Properties object that contains the name and current value of each of the
     * client info properties supported by the driver
     * @throws SQLException if the database server returns an error when fetching the client info values
     */
    @Override
    public Properties getClientInfo() throws SQLException {
        return getRealConnection().getClientInfo();
    }

    /**
     * {@inheritDoc}
     *
     * @param properties the list of client info properties to set
     * @throws SQLClientInfoException if the database server returns an error while
     *                                setting the client info values on the database server
     */
    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        getRealConnection().setClientInfo(properties);
    }

    /**
     * {@inheritDoc}
     *
     * @param typeName the SQL name of the type the elements of the array map to
     * @param elements the elements that populate the returned object
     * @return an Array object whose elements map to the specified SQL type
     * @throws SQLException if a database error occurs, the JDBC type is not appropriate for the typeName,
     *                      or the typeName is not a supported type
     */
    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return getRealConnection().createArrayOf(typeName, elements);
    }

    /**
     * {@inheritDoc}
     *
     * @param typeName   the SQL type name of the SQL structured type that this Struct object maps to
     * @param attributes the attributes that populate the returned object
     * @return a Struct object that maps to the given SQL type and is populated with the given attributes
     * @throws SQLException if a database error occurs, the type map is not found, or the typeName is not a supported type
     */
    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return getRealConnection().createStruct(typeName, attributes);
    }

    /**
     * {@inheritDoc}
     *
     * @return the current schema name or null if there is none
     * @throws SQLException if a database access error occurs
     */
    @Override
    public String getSchema() throws SQLException {
        return getRealConnection().getSchema();
    }

    /**
     * {@inheritDoc}
     *
     * @param schema the name of a schema in which to work
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void setSchema(String schema) throws SQLException {
        getRealConnection().setSchema(schema);
    }

    /**
     * {@inheritDoc}
     *
     * @param executor the Executor implementation which will be used by this method
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void abort(Executor executor) throws SQLException {
        getRealConnection().abort(executor);
    }

    /**
     * {@inheritDoc}
     *
     * @param executor     the Executor implementation which will be used by this method
     * @param milliseconds the time in milliseconds to wait for the database operation
     *                     used to abort the connection to complete
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        getRealConnection().setNetworkTimeout(executor, milliseconds);
    }

    /**
     * {@inheritDoc}
     *
     * @return the current network timeout value in milliseconds; 0 means there is no timeout
     * @throws SQLException if a database access error occurs
     */
    @Override
    public int getNetworkTimeout() throws SQLException {
        return getRealConnection().getNetworkTimeout();
    }

    /**
     * {@inheritDoc}
     *
     * @param iface A Class defining an interface that the result must implement
     * @return an object that implements the interface
     * @throws SQLException if no object found that implements the interface
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getRealConnection().unwrap(iface);
    }

    /**
     * {@inheritDoc}
     *
     * @param iface a Class defining an interface
     * @return true if this implements the interface or directly or indirectly wraps an object that does
     * @throws SQLException if an error occurs while determining whether this is a wrapper for an object with the given interface
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getRealConnection().isWrapperFor(iface);
    }

}
