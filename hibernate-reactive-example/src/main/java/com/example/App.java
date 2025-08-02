package com.example;

import com.example.entity.Author;
import com.example.entity.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.stage.Stage;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.app.VertxApplication;
import vn.com.lcx.vertx.base.custom.MyVertxDeployment;

import java.time.LocalDate;

import static org.hibernate.reactive.stage.Stage.fetch;

@VertxApplication
@Component
@RequiredArgsConstructor
@Slf4j
public class App {

    private final Stage.SessionFactory sessionFactory;

    public static void main(String[] args) {
        MyVertxDeployment.getInstance().deployVerticle(App.class);
    }

    // @PostConstruct
    public void post() {
        Author author1 = new Author("Iain M. Banks");
        Author author2 = new Author("Neal Stephenson");
        Book book1 = new Book("1-85723-235-6", "Feersum Endjinn", author1, LocalDate.of(1994, java.time.Month.JANUARY, 1));
        Book book2 = new Book("0-380-97346-4", "Cryptonomicon", author2, LocalDate.of(1999, java.time.Month.MAY, 1));
        Book book3 = new Book("0-553-08853-X", "Snow Crash", author2, LocalDate.of(1992, java.time.Month.JUNE, 1));
        author1.getBooks().add(book1);
        author2.getBooks().add(book2);
        author2.getBooks().add(book3);
        try {
            log.info("start");
            // obtain a reactive session
            sessionFactory.withTransaction(
                            // persist the Authors with their Books in a transaction
                            (session, tx) -> session.persist(author1, author2)
                    )
                    // wait for it to finish
                    .toCompletableFuture().join();

            sessionFactory.withSession(
                            // retrieve a Book
                            session -> session.find(Book.class, book1.getId())
                                    // print its title
                                    .thenAccept(book -> log.info(book.getTitle() + " is a great book!"))
                    )
                    .toCompletableFuture().join();

            sessionFactory.withSession(
                            // retrieve both Authors at once
                            session -> session.find(Author.class, author1.getId(), author2.getId())
                                    .thenAccept(authors -> authors.forEach(author -> log.info(author.getName())))
                    )
                    .toCompletableFuture().join();

            sessionFactory.withSession(
                            // retrieve an Author
                            session -> session.find(Author.class, author2.getId())
                                    // lazily fetch their books
                                    .thenCompose(author -> fetch(author.getBooks())
                                            // print some info
                                            .thenAccept(books -> {
                                                log.info(author.getName() + " wrote " + books.size() + " books");
                                                books.forEach(book -> log.info(book.getTitle()));
                                            })
                                    )
                    )
                    .toCompletableFuture().join();

            sessionFactory.withSession(
                            // retrieve the Author lazily from a Book
                            session -> session.find(Book.class, book1.getId())
                                    // fetch a lazy field of the Book
                                    .thenCompose(book -> fetch(book.getAuthor())
                                            // print the lazy field
                                            .thenAccept(author -> log.info("{} wrote '{}'\n", author.getName(), book1.getTitle()))
                                    )
                    )
                    .toCompletableFuture().join();

            sessionFactory.withSession(
                            // query the entire Book entities
                            session -> session.createQuery(
                                            "from Book book join fetch book.author order by book.title desc",
                                            Book.class
                                    )
                                    .getResultList()
                                    .thenAccept(books -> books.forEach(
                                            b -> log.info(
                                                    "{}: {} ({})\n",
                                                    b.getIsbn(),
                                                    b.getTitle(),
                                                    b.getAuthor().getName()
                                            )
                                    ))
                    )
                    .toCompletableFuture().join();

            sessionFactory.withTransaction(
                            // retrieve a Book
                            (session, tx) -> session.find(Book.class, book2.getId())
                                    // delete the Book
                                    .thenCompose(session::remove)
                    )
                    .toCompletableFuture().join();

            sessionFactory.withTransaction(
                            // delete all the Books in a transaction
                            (session, tx) -> session.createMutationQuery("delete Book").executeUpdate()
                                    // delete all the Authors
                                    .thenCompose($ -> session.createMutationQuery("delete Author").executeUpdate())
                    )
                    .toCompletableFuture().join();
            log.info("done");
        } catch (Exception e) {
            log.error("error", e);
        }
    }
}
