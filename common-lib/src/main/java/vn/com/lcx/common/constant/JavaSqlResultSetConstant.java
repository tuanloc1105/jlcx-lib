package vn.com.lcx.common.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaSqlResultSetConstant {

    public static final Map<String, String> RESULT_SET_DATA_TYPE_MAP = new HashMap<>() {
        private static final long serialVersionUID = 4230451577017832954L;

        {
            put("BigDecimal", "getBigDecimal");
            put("Blob", "getBlob");
            put("boolean", "getBoolean");
            put("Boolean", "getBoolean");
            put("byte", "getByte");
            put("byte[]", "getBytes");
            put("Clob", "getClob");
            put("Date", "getDate");
            put("double", "getDouble");
            put("Double", "getDouble");
            put("float", "getFloat");
            put("Float", "getFloat");
            put("int", "getInt");
            put("Integer", "getInt");
            put("long", "getLong");
            put("Long", "getLong");
            put("ResultSetMetaData", "getMetaData");
            put("NClob", "getNClob");
            put("Object", "getObject");
            put("RowId", "getRowId");
            put("short", "getShort");
            put("Statement", "getStatement");
            put("String", "getString");
            put("Time", "getTime");
            put("Timestamp", "getTimestamp");
            put("URL", "getURL");
            put("SQLWarning", "getWarnings");
        }
    };

    public static final Map<String, String> VERTX_SQL_CLIENT_ROW = new HashMap<>() {
        private static final long serialVersionUID = -2374136735745773476L;

        {
            put("BigDecimal", "getBigDecimal");
            put("boolean", "getBoolean");
            put("Boolean", "getBoolean");
            put("double", "getDouble");
            put("Double", "getDouble");
            put("float", "getFloat");
            put("Float", "getFloat");
            put("int", "getInteger");
            put("Integer", "getInteger");
            put("long", "getLong");
            put("Long", "getLong");
            put("short", "getShort");
            put("String", "getString");
            put("LocalDateTime", "getLocalDateTime");
            put("LocalDate", "getLocalDate");
        }
    };

    public static final Map<String, String> DATA_TYPE_DEFAULT_VALUE_MAP = new HashMap<>() {
        private static final long serialVersionUID = 2607763254654075386L;

        {
            put("BigDecimal", "java.math.BigDecimal.ZERO");
            put("BigInteger", "java.math.BigInteger.ZERO");
            put("boolean", "false");
            put("Boolean", "false");
            put("double", "0D");
            put("Double", "0D");
            put("float", "0F");
            put("Float", "0F");
            put("int", "0");
            put("Integer", "0");
            put("long", "0L");
            put("Long", "0L");
            put("short", "0");
            put("String", "\"\"");
            put("LocalDateTime", "null");
            put("LocalDate", "null");
        }
    };

    public static final List<String> NUMBER_DATA_TYPE_CLASS_NAME = new ArrayList<>() {
        private static final long serialVersionUID = 1163000042574556634L;

        {
            add("BigDecimal");
            add("BigInteger");
            add("long");
            add("Long");
        }
    };

    public static final String DOT = "\\.";

}
