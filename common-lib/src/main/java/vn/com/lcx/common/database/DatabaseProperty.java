package vn.com.lcx.common.database;

import org.apache.commons.lang3.StringUtils;

public class DatabaseProperty {
    private String connectionString;
    private String username;
    private String password;
    private String driverClassName;
    private int initialPoolSize;
    private int maxPoolSize;
    private int maxTimeout; // second
    private boolean showSql;
    private boolean showSqlParameter;

    public DatabaseProperty(String connectionString,
                            String username,
                            String password,
                            String driverClassName,
                            int initialPoolSize,
                            int maxPoolSize,
                            int maxTimeout,
                            boolean showSql,
                            boolean showSqlParameter) {
        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
        this.initialPoolSize = initialPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.maxTimeout = maxTimeout;
        this.showSql = showSql;
        this.showSqlParameter = showSqlParameter;
    }

    public DatabaseProperty() {
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public int getInitialPoolSize() {
        return initialPoolSize;
    }

    public void setInitialPoolSize(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMaxTimeout() {
        return maxTimeout;
    }

    public void setMaxTimeout(int maxTimeout) {
        this.maxTimeout = maxTimeout;
    }

    public boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }

    public boolean isShowSqlParameter() {
        return showSqlParameter;
    }

    public void setShowSqlParameter(boolean showSqlParameter) {
        this.showSqlParameter = showSqlParameter;
    }

    public boolean propertiesIsAllSet() {
        boolean connectionStringIsNotNull = StringUtils.isNotBlank(this.connectionString);
        boolean usernameIsNotNull = StringUtils.isNotBlank(this.username);
        boolean passwordIsNotNull = StringUtils.isNotBlank(this.password);
        boolean initialPoolSizeIsNotZero = initialPoolSize != 0;
        boolean maxPoolSizeIsNotZero = maxPoolSize != 0;
        boolean maxTimeoutIsNotZero = maxTimeout != 0;
        return connectionStringIsNotNull &&
                usernameIsNotNull &&
                passwordIsNotNull &&
                initialPoolSizeIsNotZero &&
                maxPoolSizeIsNotZero &&
                maxTimeoutIsNotZero;
    }
}
