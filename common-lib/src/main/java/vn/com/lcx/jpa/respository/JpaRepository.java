package vn.com.lcx.jpa.respository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.List;

public interface JpaRepository<E, ID> {

    void save(E entity);

    void update(E entity);

    void delete(E entity);

    E findById(ID id);

    List<E> find(CriteriaHandler<E> criteriaHandler);

    interface CriteriaHandler<E> {
        // CriteriaQuery<E> handle(CriteriaBuilder cb, CriteriaQuery<E> cq, Root<E> root);

        Predicate toPredicate(CriteriaBuilder cb, CriteriaQuery<E> cq, Root<E> root);
    }

}
