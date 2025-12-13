package com.example.controller;

import com.example.http.request.CreateBookRequest;
import com.example.service.BookService;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.annotation.process.RequestBody;
import vn.com.lcx.vertx.base.annotation.process.RestController;

@Component
@RestController(path = "/api")
@RequiredArgsConstructor
public class APIController {

    private final BookService bookService;

    @Post(path = "/create_book")
    public Future<Void> createBook(RoutingContext ctx, @RequestBody CreateBookRequest req) {
        return bookService.createBook(ctx, req);
    }

}
