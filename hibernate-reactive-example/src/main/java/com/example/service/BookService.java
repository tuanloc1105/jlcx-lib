package com.example.service;

import com.example.entity.Book;
import com.example.enums.AppError;
import com.example.http.request.CreateBookRequest;
import com.example.mapper.BookMapper;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.reactive.stage.Stage;
import org.slf4j.MDC;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class BookService {

    private final Stage.SessionFactory sessionFactory;
    private final BookMapper bookMapper;

    public Future<Void> createBook(RoutingContext ctx, CreateBookRequest request) {
        return Future.fromCompletionStage(
                        sessionFactory.withSession(
                                session -> {
                                    final var trace = ctx.<String>get(CommonConstant.TRACE_ID_MDC_KEY_NAME);
                                    final var operation = ctx.<String>get(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
                                    MDC.put(CommonConstant.TRACE_ID_MDC_KEY_NAME, trace);
                                    MDC.put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, operation);
                                    return session.createQuery("from Book b where b.isbn = :isbn", Book.class)
                                            .setParameter("isbn", request.getIsbn())
                                            .getSingleResultOrNull()
                                            .thenCompose(
                                                    it -> {
                                                        MDC.remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
                                                        MDC.remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
                                                        return CompletableFuture.completedStage(it);
                                                    }
                                            );
                                }
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
                                                    final var trace = ctx.<String>get(CommonConstant.TRACE_ID_MDC_KEY_NAME);
                                                    final var operation = ctx.<String>get(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
                                                    MDC.put(CommonConstant.TRACE_ID_MDC_KEY_NAME, trace);
                                                    MDC.put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, operation);
                                                    final var newBook = bookMapper.map(request);
                                                    return session.persist(newBook)
                                                            .thenCompose(
                                                                    v -> {
                                                                        MDC.remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
                                                                        MDC.remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
                                                                        return CompletableFuture.completedStage(CommonConstant.VOID);
                                                                    }
                                                            )
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
