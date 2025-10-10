package vn.com.lcx.jpa.respository;

import org.hibernate.NonUniqueResultException;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Generic interface for JPA repository operations on entities.
 *
 * @param <E>  the entity type this repository manages
 * @param <ID> the type of the entity's identifier
 */
public interface JpaRepository<E, ID> {

    /**
     * Saves a given entity.
     *
     * @param entity must not be {@literal null}
     * @throws IllegalArgumentException in case the given entity is {@literal null}
     */
    void save(E entity);


    /**
     * Updates a given entity.
     *
     * @param entity must not be {@literal null}
     * @throws IllegalArgumentException in case the given entity is {@literal null} or does not exist
     */
    E update(E entity);


    /**
     * Deletes a given entity.
     *
     * @param entity must not be {@literal null}
     * @throws IllegalArgumentException in case the given entity is {@literal null} or does not exist
     */
    void delete(E entity);


    /**
     * Saves all given entities.
     *
     * @param entities must not be {@literal null} nor contain any {@literal null} values
     * @throws IllegalArgumentException in case the given entities or any of its entities is {@literal null}
     */
    void save(List<E> entities);


    /**
     * Updates all given entities.
     *
     * @param entities must not be {@literal null} nor contain any {@literal null} values
     * @throws IllegalArgumentException in case the given entities or any of its entities is {@literal null}
     */
    void update(List<E> entities);


    /**
     * Deletes all given entities.
     *
     * @param entities must not be {@literal null} nor contain any {@literal null} values
     * @throws IllegalArgumentException in case the given entities or any of its entities is {@literal null}
     */
    void delete(List<E> entities);


    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}
     * @return the entity with the given id or {@literal Optional#empty()} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    Optional<E> findById(ID id);


    /**
     * Returns a single entity matching the given criteria or {@link Optional#empty()} if none was found.
     *
     * @param criteriaHandler must not be {@literal null}
     * @return a single entity matching the given criteria or {@link Optional#empty()} if none was found
     * @throws NonUniqueResultException if the criteria produces more than one result
     * @throws IllegalArgumentException if criteriaHandler is {@literal null}
     */
    Optional<E> findOne(CriteriaHandler<E> criteriaHandler);


    /**
     * Returns all entities matching the given criteria.
     *
     * @param criteriaHandler must not be {@literal null}
     * @return a list of entities matching the given criteria
     * @throws IllegalArgumentException if criteriaHandler is {@literal null}
     */
    List<E> find(CriteriaHandler<E> criteriaHandler);


    /**
     * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
     *
     * @param criteriaHandler must not be {@literal null}
     * @param pageable        the pageable to request a paged result, must not be {@literal null}
     * @return a page of entities
     * @throws IllegalArgumentException if either criteriaHandler or pageable is {@literal null}
     */
    Page<E> find(CriteriaHandler<E> criteriaHandler, Pageable pageable);


    /**
     * Executes the given work unit using a JDBC connection, allowing for low-level JDBC operations.
     *
     * @param work the work to be performed with the connection, must not be {@literal null}
     * @throws IllegalArgumentException if work is {@literal null}
     */
    void findById(org.hibernate.jdbc.Work work);

}
