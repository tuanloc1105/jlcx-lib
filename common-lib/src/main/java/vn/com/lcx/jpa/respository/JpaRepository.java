package vn.com.lcx.jpa.respository;

public interface JpaRepository<E, ID> {

    void save(E entity);

    void update(E entity);

    void delete(E entity);

    E findById(ID id);
}
