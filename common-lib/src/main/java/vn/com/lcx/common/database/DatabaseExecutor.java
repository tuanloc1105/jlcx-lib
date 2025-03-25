package vn.com.lcx.common.database;

import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.database.type.OracleTypeEnum;
import vn.com.lcx.common.database.type.PostgresTypeEnum;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface DatabaseExecutor {

    String SUCCESS_KEY_NAME = "success";
    String SUCCESS_BUT_NO_INFO_KEY_NAME = "success.but.no.info";
    String FAILED_KEY_NAME = "failed";

    <T> List<T> executeQuery(Connection connection,
                             String sqlString,
                             Map<Integer, Object> parameter,
                             ResultSetHandler<T> handler);

    <T> List<T> executeOracleStoreProcedure(Connection connection,
                                            String storeProcedureName,
                                            Map<Integer, Object> inParameters,
                                            Map<Integer, OracleTypeEnum> outParameters,
                                            CallableStatementHandler<List<T>> handler);

    Map<String, Integer> executeOracleStoreProcedureBatch(Connection connection,
                                                          String storeProcedureName,
                                                          List<Map<Integer, Object>> inParameterMaps);

    <T> List<T> executePostgresStoreProcedure(Connection connection,
                                              String storeProcedureName,
                                              Map<Integer, Object> inParameters,
                                              Map<Integer, PostgresTypeEnum> outParameters,
                                              CallableStatementHandler<List<T>> handler);

    int executeMutation(Connection connection,
                        String sqlString,
                        Map<Integer, Object> parameter);

    BigDecimal executeInsertReturnId(Connection connection,
                                     String sqlString,
                                     Map<Integer, Object> parameter,
                                     DBTypeEnum dbType);

    Map<String, Integer> executeMutationBatch(Connection connection,
                                              String sqlString,
                                              List<Map<Integer, Object>> parameterMapList);

}
