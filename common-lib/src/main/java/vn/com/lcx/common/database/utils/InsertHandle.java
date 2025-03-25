package vn.com.lcx.common.database.utils;

import vn.com.lcx.common.database.type.DBTypeEnum;

import java.sql.Connection;

public class InsertHandle {


    // public static String handleExtraInsertSql(Class<? extends Connection> clazz, Class<?> entity) {

    //     val builder = SelectStatementBuilder.of(entity);

    //     val idColumnField = builder.getListOfField().stream()
    //             .filter(
    //                     f ->
    //                             f.getAnnotation(IdColumn.class) != null &&
    //                                     f.getAnnotation(ColumnName.class) != null &&
    //                                     (f.getType().isAssignableFrom(Long.class) || f.getType().isAssignableFrom(BigDecimal.class))
    //             )
    //             .findFirst()
    //             .orElse(null);

    //     if (idColumnField == null) {
    //         return "";
    //     }

    //     String sql;
    //     //noinspection EnhancedSwitchMigration
    //     switch (clazz.getPackageName()) {
    //         case "oracle":
    //             sql = String.format("\n RETURNING %s INTO ?", idColumnField.getAnnotation(ColumnName.class).name());
    //             break;
    //         case "postgresql":
    //             sql = String.format("\n RETURNING %s", idColumnField.getAnnotation(ColumnName.class).name());
    //             break;
    //         case "sqlserver":
    //         case "mysql":
    //         default:
    //             sql = "";
    //     }
    //     return sql;
    // }

    public static DBTypeEnum getDbType(Class<? extends Connection> clz) {
        String packageName = clz.getPackage().getName();
        if (packageName.contains("oracle")) {
            return DBTypeEnum.ORACLE;
        } else if (packageName.contains("postgresql")) {
            return DBTypeEnum.POSTGRESQL;
        } else if (packageName.contains("sqlserver")) {
            return DBTypeEnum.MSSQL;
        } else if (packageName.contains("mysql")) {
            return DBTypeEnum.MYSQL;
        }
        throw new RuntimeException("Unsupported DB Type");

        // return switch (clz.getPackageName()) {
        //     case "oracle" -> DBTypeEnum.ORACLE;
        //     case "postgresql" -> DBTypeEnum.POSTGRESQL;
        //     case "sqlserver" -> DBTypeEnum.MSSQL;
        //     case "mysql" -> DBTypeEnum.MYSQL;
        //     default -> throw new RuntimeException("Unsupported DB Type");
        // };
    }

}
