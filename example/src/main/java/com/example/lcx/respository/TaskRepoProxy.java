package com.example.lcx.respository;

public class TaskRepoProxy extends vn.com.lcx.jpa.respository.JpaRepositoryImpl<com.example.lcx.entity.TaskEntity, java.math.BigInteger> implements com.example.lcx.respository.TaskRepo {

    public java.util.List<com.example.lcx.entity.TaskEntity> find(vn.com.lcx.jpa.respository.JpaRepository.CriteriaHandler<com.example.lcx.entity.TaskEntity> handler) {
        java.util.List<com.example.lcx.entity.TaskEntity> result = new java.util.ArrayList<>();
        handlingTransaction(
                com.example.lcx.entity.TaskEntity.class,
                session -> {
                    final jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
                    final jakarta.persistence.criteria.CriteriaQuery<com.example.lcx.entity.TaskEntity> criteriaQuery = criteriaBuilder.createQuery(com.example.lcx.entity.TaskEntity.class);
                    jakarta.persistence.criteria.Root<com.example.lcx.entity.TaskEntity> root = criteriaQuery.from(com.example.lcx.entity.TaskEntity.class);
                    jakarta.persistence.criteria.Predicate predicate = handler.toPredicate(criteriaBuilder, criteriaQuery, root);
                    criteriaQuery.where(predicate);
                    org.hibernate.query.Query<com.example.lcx.entity.TaskEntity> query = session.createQuery(criteriaQuery);
                    result.addAll(query.getResultList());
                }
        );
        return result;
    }
}
