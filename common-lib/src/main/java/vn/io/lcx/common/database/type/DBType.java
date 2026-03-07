package vn.io.lcx.common.database.type;

public interface DBType {

    String getDefaultDriverClassName();

    String getTemplateUrlConnectionString();

    String getShowDbVersionSqlStatement();

    String getDialectClass();

}
