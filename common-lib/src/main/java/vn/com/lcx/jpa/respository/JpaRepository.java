package vn.com.lcx.jpa.respository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.Pageable;

import java.util.List;
import java.util.Optional;

public interface JpaRepository<E, ID> {

    void save(E entity);

    void update(E entity);

    void delete(E entity);

    Optional<E> findById(ID id);

    Optional<E> findOne(CriteriaHandler<E> criteriaHandler);

    List<E> find(CriteriaHandler<E> criteriaHandler);

    Page<E> find(CriteriaHandler<E> criteriaHandler, Pageable pageable);

}
