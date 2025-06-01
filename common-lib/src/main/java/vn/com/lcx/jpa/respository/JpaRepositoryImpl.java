package vn.com.lcx.jpa.respository;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import vn.com.lcx.jpa.context.EntityContainer;
import vn.com.lcx.jpa.context.JpaConstant;
import vn.com.lcx.jpa.context.JpaContext;
import vn.com.lcx.jpa.exception.JpaException;
import vn.com.lcx.jpa.exception.JpaMethodNotImplementException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class JpaRepositoryImpl<E, ID> {

    public void save(E entity) {
        handlingTransaction(entity.getClass(), session -> session.persist(entity));
    }

    public void update(E entity) {
        handlingTransaction(entity.getClass(), session -> session.merge(entity));
    }

    public void delete(E entity) {
        handlingTransaction(entity.getClass(), session -> session.remove(entity));
    }

    public E findById(ID id) {
        throw new JpaMethodNotImplementException("This method is not implemented");
    }

    public void handlingTransaction(Class<?> entityClass, Consumer<Session> consumer) {
        final SessionFactory sessionFactory = EntityContainer.getEntityManager(entityClass);
        // This variable is the result of checking whether the method
        // in the class annotated with `@vn.com.lcx.jpa.annotation.Service`
        // is also annotated with `@vn.com.lcx.jpa.annotation.Transactional`.
        // This check occurs in the Proxy layer of the class annotated with `@vn.com.lcx.jpa.annotation.Service`.
        boolean transactionOpen = JpaContext.isTransactionOpen();
        if (transactionOpen) {
            boolean sessionIsJustCreate = false;
            Session currentSessionInContext = JpaContext.getSession(entityClass);
            if (currentSessionInContext == null) {
                currentSessionInContext = sessionFactory.openSession();
                sessionIsJustCreate = true;
            }
            int transactionMode = JpaContext.getTransactionMode();
            Transaction transaction = JpaContext.getTransaction(entityClass);
            switch (transactionMode) {
                case JpaConstant.CREATE_NEW_TRANSACTION_MODE:
                    if (sessionIsJustCreate) {
                        transaction = currentSessionInContext.beginTransaction();
                    } else {
                        if (transaction != null) {
                            transaction.commit();
                        }
                        transaction = currentSessionInContext.beginTransaction();
                    }
                    break;
                case JpaConstant.USE_EXISTING_TRANSACTION_MODE:
                    if (transaction == null) {
                        transaction = currentSessionInContext.beginTransaction();
                    }
                    break;
                default:
                    throw new JpaException("Invalid transaction mode");
            }
            consumer.accept(currentSessionInContext);
            JpaContext.setSession(entityClass, currentSessionInContext);
            JpaContext.setTransaction(entityClass, transaction);
        } else {
            try (Session session = sessionFactory.openSession()) {
                consumer.accept(session);
            }
        }
    }

}
