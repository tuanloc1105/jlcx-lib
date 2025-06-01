package vn.com.lcx.jpa.respository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import vn.com.lcx.jpa.context.EntityContainer;
import vn.com.lcx.jpa.context.JpaConstant;
import vn.com.lcx.jpa.context.JpaContext;
import vn.com.lcx.jpa.exception.JpaException;
import vn.com.lcx.jpa.exception.JpaMethodNotImplementException;

import java.util.function.Consumer;

public abstract class JpaRepositoryImpl<E, ID> implements JpaRepository<E, ID> {

    @Override
    public void save(E entity) {
        handlingTransaction(entity.getClass(), session -> session.persist(entity));
    }

    @Override
    public void update(E entity) {
        handlingTransaction(entity.getClass(), session -> session.merge(entity));
    }

    @Override
    public void delete(E entity) {
        handlingTransaction(entity.getClass(), session -> session.remove(entity));
    }

    @Override
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
