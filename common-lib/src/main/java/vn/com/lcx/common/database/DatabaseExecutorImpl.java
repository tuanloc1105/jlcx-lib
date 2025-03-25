package vn.com.lcx.common.database;

import lombok.val;
import lombok.var;
import oracle.jdbc.OraclePreparedStatement;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.handler.statement.SqlStatementHandler;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.database.type.OracleTypeEnum;
import vn.com.lcx.common.database.type.PostgresTypeEnum;
import vn.com.lcx.common.utils.LogUtils;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static vn.com.lcx.common.constant.CommonConstant.DATA_TYPE_AND_SQL_STATEMENT_METHOD_MAP;

public class DatabaseExecutorImpl implements DatabaseExecutor {

    private static final DatabaseExecutorImpl INSTANCE = new DatabaseExecutorImpl();

    private DatabaseExecutorImpl() {
    }

    public static DatabaseExecutorImpl getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public <T> List<T> executeQuery(Connection connection,
                                    String sqlString,
                                    Map<Integer, Object> parameter,
                                    ResultSetHandler<T> handler) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<T> result = new ArrayList<>();
        try {
            var transactionIsEnabled = !connection.getAutoCommit();
            String finalQueryString;
            if (transactionIsEnabled) {
                finalQueryString = String.format("%s FOR UPDATE", sqlString);
            } else {
                finalQueryString = sqlString;
            }
            // finalQueryString = MyStringUtils.minifyString(finalQueryString);
            statement = connection.prepareStatement(finalQueryString);
            LogUtils.writeLog2(LogUtils.Level.INFO, "\n" + finalQueryString.replaceAll("^\\n+|\\n+$", CommonConstant.EMPTY_STRING));
            var parameterIsNotNullAndNotEmpty = parameter != null && !parameter.isEmpty();
            if (parameterIsNotNullAndNotEmpty) {
                StringBuilder parametersLog = new StringBuilder("parameters:");
                for (Integer i : parameter.keySet()) {
                    Object parameterValue = parameter.get(i);
                    String aNull = String.format(
                            "\n\t- parameter %s: %s",
                            String.format("%-3d %-20s)", i, "("),
                            "NULL"
                    );
                    if (parameterValue == null) {
                        statement.setObject(i, null);
                        parametersLog.append(
                                aNull
                        );
                        continue;
                    }
                    Class<?> parameterClass = parameter.get(i).getClass();
                    if (parameterClass.isPrimitive()) {
                        statement.setObject(i, null);
                        parametersLog.append(
                                aNull
                        );
                        continue;
                    }
                    String parameterSimpleClassName = parameterClass.getSimpleName();
                    SqlStatementHandler sqlStatementHandler = DATA_TYPE_AND_SQL_STATEMENT_METHOD_MAP.get(parameterSimpleClassName);
                    if (sqlStatementHandler == null) {
                        throw new RuntimeException("unknown parameter type");
                    }
                    parametersLog.append(
                            String.format(
                                    "\n\t- parameter %s: %s",
                                    String.format("%-3d %-20s)", i, "(" + parameterSimpleClassName),
                                    parameterValue
                            )
                    );
                    // statement.setObject(i, parameterValue);
                    sqlStatementHandler.handle(i, parameterValue, statement);
                }
                LogUtils.writeLog2(LogUtils.Level.INFO, parametersLog.toString());
            }
            val startingTime = (double) System.currentTimeMillis();

            // TODO: should set time-out using `.setQueryTimeout(int seconds);`
            resultSet = statement.executeQuery();

            val endingTime = (double) System.currentTimeMillis();
            val duration = (endingTime - startingTime) / 1000D;

            LogUtils.writeLog2(
                    LogUtils.Level.INFO, String.format("Executed SQL statement take %.2f second(s)", duration)
            );

            var count = 0;
            while (resultSet.next()) {
                result.add(handler.handle(resultSet));
                count++;
            }
            LogUtils.writeLog2(LogUtils.Level.INFO, "Fetched {} row(s)", count);
        } catch (SQLException e) {
            LogUtils.writeLog2(e.getMessage(), e);
            result = null;
        } finally {
            this.closeStatementAndResultSet(statement, resultSet);
        }
        return result;
    }

    @Override
    @SuppressWarnings("SqlSourceToSinkFlow")
    public <T> List<T> executeOracleStoreProcedure(Connection connection,
                                                   String storeProcedureName,
                                                   Map<Integer, Object> inParameters,
                                                   Map<Integer, OracleTypeEnum> outParameters,
                                                   CallableStatementHandler<List<T>> handler) {
        CallableStatement statement = null;
        List<T> result;
        try {

            int numberOfParameters = 0;

            if (inParameters != null && !inParameters.isEmpty()) {
                numberOfParameters += inParameters.size();
            }
            if (outParameters != null && !outParameters.isEmpty()) {
                numberOfParameters += outParameters.size();
            }
            String parameterString = "";
            for (int i = 0; i < numberOfParameters; i++) {
                if (i == numberOfParameters - 1) {
                    parameterString += "?";
                } else {
                    parameterString += "?, ";
                }
            }
            String sqlCallingSPStatement = String.format(
                    "{ CALL %s(%s) }",
                    storeProcedureName,
                    parameterString
            );
            statement = connection.prepareCall(sqlCallingSPStatement);
            LogUtils.writeLog2(LogUtils.Level.INFO, "\n" + sqlCallingSPStatement.replaceAll("^\\n+|\\n+$", CommonConstant.EMPTY_STRING));
            var inParameterIsNotNullAndNotEmpty = inParameters != null && !inParameters.isEmpty();
            if (inParameterIsNotNullAndNotEmpty) {
                StringBuilder parametersLog = new StringBuilder("input parameters:");
                for (Integer i : inParameters.keySet()) {
                    Object parameterValue = inParameters.get(i);
                    String aNull = String.format(
                            "\n\t- parameter %s: %s",
                            String.format("%-3d %-20s)", i, "("),
                            "NULL"
                    );
                    if (parameterValue == null) {
                        statement.setObject(i, null);
                        parametersLog.append(
                                aNull
                        );
                        continue;
                    }
                    Class<?> parameterClass = inParameters.get(i).getClass();
                    if (parameterClass.isPrimitive()) {
                        statement.setObject(i, null);
                        parametersLog.append(
                                aNull
                        );
                        continue;
                    }
                    String parameterSimpleClassName = parameterClass.getSimpleName();
                    SqlStatementHandler sqlStatementHandler = DATA_TYPE_AND_SQL_STATEMENT_METHOD_MAP.get(parameterSimpleClassName);
                    if (sqlStatementHandler == null) {
                        throw new RuntimeException("unknown parameter type");
                    }
                    parametersLog.append(
                            String.format(
                                    "\n\t- parameter %s: %s",
                                    String.format("%-3d %-20s)", i, "(" + parameterSimpleClassName),
                                    parameterValue
                            )
                    );
                    sqlStatementHandler.handle(i, parameterValue, statement);
                }
                LogUtils.writeLog2(LogUtils.Level.INFO, parametersLog.toString());
            }
            var outParameterIsNotNullAndNotEmpty = outParameters != null && !outParameters.isEmpty();
            if (outParameterIsNotNullAndNotEmpty) {
                StringBuilder parametersLog = new StringBuilder("output parameters:");
                for (Integer i : outParameters.keySet()) {
                    statement.registerOutParameter(i, outParameters.get(i).getType());
                    parametersLog.append(
                            String.format(
                                    "\n\t- parameter %s: %s",
                                    String.format("%-3d", i),
                                    outParameters.get(i).name()
                            )
                    );
                }
                LogUtils.writeLog2(LogUtils.Level.INFO, parametersLog.toString());
            }
            val startingTime = (double) System.currentTimeMillis();

            statement.execute();

            val endingTime = (double) System.currentTimeMillis();
            val duration = (endingTime - startingTime) / 1000D;

            LogUtils.writeLog2(
                    LogUtils.Level.INFO, String.format("Executed SQL statement take %.2f second(s)", duration)
            );

            result = new ArrayList<>(handler.handle(statement));
        } catch (SQLException e) {
            LogUtils.writeLog2(e.getMessage(), e);
            result = null;
        } finally {
            this.closeStatementAndResultSet(statement, null);
        }
        return result;
    }

    @Override
    @SuppressWarnings("SqlSourceToSinkFlow")
    public Map<String, Integer> executeOracleStoreProcedureBatch(Connection connection,
                                                                 String storeProcedureName,
                                                                 List<Map<Integer, Object>> inParameterMaps) {
        val batchExecutionResult = new HashMap<String, Integer>();
        CallableStatement statement = null;
        try {

            int numberOfParameters = 0;

            if (inParameterMaps != null && !inParameterMaps.isEmpty()) {
                if (!inParameterMaps.get(0).isEmpty()) {
                    numberOfParameters += inParameterMaps.get(0).size();
                }
            } else {
                throw new RuntimeException("inParameterMaps is empty");
            }
            String parameterString = "";
            for (int i = 0; i < numberOfParameters; i++) {
                if (i == numberOfParameters - 1) {
                    parameterString += "?";
                } else {
                    parameterString += "?, ";
                }
            }
            String sqlCallingSPStatement = String.format(
                    "{ CALL %s(%s) }",
                    storeProcedureName,
                    parameterString
            );
            statement = connection.prepareCall(sqlCallingSPStatement);
            LogUtils.writeLog2(LogUtils.Level.INFO, "\n" + sqlCallingSPStatement.replaceAll("^\\n+|\\n+$", CommonConstant.EMPTY_STRING));
            for (Map<Integer, Object> inParameters : inParameterMaps) {
                var inParameterIsNotNullAndNotEmpty = inParameters != null && !inParameters.isEmpty();
                if (inParameterIsNotNullAndNotEmpty) {
                    StringBuilder parametersLog = new StringBuilder("input parameters:");
                    for (Integer i : inParameters.keySet()) {
                        Object parameterValue = inParameters.get(i);
                        String aNull = String.format(
                                "\n\t- parameter %s: %s",
                                String.format("%-3d %-20s)", i, "("),
                                "NULL"
                        );
                        if (parameterValue == null) {
                            statement.setObject(i, null);
                            parametersLog.append(
                                    aNull
                            );
                            continue;
                        }
                        Class<?> parameterClass = inParameters.get(i).getClass();
                        if (parameterClass.isPrimitive()) {
                            statement.setObject(i, null);
                            parametersLog.append(
                                    aNull
                            );
                            continue;
                        }
                        String parameterSimpleClassName = parameterClass.getSimpleName();
                        SqlStatementHandler sqlStatementHandler = DATA_TYPE_AND_SQL_STATEMENT_METHOD_MAP.get(parameterSimpleClassName);
                        if (sqlStatementHandler == null) {
                            throw new RuntimeException("unknown parameter type");
                        }
                        parametersLog.append(
                                String.format(
                                        "\n\t- parameter %s: %s",
                                        String.format("%-3d %-20s)", i, "(" + parameterSimpleClassName),
                                        parameterValue
                                )
                        );
                        sqlStatementHandler.handle(i, parameterValue, statement);
                    }
                    statement.addBatch();
                    LogUtils.writeLog2(LogUtils.Level.INFO, parametersLog.toString());
                }
            }
            var success = 0;
            var successNoInfo = 0;
            var executeFailed = 0;
            val startingTime = (double) System.currentTimeMillis();

            var result = statement.executeBatch();

            val endingTime = (double) System.currentTimeMillis();
            val duration = (endingTime - startingTime) / 1000D;

            LogUtils.writeLog2(
                    LogUtils.Level.INFO, String.format("Executed SQL statement take %.2f second(s)", duration)
            );

            if (result.length > 0) {
                for (int i : result) {
                    if (i >= 0) {
                        success++;
                    } else if (i == -2) {
                        successNoInfo++;
                    } else if (i == -3) {
                        executeFailed++;
                    }
                }
                LogUtils.writeLog2(
                        LogUtils.Level.INFO,
                        "Executed batch, result:" +
                                "\n    - Success: {}" +
                                "\n    - Success no info: {}" +
                                "\n    - Execute failed: {}",
                        success,
                        successNoInfo,
                        executeFailed
                );
            }
            batchExecutionResult.put(SUCCESS_KEY_NAME, success);
            batchExecutionResult.put(SUCCESS_BUT_NO_INFO_KEY_NAME, successNoInfo);
            batchExecutionResult.put(FAILED_KEY_NAME, executeFailed);
        } catch (SQLException e) {
            LogUtils.writeLog2(e.getMessage(), e);
            return null;
        } finally {
            this.closeStatementAndResultSet(statement, null);
        }
        return batchExecutionResult;
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public <T> List<T> executePostgresStoreProcedure(Connection connection,
                                                     String storeProcedureName,
                                                     Map<Integer, Object> inParameters,
                                                     Map<Integer, PostgresTypeEnum> outParameters,
                                                     CallableStatementHandler<List<T>> handler) {

        try {
            if (connection.getAutoCommit()) {
                LogUtils.writeLog2(LogUtils.Level.INFO, "Connection must be disabled auto-commit mode");
                return null;
            }
        } catch (SQLException e) {
            LogUtils.writeLog2("Cannot check auto-commit status of connection", e);
            return null;
        }

        CallableStatement statement = null;
        List<T> result;
        try {

            int numberOfParameters = 0;

            if (inParameters != null && !inParameters.isEmpty()) {
                numberOfParameters += inParameters.size();
            }
            if (outParameters != null && !outParameters.isEmpty()) {
                numberOfParameters += outParameters.size();
            }
            String parameterString = "";
            for (int i = 0; i < numberOfParameters; i++) {
                if (i == numberOfParameters - 1) {
                    parameterString += "?";
                } else {
                    parameterString += "?, ";
                }
            }
            String sqlCallingSPStatement = String.format(
                    "CALL %s(%s)",
                    storeProcedureName,
                    parameterString
            );
            statement = connection.prepareCall(sqlCallingSPStatement);
            LogUtils.writeLog2(LogUtils.Level.INFO, "\n" + sqlCallingSPStatement.replaceAll("^\\n+|\\n+$", CommonConstant.EMPTY_STRING));
            var inParameterIsNotNullAndNotEmpty = inParameters != null && !inParameters.isEmpty();
            if (inParameterIsNotNullAndNotEmpty) {
                StringBuilder parametersLog = new StringBuilder("input parameters:");
                for (Integer i : inParameters.keySet()) {
                    Object parameterValue = inParameters.get(i);
                    String aNull = String.format(
                            "\n\t- parameter %s: %s",
                            String.format("%-3d %-20s)", i, "("),
                            "NULL"
                    );
                    if (parameterValue == null) {
                        statement.setObject(i, null);
                        parametersLog.append(
                                aNull
                        );
                        continue;
                    }
                    Class<?> parameterClass = inParameters.get(i).getClass();
                    if (parameterClass.isPrimitive()) {
                        statement.setObject(i, null);
                        parametersLog.append(
                                aNull
                        );
                        continue;
                    }
                    String parameterSimpleClassName = parameterClass.getSimpleName();
                    SqlStatementHandler sqlStatementHandler = DATA_TYPE_AND_SQL_STATEMENT_METHOD_MAP.get(parameterSimpleClassName);
                    if (sqlStatementHandler == null) {
                        throw new RuntimeException("unknown parameter type");
                    }
                    parametersLog.append(
                            String.format(
                                    "\n\t- parameter %s: %s",
                                    String.format("%-3d %-20s)", i, "(" + parameterSimpleClassName),
                                    parameterValue
                            )
                    );
                    sqlStatementHandler.handle(i, parameterValue, statement);
                }
                LogUtils.writeLog2(LogUtils.Level.INFO, parametersLog.toString());
            }
            var outParameterIsNotNullAndNotEmpty = outParameters != null && !outParameters.isEmpty();
            if (outParameterIsNotNullAndNotEmpty) {
                StringBuilder parametersLog = new StringBuilder("output parameters:");
                for (Integer i : outParameters.keySet()) {
                    statement.registerOutParameter(i, outParameters.get(i).getType());
                    parametersLog.append(
                            String.format(
                                    "\n\t- parameter %s: %s",
                                    String.format("%-3d", i),
                                    outParameters.get(i).name()
                            )
                    );
                }
                LogUtils.writeLog2(LogUtils.Level.INFO, parametersLog.toString());
            }
            val startingTime = (double) System.currentTimeMillis();

            statement.execute();

            val endingTime = (double) System.currentTimeMillis();
            val duration = (endingTime - startingTime) / 1000D;

            LogUtils.writeLog2(
                    LogUtils.Level.INFO, String.format("Executed SQL statement take %.2f second(s)", duration)
            );

            result = new ArrayList<>(handler.handle(statement));
        } catch (SQLException e) {
            LogUtils.writeLog2(e.getMessage(), e);
            result = null;
        } finally {
            this.closeStatementAndResultSet(statement, null);
        }
        return result;
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public int executeMutation(Connection connection,
                               String sqlString,
                               Map<Integer, Object> parameter) {
        PreparedStatement statement = null;
        int rowAffected = 0;
        try {
            // sqlString = MyStringUtils.minifyString(sqlString);
            statement = connection.prepareStatement(sqlString);
            LogUtils.writeLog2(LogUtils.Level.INFO, "\n" + sqlString.replaceAll("^\\n+|\\n+$", CommonConstant.EMPTY_STRING));
            var parameterIsNotNullAndNotEmpty = parameter != null && !parameter.isEmpty();
            if (parameterIsNotNullAndNotEmpty) {
                StringBuilder parametersLog = new StringBuilder("parameters:");
                for (Integer i : parameter.keySet()) {
                    Object parameterValue = parameter.get(i);
                    String aNull = String.format(
                            "\n\t- parameter %s: %s",
                            String.format("%-3d %-20s)", i, "("),
                            "NULL"
                    );
                    if (parameterValue == null) {
                        statement.setObject(i, null);
                        parametersLog.append(
                                aNull
                        );
                        continue;
                    }
                    Class<?> parameterClass = parameter.get(i).getClass();
                    if (parameterClass.isPrimitive()) {
                        statement.setObject(i, null);
                        parametersLog.append(
                                aNull
                        );
                        continue;
                    }
                    String parameterSimpleClassName = parameterClass.getSimpleName();
                    SqlStatementHandler sqlStatementHandler = DATA_TYPE_AND_SQL_STATEMENT_METHOD_MAP.get(parameterSimpleClassName);
                    if (sqlStatementHandler == null) {
                        throw new RuntimeException("unknown parameter type");
                    }
                    parametersLog.append(
                            String.format(
                                    "\n\t- parameter %s: %s",
                                    String.format("%-3d %-20s)", i, "(" + parameterSimpleClassName),
                                    parameterValue
                            )
                    );
                    // statement.setObject(i, parameterValue);
                    sqlStatementHandler.handle(i, parameterValue, statement);
                }
                LogUtils.writeLog2(LogUtils.Level.INFO, parametersLog.toString());
            }
            val startingTime = (double) System.currentTimeMillis();

            rowAffected = statement.executeUpdate();

            val endingTime = (double) System.currentTimeMillis();
            val duration = (endingTime - startingTime) / 1000D;

            LogUtils.writeLog2(
                    LogUtils.Level.INFO, String.format("Executed SQL statement take %.2f second(s)", duration)
            );

        } catch (SQLException e) {
            LogUtils.writeLog2(e.getMessage(), e);
        } finally {
            this.closeStatementAndResultSet(statement, null);
        }
        LogUtils.writeLog2(LogUtils.Level.INFO, "Modified {} row(s)", rowAffected);
        return rowAffected;
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public BigDecimal executeInsertReturnId(Connection connection,
                                            String sqlString,
                                            Map<Integer, Object> parameter,
                                            DBTypeEnum dbType) {
        if (dbType.equals(DBTypeEnum.MYSQL) || dbType.equals(DBTypeEnum.MSSQL)) {
            throw new RuntimeException("Unsupported db type");
        }

        PreparedStatement statement = null;
        BigDecimal id = BigDecimal.ZERO;
        try {
            statement = connection.prepareStatement(sqlString);
            LogUtils.writeLog2(LogUtils.Level.INFO, "\n" + sqlString.replaceAll("^\\n+|\\n+$", CommonConstant.EMPTY_STRING));
            var parameterIsNotNullAndNotEmpty = parameter != null && !parameter.isEmpty();
            if (parameterIsNotNullAndNotEmpty) {
                StringBuilder parametersLog = new StringBuilder("parameters:");
                for (Integer i : parameter.keySet()) {
                    Object parameterValue = parameter.get(i);
                    String aNull = String.format(
                            "\n\t- parameter %s: %s",
                            String.format("%-3d %-20s)", i, "("),
                            "NULL"
                    );
                    if (parameterValue == null) {
                        statement.setObject(i, null);
                        parametersLog.append(
                                aNull
                        );
                        continue;
                    }
                    Class<?> parameterClass = parameter.get(i).getClass();
                    if (parameterClass.isPrimitive()) {
                        statement.setObject(i, null);
                        parametersLog.append(
                                aNull
                        );
                        continue;
                    }
                    String parameterSimpleClassName = parameterClass.getSimpleName();
                    SqlStatementHandler sqlStatementHandler = DATA_TYPE_AND_SQL_STATEMENT_METHOD_MAP.get(parameterSimpleClassName);
                    if (sqlStatementHandler == null) {
                        throw new RuntimeException("unknown parameter type");
                    }
                    parametersLog.append(
                            String.format(
                                    "\n\t- parameter %s: %s",
                                    String.format("%-3d %-20s)", i, "(" + parameterSimpleClassName),
                                    parameterValue
                            )
                    );
                    sqlStatementHandler.handle(i, parameterValue, statement);
                }
                LogUtils.writeLog2(LogUtils.Level.INFO, parametersLog.toString());
            }
            val startingTime = (double) System.currentTimeMillis();

            if (dbType.equals(DBTypeEnum.ORACLE)) {
                val oraclePreparedStatement = (OraclePreparedStatement) statement.unwrap(PreparedStatement.class);
                val index = parameter != null && !parameter.isEmpty() ? parameter.size() + 1 : 2;
                oraclePreparedStatement.registerReturnParameter(index, OracleTypeEnum.NUMBER.getType());
                oraclePreparedStatement.executeUpdate();

                ResultSet rs = oraclePreparedStatement.getReturnResultSet();
                if (rs.next()) {
                    id = rs.getBigDecimal(1);
                }
                rs.close();
            } else {
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    id = rs.getBigDecimal(1);
                }
            }

            val endingTime = (double) System.currentTimeMillis();
            val duration = (endingTime - startingTime) / 1000D;

            LogUtils.writeLog2(
                    LogUtils.Level.INFO, String.format("Executed SQL statement take %.2f second(s)", duration)
            );

        } catch (SQLException e) {
            throw new RuntimeException("Cannot insert");
        } finally {
            this.closeStatementAndResultSet(statement, null);
        }
        return id;
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public Map<String, Integer> executeMutationBatch(Connection connection,
                                                     String sqlString,
                                                     List<Map<Integer, Object>> parameterMapList) {
        val batchExecutionResult = new HashMap<String, Integer>();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sqlString);
            LogUtils.writeLog2(LogUtils.Level.INFO, "\n" + sqlString.replaceAll("^\\n+|\\n+$", CommonConstant.EMPTY_STRING));
            val parameterMapListIsNotNullAndNotEmpty = parameterMapList != null && !parameterMapList.isEmpty();
            if (parameterMapListIsNotNullAndNotEmpty) {
                for (Map<Integer, Object> parameter : parameterMapList) {
                    val parameterIsNotNullAndNotEmpty = parameter != null && !parameter.isEmpty();
                    if (parameterIsNotNullAndNotEmpty) {
                        StringBuilder parametersLog = new StringBuilder("parameters:");
                        for (Integer i : parameter.keySet()) {
                            Object parameterValue = parameter.get(i);
                            String aNull = String.format(
                                    "\n\t- parameter %s: %s",
                                    String.format("%-3d %-20s)", i, "("),
                                    "NULL"
                            );
                            if (parameterValue == null) {
                                statement.setObject(i, null);
                                parametersLog.append(
                                        aNull
                                );
                                continue;
                            }
                            Class<?> parameterClass = parameter.get(i).getClass();
                            if (parameterClass.isPrimitive()) {
                                statement.setObject(i, null);
                                parametersLog.append(
                                        aNull
                                );
                                continue;
                            }
                            String parameterSimpleClassName = parameterClass.getSimpleName();
                            SqlStatementHandler sqlStatementHandler = DATA_TYPE_AND_SQL_STATEMENT_METHOD_MAP.get(parameterSimpleClassName);
                            if (sqlStatementHandler == null) {
                                throw new RuntimeException("unknown parameter type");
                            }
                            parametersLog.append(
                                    String.format(
                                            "\n\t- parameter %s: %s",
                                            String.format("%-3d %-20s)", i, "(" + parameterSimpleClassName),
                                            parameterValue
                                    )
                            );
                            sqlStatementHandler.handle(i, parameterValue, statement);
                        }
                        LogUtils.writeLog2(LogUtils.Level.INFO, parametersLog.toString());
                        statement.addBatch();
                    }
                }
            }
            var success = 0;
            var successNoInfo = 0;
            var executeFailed = 0;
            val startingTime = (double) System.currentTimeMillis();

            var result = statement.executeBatch();

            val endingTime = (double) System.currentTimeMillis();
            val duration = (endingTime - startingTime) / 1000D;

            LogUtils.writeLog2(
                    LogUtils.Level.INFO, String.format("Executed SQL statement take %.2f second(s)", duration)
            );

            if (result.length > 0) {
                for (int i : result) {
                    if (i >= 0) {
                        success++;
                    } else if (i == -2) {
                        successNoInfo++;
                    } else if (i == -3) {
                        executeFailed++;
                    }
                }
                LogUtils.writeLog2(
                        LogUtils.Level.INFO,
                        "Executed batch, result:" +
                                "\n    - Success: {}" +
                                "\n    - Success no info: {}" +
                                "\n    - Execute failed: {}",
                        success,
                        successNoInfo,
                        executeFailed
                );
                batchExecutionResult.put(SUCCESS_KEY_NAME, success);
                batchExecutionResult.put(SUCCESS_BUT_NO_INFO_KEY_NAME, successNoInfo);
                batchExecutionResult.put(FAILED_KEY_NAME, executeFailed);
            }
        } catch (SQLException e) {
            LogUtils.writeLog2(e.getMessage(), e);
            return null;
        } finally {
            this.closeStatementAndResultSet(statement, null);
        }
        return batchExecutionResult;
    }

    public void closeStatementAndResultSet(PreparedStatement statement, ResultSet resultSet) {
        LogUtils.writeLog2(LogUtils.Level.INFO, "Closing result set and statement");
        try {
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
                // LogUtils.writeLogForDBExecutor(LogUtils.Level.INFO, "Result set closed");
            }
            if (statement != null && !statement.isClosed()) {
                statement.close();
                // LogUtils.writeLogForDBExecutor(LogUtils.Level.INFO, "Statement closed");
            }
        } catch (SQLException e) {
            LogUtils.writeLog2(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
