package vn.com.lcx.common.database.repository;

import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.Pageable;
import vn.com.lcx.common.database.specification.Specification;

import java.util.List;
import java.util.Map;

public interface LCXRepository<T> {

    int save(T entity);

    void save2(T entity);

    int update(T entity);

    int delete(T entity);

    Map<String, Integer> save(List<T> entities);

    Map<String, Integer> update(List<T> entities);

    Map<String, Integer> delete(List<T> entities);

    Page<T> find(Specification specification, Pageable pageable);

    List<T> find(Specification specification);

}
