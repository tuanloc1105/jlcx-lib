package vn.com.lcx.common.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@Data
@NoArgsConstructor
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
