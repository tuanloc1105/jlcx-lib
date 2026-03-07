package vn.io.lcx.reactive.repository;

import io.vertx.core.Future;
import org.hibernate.reactive.stage.Stage;
import vn.io.lcx.common.database.pageable.Page;
import vn.io.lcx.common.database.pageable.Pageable;
import vn.io.lcx.jpa.respository.CriteriaHandler;

import java.util.List;
import java.util.Optional;

/**
 * The base interface for Reactive Hibernate Repositories.
 * <p>
 * This interface defines standard CRUD and finder operations that are
 * automatically implemented
 * by the annotation processor for interfaces annotated with
 * {@link vn.io.lcx.reactive.annotation.HRRepository}.
 * </p>
 *
 * @param <T> The entity type managed by this repository.
 */
public interface HReactiveRepository<T> {

    /**
     * Persists or updates the given entity.
     * <p>
     * Uses {@link Stage.Session#merge(Object)} to merge the state of the given
     * entity into the
     * current persistence context.
     * </p>
     *
     * @param session The Hibernate Reactive session.
     * @param entity  The entity to save or update.
     * @return A {@link Future} containing the merged entity.
     */
    Future<T> save(Stage.Session session, T entity);

    /**
     * Persists or updates the given list of entities.
     * <p>
     * Uses {@link Stage.Session#merge(Object)} to merge the state of the given
     * entities into the current persistence context.
     * Processed in batches to avoid memory issues.
     * </p>
     *
     * @param session The Hibernate Reactive session.
     * @param entity  The list of entities to save or update.
     * @return A {@link Future} containing the list of merged entities.
     */
    Future<List<T>> save(Stage.Session session, List<T> entity);

    /**
     * Deletes the given entity.
     * <p>
     * Uses {@link Stage.Session#remove(Object)} to remove the entity instance.
     * </p>
     *
     * @param session The Hibernate Reactive session.
     * @param entity  The entity to delete.
     * @return A {@link Future} that completes when the operation is finished.
     */
    Future<Void> delete(Stage.Session session, T entity);

    /**
     * Deletes the given list of entities.
     * <p>
     * Uses {@link Stage.Session#remove(Object)} to remove the entity instances.
     * Processed in batches.
     * </p>
     *
     * @param session The Hibernate Reactive session.
     * @param entity  The list of entities to delete.
     * @return A {@link Future} that completes when the operation is finished.
     */
    Future<Void> delete(Stage.Session session, List<T> entity);

    /**
     * Finds entities matching the given criteria.
     * <p>
     * Constructs a {@link jakarta.persistence.criteria.CriteriaQuery} based on the
     * provided {@link CriteriaHandler}.
     * </p>
     *
     * @param session         The Hibernate Reactive session.
     * @param criteriaHandler A handler to build the Predicate for the query.
     * @return A {@link Future} containing a list of matching entities.
     */
    Future<List<T>> find(Stage.Session session, CriteriaHandler<T> criteriaHandler);

    /**
     * Finds a page of entities matching the given criteria.
     * <p>
     * Performs a paginated query and a count query to return a {@link Page} result.
     * </p>
     *
     * @param session  The Hibernate Reactive session.
     * @param handler  A handler to build the Predicate for the query.
     * @param pageable The pagination information (page number, size, sort).
     * @return A {@link Future} containing a page of matching entities.
     */
    public Future<Page<T>> find(Stage.Session session, CriteriaHandler<T> handler, Pageable pageable);

    /**
     * Finds a single entity matching the given criteria.
     * <p>
     * Uses {@code getSingleResultOrNull()} logic to retrieve an optional result.
     * </p>
     *
     * @param session The Hibernate Reactive session.
     * @param handler A handler to build the Predicate for the query.
     * @return A {@link Future} containing an {@link Optional} with the found
     *         entity, or empty if not found.
     */
    public Future<Optional<T>> findOne(Stage.Session session, CriteriaHandler<T> handler);

}
