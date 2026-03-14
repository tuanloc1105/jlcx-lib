package vn.io.lcx.common.constant;

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
            put("Byte", "getByte");
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
            put("Short", "getShort");
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
            put("Short", "getShort");
            put("String", "getString");
            put("LocalDateTime", "getLocalDateTime");
            put("LocalDate", "getLocalDate");
            put("OffsetDateTime", "getOffsetDateTime");
        }
    };

    public static final Map<String, String> DATA_TYPE_DEFAULT_VALUE_MAP = new HashMap<>() {
        private static final long serialVersionUID = 2607763254654075386L;

        {
            put("BigDecimal", "java.math.BigDecimal.ZERO");
            put("BigInteger", "java.math.BigInteger.ZERO");
            put("boolean", "false");
            put("Boolean", "false");
            put("byte", "(byte) 0");
            put("Byte", "(byte) 0");
            put("char", "'\\0'");
            put("Character", "null");
            put("double", "0D");
            put("Double", "0D");
            put("float", "0F");
            put("Float", "0F");
            put("int", "0");
            put("Integer", "0");
            put("long", "0L");
            put("Long", "0L");
            put("short", "0");
            put("Short", "(short) 0");
            put("String", "\"\"");
            put("LocalDateTime", "null");
            put("LocalDate", "null");
            put("OffsetDateTime", "null");
        }
    };

    public static final List<String> NUMBER_DATA_TYPE_CLASS_NAME = new ArrayList<>() {
        private static final long serialVersionUID = 1163000042574556634L;

        {
            add("BigDecimal");
            add("BigInteger");
            add("byte");
            add("Byte");
            add("double");
            add("Double");
            add("float");
            add("Float");
            add("int");
            add("Integer");
            add("long");
            add("Long");
            add("short");
            add("Short");
        }
    };

    public static final Map<String, String> PRIMITIVE_TO_WRAPPER_MAP = Map.of(
            "boolean", "Boolean",
            "byte", "Byte",
            "char", "Character",
            "double", "Double",
            "float", "Float",
            "int", "Integer",
            "long", "Long",
            "short", "Short"
    );

    public static final String DOT = "\\.";

}
