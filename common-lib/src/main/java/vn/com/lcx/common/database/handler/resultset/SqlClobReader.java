package vn.com.lcx.common.database.handler.resultset;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.LogUtils;

import java.io.BufferedReader;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;

public final class SqlClobReader {

    private SqlClobReader() {
    }

    public static String parseClobToString(Clob clob) {
        if (clob == null) {
            // throw new NullPointerException("clob is null");
            return CommonConstant.EMPTY_STRING;
        }
        StringBuilder clobString = new StringBuilder();
        try {
            Reader reader = clob.getCharacterStream();
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                clobString.append(line);
            }
        } catch (Throwable e) {
            LogUtils.writeLog(e.getMessage(), e);
            return CommonConstant.EMPTY_STRING;
        }
        return clobString.toString();
    }

    public static Clob convertStringToClob(Connection connection, String clobString) {
        if (StringUtils.isBlank(clobString)) {
            throw new NullPointerException("clobString is null");
        }
        if (connection == null) {
            throw new NullPointerException("connection is null");
        }
        Clob clob = null;
        try {
            clob = connection.createClob();
            clob.setString(1, clobString);
        } catch (Throwable e) {
            LogUtils.writeLog(e.getMessage(), e);
        }
        return clob;
    }

}
