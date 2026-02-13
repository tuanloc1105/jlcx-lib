package vn.com.lcx.common.database.utils;

import java.util.List;

/**
 * Internal representation of an index definition, holding all metadata
 * required for DDL generation.
 */
public class IndexInfo {
    private final String name;
    private final List<String> columns;
    private final boolean unique;

    public IndexInfo(String name, List<String> columns, boolean unique) {
        this.name = name;
        this.columns = columns;
        this.unique = unique;
    }

    public String getName() {
        return name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public boolean isUnique() {
        return unique;
    }
}
