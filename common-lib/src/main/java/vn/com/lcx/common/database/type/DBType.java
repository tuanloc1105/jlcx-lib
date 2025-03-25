package vn.com.lcx.common.database.type;

public interface DBType {

    String getDefaultDriverClassName();

    String getTemplateUrlConnectionString();

    String getShowDbVersionSqlStatement();

}
