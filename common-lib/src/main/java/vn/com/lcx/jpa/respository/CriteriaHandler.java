package vn.com.lcx.jpa.respository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface CriteriaHandler<E> {

    Predicate toPredicate(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<E> root);

}
