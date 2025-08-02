package com.example.service;

import com.example.entity.Book;
import com.example.enums.AppError;
import com.example.http.request.CreateBookRequest;
import com.example.mapper.BookMapper;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import org.hibernate.reactive.stage.Stage;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookService {

    private final Stage.SessionFactory sessionFactory;
    private final BookMapper bookMapper;

    public Future<Void> createBook(CreateBookRequest request) {
        return Future.fromCompletionStage(
                        sessionFactory.withSession(session ->
                                session.createQuery("from Book b where b.isbn = :isbn", Book.class)
                                        .setParameter("isbn", request.getIsbn())
                                        .getSingleResultOrNull()
                        )
                ).map(Optional::ofNullable)
                .compose(
                        it ->
                                it.isEmpty() ? Future.succeededFuture(CommonConstant.VOID) :
                                        Future.failedFuture(new InternalServiceException(AppError.DATA_EXISTED))
                ).compose(
                        it ->
                                Future.fromCompletionStage(
                                        sessionFactory.withTransaction(
                                                (session, transaction) -> {
                                                    final var newBook = bookMapper.map(request);
                                                    return session.persist(newBook)
                                                            .exceptionally(e -> {
                                                                transaction.markForRollback();
                                                                return CommonConstant.VOID;
                                                            });
                                                }
                                        )
                                )
                );
    }

}
