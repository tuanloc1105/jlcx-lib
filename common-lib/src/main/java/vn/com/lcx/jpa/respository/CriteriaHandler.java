package vn.com.lcx.jpa.respository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface CriteriaHandler<E> {

    /**
     * Builds a predicate for the given query.
     *
     * @param cb the criteria builder
     * @param cq the criteria query
     * @param root the root of the query
     * @return the predicate
     */
    Predicate toPredicate(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<E> root);

}
