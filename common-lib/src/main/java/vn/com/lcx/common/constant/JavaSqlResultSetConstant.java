package vn.com.lcx.common.constant;

import java.util.HashMap;
import java.util.Map;

public class JavaSqlResultSetConstant {

    public static final Map<String, String> RESULT_SET_DATA_TYPE_MAP = new HashMap<String, String>() {{
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
    }};

    public static final String DOT = "\\.";
    public static final String EMPTY_STRING = "";

}
