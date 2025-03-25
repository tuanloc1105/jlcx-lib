package vn.com.lcx.common.constant;

import com.google.gson.reflect.TypeToken;
import vn.com.lcx.common.database.handler.statement.BigDecimalHandler;
import vn.com.lcx.common.database.handler.statement.BooleanHandler;
import vn.com.lcx.common.database.handler.statement.DateHandler;
import vn.com.lcx.common.database.handler.statement.DoubleHandler;
import vn.com.lcx.common.database.handler.statement.FloatHandler;
import vn.com.lcx.common.database.handler.statement.IntegerHandler;
import vn.com.lcx.common.database.handler.statement.LocalDateHandler;
import vn.com.lcx.common.database.handler.statement.LocalDateTimeHandler;
import vn.com.lcx.common.database.handler.statement.LongHandler;
import vn.com.lcx.common.database.handler.statement.SqlStatementHandler;
import vn.com.lcx.common.database.handler.statement.StringHandler;
import vn.com.lcx.common.database.reflect.SelectStatementBuilder;
import vn.com.lcx.common.utils.LCXProperties;

import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CommonConstant {

    public static final String EMPTY_STRING = "";
    public static final String HYPHEN = "-";
    public static final String NULL_STRING = "null";
    public static final String DEFAULT_LOCAL_DATE_TIME_VIETNAMESE_STRING_PATTERN = "dd-MM-yyyy HH:mm:ss.SSS";
    public static final String DEFAULT_LOCAL_DATE_VIETNAMESE_STRING_PATTERN = "dd-MM-yyyy";
    public static final String LOCAL_DATE_TIME_STRING_PATTERN_1 = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String LOCAL_DATE_TIME_STRING_PATTERN_2 = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String LOCAL_DATE_TIME_STRING_PATTERN_3 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX";
    public static final String LOCAL_DATE_TIME_STRING_PATTERN_4 = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final String DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DEFAULT_LOCAL_DATE_STRING_PATTERN = "yyyy-MM-dd";
    public static final String UTF_8_STANDARD_CHARSET = "UTF-8";
    public static final TypeToken<LinkedHashMap<String, Object>> HASH_MAP_GSON_TYPE_TOKEN = new TypeToken<LinkedHashMap<String, Object>>() {
    };
    public static final Map<String, SqlStatementHandler> DATA_TYPE_AND_SQL_STATEMENT_METHOD_MAP = new HashMap<String, SqlStatementHandler>() {
        private static final long serialVersionUID = 7280484430132716574L;

        {
            put("Boolean", BooleanHandler.getInstance());
            put("Date", DateHandler.getInstance());
            put("Double", DoubleHandler.getInstance());
            put("Float", FloatHandler.getInstance());
            put("Integer", IntegerHandler.getInstance());
            put("LocalDateTime", LocalDateTimeHandler.getInstance());
            put("LocalDate", LocalDateHandler.getInstance());
            put("Long", LongHandler.getInstance());
            put("String", StringHandler.getInstance());
            put("BigDecimal", BigDecimalHandler.getInstance());
        }
    };
    public static final String ROOT_DIRECTORY_PROJECT_PATH = FileSystems.
            getDefault().
            getPath(EMPTY_STRING).
            toAbsolutePath().
            toString();
    public static final String TRACE_ID_MDC_KEY_NAME = "trace_id";
    public static final String OPERATION_NAME_MDC_KEY_NAME = "operation_name";
    public final static ConcurrentHashMap<String, SelectStatementBuilder> BUILDER_MAP = new ConcurrentHashMap<>();
    public static LCXProperties applicationConfig;

    private CommonConstant() {
    }
}
