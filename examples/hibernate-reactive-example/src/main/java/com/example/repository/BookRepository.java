package com.example.repository;

import com.example.entity.Book;
import io.vertx.core.Future;
import org.hibernate.reactive.stage.Stage;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.Pageable;
import vn.com.lcx.jpa.annotation.Modifying;
import vn.com.lcx.jpa.annotation.Param;
import vn.com.lcx.jpa.annotation.Query;
import vn.com.lcx.reactive.annotation.HRRepository;
import vn.com.lcx.reactive.repository.HReactiveRepository;

import java.util.List;
import java.util.Optional;

@HRRepository
public interface BookRepository extends HReactiveRepository<Book> {

    @Query("from Book b where b.isbn = :isbn")
    Future<Optional<Book>> findBook(Stage.Session session, @Param("isbn") String isbn);

    @Query("from Book b")
    Future<List<Book>> findBooks(Stage.Session session);

    @Query(value = "select * from Book b", isNative = true)
    Future<List<Book>> findBooksNative(Stage.Session session);

    @Query("from Book b")
    Future<Page<Book>> findBooksPage(Stage.Session session, Pageable pageable);

    @Query(value = "select id, title from Book b", isNative = true)
    Future<Page<Book>> findBooksNativePage(Stage.Session session, Pageable pageable);

    @Query(value = "delete from Book where isbn = :isbn", isNative = true)
    @Modifying
    Future<Integer> deleteBook(Stage.Session session, @Param("isbn") String isbn);

}
